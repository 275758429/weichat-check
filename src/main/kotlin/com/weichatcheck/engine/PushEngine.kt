package com.weichatcheck.engine

import com.weichatcheck.model.Clue
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PushPayload(
    val groupName: String,
    val senderName: String,
    val sendTime: String,
    val hitContent: String,
    val hitKeyword: String,
    val matchType: String,
    val createdAt: Long
)

class PushEngine(private var endpoint: String = "") {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    var enabled: Boolean = false

    fun updateEndpoint(url: String) {
        endpoint = url
    }

    suspend fun push(clue: Clue): Result<Unit> {
        if (!enabled || endpoint.isBlank()) {
            return Result.failure(IllegalStateException("Push not enabled or endpoint not set"))
        }
        return try {
            val payload = PushPayload(
                groupName = clue.groupName,
                senderName = clue.senderName,
                sendTime = clue.sendTime,
                hitContent = clue.hitContent,
                hitKeyword = clue.hitKeyword,
                matchType = clue.matchType.name.lowercase(),
                createdAt = clue.createdAt
            )
            val response = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(): Result<String> {
        if (endpoint.isBlank()) return Result.failure(IllegalStateException("Endpoint not set"))
        return try {
            val response = client.get(endpoint)
            if (response.status.isSuccess() || response.status.value == 405) {
                Result.success("OK")
            } else {
                Result.failure(Exception("HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
