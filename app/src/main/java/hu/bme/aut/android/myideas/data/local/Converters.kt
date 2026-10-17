package hu.bme.aut.android.myideas.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return try {
            json.encodeToString(value ?: emptyList())
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<String>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
