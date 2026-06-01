package com.weichatcheck.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConfigDaoTest {
    @Test
    fun `sets and gets string value`() {
        val db = Database.createTemp()
        val dao = ConfigDao(db)
        dao.set("test_key", "test_value")
        assertEquals("test_value", dao.get("test_key"))
    }

    @Test
    fun `returns default for missing key`() {
        val db = Database.createTemp()
        val dao = ConfigDao(db)
        assertNull(dao.get("missing"))
        assertEquals("default", dao.get("missing", "default"))
    }

    @Test
    fun `updates existing key`() {
        val db = Database.createTemp()
        val dao = ConfigDao(db)
        dao.set("key", "old")
        dao.set("key", "new")
        assertEquals("new", dao.get("key"))
    }
}
