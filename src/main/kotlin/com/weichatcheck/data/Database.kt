package com.weichatcheck.data

import java.sql.Connection
import java.sql.DriverManager
import java.io.File

class Database(dbPath: String = "weichat_check.db") {
    val connection: Connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

    init {
        connection.createStatement().use { stmt ->
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS clues (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    group_name TEXT NOT NULL,
                    sender_name TEXT NOT NULL,
                    send_time TEXT NOT NULL,
                    hit_content TEXT NOT NULL,
                    hit_keyword TEXT NOT NULL,
                    match_type TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    pushed INTEGER DEFAULT 0
                )
                """.trimIndent()
            )
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS config (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    companion object {
        fun createTemp(): Database {
            val file = File.createTempFile("test", ".db")
            file.deleteOnExit()
            return Database(file.absolutePath)
        }
    }
}
