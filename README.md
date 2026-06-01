# WeChat Monitor

基于 Windows UI Automation 的微信群聊关键词监控工具。

## 功能

- 自动监控指定微信群聊
- 多模式关键词匹配：包含、正则、模糊（编辑距离）
- 命中线索本地 SQLite 存储
- HTTP POST 推送至自定义接口
- 导出 CSV

## 技术栈

- Kotlin + Compose Desktop
- JNA (Windows UIA COM)
- SQLite + JDBC
- Ktor Client
- Tesseract OCR (降级方案)

## 运行要求

- Windows 10/11
- 微信 PC 客户端已安装并登录
- Java 17+ (运行时)

## 安装

下载 `WeChatMonitor-1.0.0.exe` 并安装。

## 使用

1. 启动 WeChat Monitor
2. 在"群聊管理"中添加要监控的微信群名称
3. 在"关键词"中添加监控关键词
4. 可选：在"推送设置"中配置 HTTP 推送地址
5. 点击"开始扫描"

## 注意事项

- 微信窗口在扫描期间应保持可见，不要被其他窗口遮挡
- 首次使用建议先"测试打开"验证群聊能否正常进入
- OCR 降级需要下载中文语言包放置到 `tessdata/chi_sim.traineddata`

## 构建

```bash
./gradlew packageExe
```

输出在 `build/compose/binaries/main/exe/`。
