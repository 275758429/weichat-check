# WeChat 群聊关键词监控 — PC 端设计文档

## 目标

开发一款 Windows 桌面应用，基于 Windows UI Automation (UIA) 系统标准 API，模拟人工操作微信 PC 客户端，自动遍历指定微信群，识别聊天文字并匹配关键词，记录命中线索，提供结果查看与 HTTP 推送能力。

**硬性约束**：仅使用系统标准权限，不破解微信、不抓包、不修改客户端。

---

## 技术栈

| 层级 | 技术 |
|---|---|
| 开发语言 | Kotlin |
| UI 框架 | Compose Desktop |
| UI 自动化 | JNA 调用 Windows UIA COM 接口 |
| OCR 回退 | Tesseract (tess4j) |
| 数据存储 | SQLite (JDBC) |
| HTTP 推送 | Ktor Client |
| 打包 | Gradle + jpackage |

---

## 架构

### 模块分层

```
┌─────────────────────────────────────────────────┐
│                    UI 层                         │
│  Compose Desktop — 主窗口（配置页、群聊管理、    │
│                     关键词管理、线索列表、        │
│                     推送设置、启动引导）          │
├─────────────────────────────────────────────────┤
│                  引擎层                          │
│  ScanEngine — 扫描调度器：管理扫描周期、群聊     │
│               遍历、关键词匹配、数据持久化        │
│  WeChatUIA — UIA 操作封装：查找微信窗口、定位    │
│              聊天列表、提取消息节点、模拟滑动     │
│  OCRFallback — 图像回退：当 UIA 无法读取文字     │
│                时，截图 + OCR 识别                │
│  PushEngine — HTTP POST 异步推送命中线索         │
├─────────────────────────────────────────────────┤
│                   数据层                         │
│  SQLite (JDBC) — clues 表、config 表             │
├─────────────────────────────────────────────────┤
│                   工具层                         │
│  KeywordMatcher — 多模式匹配引擎                 │
│  RandomDelayer — 随机延时生成器                  │
│  WindowWatcher — 检测微信窗口状态（存活/卡死）   │
└─────────────────────────────────────────────────┘
```

### 单元职责边界

| 单元 | 职责 | 不做什么 |
|---|---|---|
| **ScanEngine** | 管理扫描周期、遍历群聊列表、调用匹配引擎、读写数据库、触发推送 | 不直接操作 Windows UIA、不处理 HTTP 响应 |
| **WeChatUIA** | 通过 UIA API 查找微信窗口、定位节点、提取文字、模拟滚动/点击 | 不做业务判断、不知道关键词是什么 |
| **OCRFallback** | 当 WeChatUIA.extractText() 返回空时，对指定区域截图并用 Tesseract OCR 识别 | 不主动调用，仅在被请求时执行 |
| **PushEngine** | 接收线索数据、构造 JSON、执行 HTTP POST、记录推送状态 | 不读取 UI、不决定推送时机 |

### 通信方式

- `ScanEngine` → `WeChatUIA`：直接方法调用（同进程）
- `WeChatUIA` → `OCRFallback`：当 UIA 返回空时自动降级调用
- `ScanEngine` → `PushEngine`：通过 Kotlin 协程异步触发

---

## 数据模型

### clues 表（线索记录）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INTEGER PRIMARY KEY AUTOINCREMENT | 自增 |
| group_name | TEXT | 群名称 |
| sender_name | TEXT | 发送人昵称 |
| send_time | TEXT | 发送时间（从微信 UI 解析的原始文本） |
| hit_content | TEXT | 命中的完整消息内容 |
| hit_keyword | TEXT | 命中的关键词 |
| match_type | TEXT | 匹配模式：`contains` / `regex` / `fuzzy` |
| created_at | INTEGER | 记录入库时间戳（Unix ms） |
| pushed | INTEGER DEFAULT 0 | 是否已推送：0 否 / 1 是 |

### config 表（应用配置）

| 字段 | 类型 | 说明 |
|---|---|---|
| key | TEXT PRIMARY KEY | 配置键 |
| value | TEXT | 配置值（JSON 或纯文本） |

配置内容：

