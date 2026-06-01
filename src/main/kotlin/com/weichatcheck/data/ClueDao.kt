package com.weichatcheck.data

import com.weichatcheck.model.Clue
import com.weichatcheck.model.MatchType

class ClueDao(private val database: Database) {
    fun insert(clue: Clue): Long {
        val sql = """
            INSERT INTO clues (group_name, sender_name, send_time, hit_content, hit_keyword, match_type, created_at, pushed)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        database.connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, clue.groupName)
            stmt.setString(2, clue.senderName)
            stmt.setString(3, clue.sendTime)
            stmt.setString(4, clue.hitContent)
            stmt.setString(5, clue.hitKeyword)
            stmt.setString(6, clue.matchType.name.lowercase())
            stmt.setLong(7, clue.createdAt)
            stmt.setInt(8, if (clue.pushed) 1 else 0)
            stmt.executeUpdate()
        }
        return database.connection.createStatement().use { stmt ->
            stmt.executeQuery("SELECT last_insert_rowid()").use { rs ->
                rs.getLong(1)
            }
        }
    }

    fun getAll(): List<Clue> = query("SELECT * FROM clues ORDER BY created_at DESC")

    fun getByKeyword(keyword: String): List<Clue> {
        return database.connection.prepareStatement(
            "SELECT * FROM clues WHERE hit_keyword = ? ORDER BY created_at DESC"
        ).use { stmt ->
            stmt.setString(1, keyword)
            stmt.executeQuery().use { rs ->
                generateSequence { if (rs.next()) rs.toClue() else null }.toList()
            }
        }
    }

    fun getByGroup(groupName: String): List<Clue> {
        return database.connection.prepareStatement(
            "SELECT * FROM clues WHERE group_name = ? ORDER BY created_at DESC"
        ).use { stmt ->
            stmt.setString(1, groupName)
            stmt.executeQuery().use { rs ->
                generateSequence { if (rs.next()) rs.toClue() else null }.toList()
            }
        }
    }

    fun getUnpushed(): List<Clue> = query("SELECT * FROM clues WHERE pushed = 0 ORDER BY created_at DESC")

    fun markPushed(id: Long) {
        database.connection.prepareStatement("UPDATE clues SET pushed = 1 WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate()
        }
    }

    fun clearAll() {
        database.connection.createStatement().use { it.executeUpdate("DELETE FROM clues") }
    }

    fun getCount(): Int {
        return database.connection.createStatement().use { stmt ->
            stmt.executeQuery("SELECT COUNT(*) FROM clues").use { rs ->
                if (rs.next()) rs.getInt(1) else 0
            }
        }
    }

    private fun query(sql: String): List<Clue> {
        return database.connection.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                generateSequence { if (rs.next()) rs.toClue() else null }.toList()
            }
        }
    }

    private fun java.sql.ResultSet.toClue(): Clue = Clue(
        id = getLong("id"),
        groupName = getString("group_name"),
        senderName = getString("sender_name"),
        sendTime = getString("send_time"),
        hitContent = getString("hit_content"),
        hitKeyword = getString("hit_keyword"),
        matchType = MatchType.valueOf(getString("match_type").uppercase()),
        createdAt = getLong("created_at"),
        pushed = getInt("pushed") == 1
    )
}
