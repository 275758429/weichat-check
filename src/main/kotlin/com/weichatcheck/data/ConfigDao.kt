package com.weichatcheck.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ConfigDao(private val database: Database) {
    fun set(key: String, value: String) {
        database.connection.prepareStatement(
            "INSERT OR REPLACE INTO config (key, value) VALUES (?, ?)"
        ).use { stmt ->
            stmt.setString(1, key)
            stmt.setString(2, value)
            stmt.executeUpdate()
        }
    }

    fun get(key: String, default: String? = null): String? {
        return database.connection.prepareStatement(
            "SELECT value FROM config WHERE key = ?"
        ).use { stmt ->
            stmt.setString(1, key)
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.getString("value") else default
            }
        }
    }

    fun getString(key: String, default: String = ""): String = get(key, default) ?: default
    fun getInt(key: String, default: Int = 0): Int = get(key)?.toIntOrNull() ?: default
    fun getBoolean(key: String, default: Boolean = false): Boolean =
        get(key)?.toBooleanStrictOrNull() ?: default

    inline fun <reified T> getJson(key: String, default: T): T {
        return get(key)?.let { Json.decodeFromString<T>(it) } ?: default
    }

    inline fun <reified T> setJson(key: String, value: T) {
        set(key, Json.encodeToString(value))
    }

    fun remove(key: String) {
        database.connection.prepareStatement("DELETE FROM config WHERE key = ?").use { stmt ->
            stmt.setString(1, key)
            stmt.executeUpdate()
        }
    }
}
