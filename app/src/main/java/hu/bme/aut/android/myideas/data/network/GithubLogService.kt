package hu.bme.aut.android.myideas.data.network

import hu.bme.aut.android.myideas.data.local.GitCommitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GithubLogService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun fetchCommits(
        projectId: String,
        repoSlug: String,
        token: String? = null
    ): List<GitCommitEntity> = withContext(Dispatchers.IO) {
        val cleanSlug = extractRepoSlug(repoSlug) ?: throw IllegalArgumentException("Invalid GitHub repo format: $repoSlug")
        val apiUrl = "https://api.github.com/repos/$cleanSlug/commits?per_page=20"

        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "MyIdeas-Android-App")
            if (!token.isNullOrBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorStream = connection.errorStream ?: connection.inputStream
                val errorText = BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
                throw IllegalStateException("GitHub API error ($responseCode): $errorText")
            }

            val responseText = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            val jsonArray = json.parseToJsonElement(responseText).jsonArray

            jsonArray.mapNotNull { element ->
                try {
                    val obj = element.jsonObject
                    val sha = obj["sha"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val htmlUrl = obj["html_url"]?.jsonPrimitive?.content ?: "https://github.com/$cleanSlug/commit/$sha"

                    val commitObj = obj["commit"]?.jsonObject ?: return@mapNotNull null
                    val fullMessage = commitObj["message"]?.jsonPrimitive?.content ?: "Commit"

                    val messageLines = fullMessage.split("\n", limit = 2)
                    val messageTitle = messageLines.firstOrNull()?.trim() ?: "Commit"
                    val messageBody = messageLines.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }

                    val authorObj = commitObj["author"]?.jsonObject
                    val authorName = authorObj?.get("name")?.jsonPrimitive?.content ?: "Developer"
                    val timestamp = authorObj?.get("date")?.jsonPrimitive?.content ?: ""

                    GitCommitEntity(
                        sha = sha,
                        parentProjectId = projectId,
                        messageTitle = messageTitle,
                        messageBody = messageBody,
                        authorName = authorName,
                        timestamp = timestamp,
                        webUrl = htmlUrl
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        fun extractRepoSlug(input: String): String? {
            var trimmed = input.trim()
            if (trimmed.isBlank()) return null

            // Clean trailing .git if present
            if (trimmed.endsWith(".git")) {
                trimmed = trimmed.removeSuffix(".git")
            }

            // Handles https://github.com/owner/repo or http://github.com/owner/repo
            if (trimmed.contains("github.com/")) {
                val afterGithub = trimmed.substringAfter("github.com/").trim('/')
                val parts = afterGithub.split('/')
                if (parts.size >= 2) {
                    return "${parts[0]}/${parts[1]}"
                }
            }

            // Handles git@github.com:owner/repo
            if (trimmed.contains("github.com:")) {
                val afterGithub = trimmed.substringAfter("github.com:").trim('/')
                val parts = afterGithub.split('/')
                if (parts.size >= 2) {
                    return "${parts[0]}/${parts[1]}"
                }
            }

            // Handles owner/repo
            val parts = trimmed.split('/')
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                return "${parts[0]}/${parts[1]}"
            }

            return null
        }
    }
}
