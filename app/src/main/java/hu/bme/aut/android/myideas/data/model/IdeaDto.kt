package hu.bme.aut.android.myideas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdeaDto(
    @SerialName("schema_version") val schemaVersion: Int? = 1,
    val title: String = "",
    val summary: String = "",
    val category: String = "EGYÉB",
    val tags: List<String> = emptyList(),
    @SerialName("first_step") val firstStep: String? = null,
    @SerialName("key_points") val keyPoints: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null
)