| key | value 示例 |
|---|---|
| `target_groups` | `["群A", "群B"]` |
| `keywords` | `[{"text":"优惠","type":"contains"}, ...]` |
| `scan_interval_sec` | `300` |
| `slide_delay_min_ms` | `800` |
| `slide_delay_max_ms` | `2500` |
| `push_url` | `https://example.com/api/clues` |
| `push_enabled` | `true` |

---

## 关键词匹配引擎

支持三种模式，按配置顺序依次匹配：

| 模式 | 实现方式 | 示例 |
|---|---|---|
| `contains` | Kotlin `String.contains()` | "优惠" 匹配 "今日有优惠活动" |
| `regex` | Kotlin `Regex.containsMatchIn()` | `\d{4}.*活动` 匹配 "2024年大活动" |
| `fuzzy` | Levenshtein 编辑距离 | "免费" tolerance=1 匹配 "兔费" |

关键词配置格式（JSON 数组）：

```json
[
  {"text": "优惠", "type": "contains"},
  {"text": "\\d{4}.*活动", "type": "regex"},
  {"text": "免费", "type": "fuzzy", "tolerance": 1}
]
```

---

## 核心扫描流程

```
启动 ScanEngine（Kotlin 协程）
  │
  ▼
加载配置（群列表、关键词、间隔参数）
  │
  ▼
循环（while isRunning）：
  │
  ├── 延时 scan_interval_sec
  │
  ├── 对每个目标群名：
  │     │
  │     ├── WeChatUIA.openWeChat()        // 确保微信窗口存在
  │     │
  │     ├── WeChatUIA.openChat(groupName) // 搜索/点击进入群
  │     │
  │     ├── 重复 N 次（或直到无新内容）：
  │     │     │
  │     │     ├── messages = WeChatUIA.extractMessages()
  │     │     │         （优先 UIA，失败则 OCRFallback）
  │     │     │
  │     │     ├── 对每条消息调用 KeywordMatcher.match(content)
  │     │     │
  │     │     ├── 命中 → 写入 SQLite → 可选触发 PushEngine
  │     │     │
  │     │     └── WeChatUIA.scrollUp() + RandomDelayer.sleep()
  │     │
  │     └── 记录该群最后扫描状态
  │
  └── 一轮结束，回到循环开头
```

---

## 微信 UIA 操作封装（WeChatUIA）

通过 JNA 调用 Windows `UIAutomationClient` COM 接口：

| 方法 | 实现逻辑 |
|---|---|
| `findWeChatWindow()` | UIA 查找窗口标题含 "微信" 的顶层窗口 |
| `openChat(name)` | 点击左侧搜索框 → 输入群名 → 点击搜索结果 |
| `scrollUp()` | 模拟鼠标滚轮向上（或找到聊天区域发送 PageUp） |
| `extractMessages()` | 遍历聊天区域 List/Tree 节点，提取 Name/Value 属性中的文字 |
| `isInChat()` | 判断当前窗口是否存在聊天输入框 UIA 节点 |

---

## OCR 回退（OCRFallback）

当 `extractMessages()` 返回空列表时触发：

1. 获取聊天区域窗口坐标
2. `Robot.createScreenCapture()` 截图该区域
3. Tesseract OCR 识别文字
4. 正则解析出发送人、时间、内容（依赖微信固定布局）

---

## 容错恢复

| 场景 | 检测方式 | 恢复动作 |
|---|---|---|
| 微信窗口被关闭 | `findWeChatWindow()` 返回 null | 调用 `openWeChat()` 重新启动微信 |
| UIA 提取不到文字 | `extractMessages()` 连续空 | 降级 OCRFallback，OCR 也失败则跳过该群 |
| 微信卡死无响应 | 窗口存在但 10s 内 UIA 查询超时 | 杀掉微信进程重新启动 |
| 群聊找不到 | 搜索后无匹配结果 | 记录日志，下一轮重试 |

---

## UI 设计

Compose Desktop 单窗口应用，左侧导航 + 右侧内容区。

