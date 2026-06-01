package com.weichatcheck.data

import com.weichatcheck.model.Clue
import com.weichatcheck.model.MatchType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClueDaoTest {
    @Test
    fun `inserts and retrieves clue`() {
        val db = Database.createTemp()
        val dao = ClueDao(db)
        val clue = Clue(
            groupName = "测试群",
            senderName = "张三",
            sendTime = "10:30",
            hitContent = "有优惠活动",
            hitKeyword = "优惠",
            matchType = MatchType.CONTAINS
        )
        val id = dao.insert(clue)
        assertTrue(id > 0)

        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("测试群", all[0].groupName)
        assertEquals("有优惠活动", all[0].hitContent)
    }

    @Test
    fun `filters by keyword`() {
        val db = Database.createTemp()
        val dao = ClueDao(db)
        dao.insert(Clue(groupName = "群A", senderName = "a", sendTime = "1", hitContent = "x", hitKeyword = "优惠", matchType = MatchType.CONTAINS))
        dao.insert(Clue(groupName = "群B", senderName = "b", sendTime = "2", hitContent = "y", hitKeyword = "免费", matchType = MatchType.CONTAINS))

        val filtered = dao.getByKeyword("优惠")
        assertEquals(1, filtered.size)
        assertEquals("群A", filtered[0].groupName)
    }

    @Test
    fun `clears all clues`() {
        val db = Database.createTemp()
        val dao = ClueDao(db)
        dao.insert(Clue(groupName = "群", senderName = "a", sendTime = "1", hitContent = "x", hitKeyword = "k", matchType = MatchType.CONTAINS))
        dao.clearAll()
        assertEquals(0, dao.getCount())
    }
}
