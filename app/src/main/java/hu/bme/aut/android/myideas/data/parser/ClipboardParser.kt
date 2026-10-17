package hu.bme.aut.android.myideas.data.parser

import hu.bme.aut.android.myideas.data.local.IdeaEntity
import hu.bme.aut.android.myideas.data.model.IdeaDto
import hu.bme.aut.android.myideas.data.model.ProjectDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface ParseResult {
    data class IdeaSuccess(val dto: IdeaDto, val entity: IdeaEntity) : ParseResult
    data class ProjectSuccess(val dto: ProjectDto) : ParseResult
    data class Error(val message: String) : ParseResult
}

class ClipboardParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parse(rawText: String?): ParseResult {
        if (rawText.isNullOrBlank()) {
            return ParseResult.Error("Clipboard is empty. Copy an idea or project in JSON format!")
        }

        val cleanedText = cleanJsonText(rawText)
        if (cleanedText.isBlank()) {
            return ParseResult.Error("No valid JSON content found on the clipboard.")
        }

        return try {
            val jsonElement = json.parseToJsonElement(cleanedText)
            val jsonObject = jsonElement.jsonObject

            val schemaVersion = jsonObject["schema_version"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1

            if (schemaVersion == 2 || jsonObject.containsKey("nodes") || jsonObject.containsKey("project_id")) {
                val projectDto = json.decodeFromString<ProjectDto>(cleanedText)
                if (projectDto.projectTitle.isBlank() && projectDto.projectId.isBlank()) {
                    return ParseResult.Error("Project JSON did not contain a title or project ID.")
                }
                ParseResult.ProjectSuccess(projectDto)
            } else {
                val dto = json.decodeFromString<IdeaDto>(cleanedText)
                if (dto.title.isBlank() && dto.summary.isBlank()) {
                    return ParseResult.Error("JSON structure did not contain a title or summary.")
                }
                val entity = dtoToEntity(dto)
                ParseResult.IdeaSuccess(dto, entity)
            }
        } catch (e: Exception) {
            ParseResult.Error("Invalid JSON format! Please check the copied text. (${e.localizedMessage ?: "Unknown error"})")
        }
    }

    fun dtoToEntity(dto: IdeaDto): IdeaEntity {
        val categoryNormalized = dto.category.trim().uppercase().ifBlank { "OTHER" }
        return IdeaEntity(
            title = dto.title.ifBlank { "Untitled Idea" },
            summary = dto.summary.ifBlank { "No details provided." },
            category = categoryNormalized,
            tags = dto.tags.filter { it.isNotBlank() },
            firstStep = dto.firstStep?.takeIf { it.isNotBlank() },
            keyPoints = dto.keyPoints.filter { it.isNotBlank() },
            isFirstStepDone = false,
            savedAt = System.currentTimeMillis()
        )
    }

    private fun cleanJsonText(raw: String): String {
        var trimmed = raw.trim()

        if (trimmed.startsWith("```")) {
            val firstNewLine = trimmed.indexOf('\n')
            if (firstNewLine != -1) {
                trimmed = trimmed.substring(firstNewLine + 1)
            } else {
                trimmed = trimmed.removePrefix("```json").removePrefix("```")
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.lastIndexOf("```"))
            }
            trimmed = trimmed.trim()
        }

        val startBrace = trimmed.indexOf('{')
        val endBrace = trimmed.lastIndexOf('}')

        if (startBrace != -1 && endBrace != -1 && endBrace > startBrace) {
            return trimmed.substring(startBrace, endBrace + 1).trim()
        }

        return trimmed
    }
}