```
┌──────────────────────────────────────────────────────┐
│  WeChat Monitor                [最小化到托盘] [退出]  │
├──────────┬───────────────────────────────────────────┤
│ 状态概览  │                                           │
│ 群聊管理  │   右侧内容区（根据左侧导航切换）           │
│ 关键词    │                                           │
│ 检索记录  │                                           │
│ 推送设置  │                                           │
│ 关于      │                                           │
└──────────┴───────────────────────────────────────────┘
```

### 各页面

| 页面 | 内容 |
|---|---|
| **状态概览** | 当前运行状态（运行中/已停止）、本轮扫描进度、已监控群数、今日命中数、最近命中内容预览 |
| **群聊管理** | 输入框 + 添加按钮添加群名，列表展示已添加群，可删除。提供"测试打开"按钮手动验证该群能否进入 |
| **关键词** | 输入框 + 匹配模式下拉框（contains/regex/fuzzy）+ 添加按钮。列表展示所有关键词，可删除。fuzzy 模式显示 tolerance 输入 |
| **检索记录** | 表格展示 clues 数据（群名、发送人、时间、命中内容、关键词），支持按群名/关键词筛选，支持清空，支持导出 CSV |
| **推送设置** | 开关启用/禁用推送，URL 输入框，测试推送按钮，显示最近推送状态/失败原因 |
| **关于** | 应用版本、使用说明（微信窗口不要最小化、不要遮挡）、免责声明 |

### 系统托盘

最小化后驻留托盘，右键菜单：
- 显示主窗口
- 开始扫描 / 停止扫描
- 退出

---

## 错误处理与日志

| 层级 | 处理方式 |
|---|---|
| **UI 层** | 所有异常捕获后弹 Toast/Snackbar，不崩溃 |
| **扫描引擎** | 协程内 `try/catch`，单群失败记录日志并继续下一群，整轮失败重试最多 3 次后暂停并通知用户 |
| **UIA 操作** | 每个 UIA 调用设 10s 超时，超时抛异常由上层捕获 |
| **数据库** | 写入失败重试 1 次，仍失败则记录到内存队列稍后批量写入 |
| **推送** | 失败记录到 `push_failures` 表，提供"重试所有失败"按钮 |

日志输出到 `logs/app.log`，按日期轮转，保留 7 天。

---

## 项目结构

```
weichat-check/
├── build.gradle.kts           # Gradle 构建配置
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── com/weichatcheck/
│       │       ├── Main.kt                 # 程序入口
│       │       ├── ui/
│       │       │   ├── App.kt              # Compose 根组件
│       │       │   ├── NavSidebar.kt       # 左侧导航
│       │       │   ├── DashboardScreen.kt  # 状态概览
│       │       │   ├── GroupsScreen.kt     # 群聊管理
│       │       │   ├── KeywordsScreen.kt   # 关键词
│       │       │   ├── CluesScreen.kt      # 检索记录
│       │       │   ├── PushScreen.kt       # 推送设置
│       │       │   └── components/         # 复用组件
│       │       ├── engine/
│       │       │   ├── ScanEngine.kt       # 扫描调度核心
│       │       │   ├── WeChatUIA.kt        # UIA 操作封装
│       │       │   ├── OCRFallback.kt      # OCR 回退
│       │       │   ├── PushEngine.kt       # HTTP 推送
│       │       │   ├── KeywordMatcher.kt   # 匹配引擎
│       │       │   ├── RandomDelayer.kt    # 随机延时
│       │       │   └── WindowWatcher.kt    # 窗口状态监控
│       │       ├── data/
│       │       │   ├── Database.kt         # SQLite 连接管理
│       │       │   ├── ClueDao.kt          # 线索表操作
│       │       │   └── ConfigDao.kt        # 配置表操作
│       │       └── model/
│       │           ├── Clue.kt             # 线索数据类
│       │           ├── KeywordConfig.kt    # 关键词配置
│       │           └── ScanState.kt        # 扫描状态枚举
│       └── resources/
│           └── icon.ico                    # 托盘图标
├── docs/
│   └── usage.md               # 使用说明
└── README.md
```

---

## 构建输出

`gradle packageMsi` / `packageExe` 生成可安装 `.exe`
