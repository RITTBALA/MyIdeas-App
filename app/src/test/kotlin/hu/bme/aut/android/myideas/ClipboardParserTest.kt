package hu.bme.aut.android.myideas

import hu.bme.aut.android.myideas.data.network.GithubLogService
import hu.bme.aut.android.myideas.data.parser.ClipboardParser
import hu.bme.aut.android.myideas.data.parser.ParseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardParserTest {

    private val parser = ClipboardParser()

    @Test
    fun extractRepoSlug_handlesVariousGithubFormats() {
        assertEquals("owner/repo", GithubLogService.extractRepoSlug("https://github.com/owner/repo"))
        assertEquals("owner/repo", GithubLogService.extractRepoSlug("https://github.com/owner/repo.git"))
        assertEquals("owner/repo", GithubLogService.extractRepoSlug("git@github.com:owner/repo.git"))
        assertEquals("owner/repo", GithubLogService.extractRepoSlug("owner/repo"))
    }

    @Test
    fun parse_validIdeaJsonSchemaV1_returnsIdeaSuccess() {
        val markdownJson = """
            Itt van az ötleted JSON formátumban:
            ```json
            {
              "schema_version": 1,
              "title": "Smart Home Hub",
              "summary": "Raspberry Pi alapú automatizálási központ",
              "category": "HARDWARE",
              "tags": ["iot", "raspberry"],
              "first_step": "Alkatrészek megrendelése",
              "key_points": ["Zigbee csatlakozás", "Webes felület"],
              "created_at": "2025-02-21T10:00:00Z"
            }
            ```
            Remélem ez segít!
        """.trimIndent()

        val result = parser.parse(markdownJson)

        assertTrue(result is ParseResult.IdeaSuccess)
        val success = result as ParseResult.IdeaSuccess
        assertEquals("Smart Home Hub", success.dto.title)
        assertEquals("HARDWARE", success.entity.category)
        assertEquals("Alkatrészek megrendelése", success.entity.firstStep)
        assertEquals(2, success.entity.tags.size)
        assertEquals(2, success.entity.keyPoints.size)
    }

    @Test
    fun parse_validProjectJsonSchemaV2WithDescriptionAndRepo_returnsProjectSuccess() {
        val projectJson = """
            {
              "schema_version": 2,
              "project_id": "esp32-ble-environmental-sensor-hub",
              "project_title": "ESP32 Ultra-Low-Power BLE Environmental Node",
              "project_description": "An ultra-low-power environmental telemetry node...",
              "github_repo": "owner/esp32-ble-environmental-sensor-hub",
              "category": "HARDWARE",
              "export_type": "INIT",
              "current_head": {
                "next_micro_step": "Draft PlatformIO project config",
                "status": "ACTIVE"
              },
              "nodes": [
                {
                  "node_id": "a1f0",
                  "type": "DECISION",
                  "tag": "HARDW",
                  "title": "Select ESP32-C3 over Classic ESP32-WROOM",
                  "detail": "Opted for the RISC-V based ESP32-C3-MINI-1 module...",
                  "timestamp": "2026-10-17T21:19:00Z"
                }
              ],
              "commits": [
                {
                  "sha": "a1b2c3d",
                  "message": "Initial commit",
                  "author": "Developer",
                  "timestamp": "2026-10-17T22:00:00Z"
                }
              ]
            }
        """.trimIndent()

        val result = parser.parse(projectJson)

        assertTrue(result is ParseResult.ProjectSuccess)
        val success = result as ParseResult.ProjectSuccess
        assertEquals("esp32-ble-environmental-sensor-hub", success.dto.projectId)
        assertEquals("ESP32 Ultra-Low-Power BLE Environmental Node", success.dto.projectTitle)
        assertNotNull(success.dto.projectDescription)
        assertEquals("owner/esp32-ble-environmental-sensor-hub", success.dto.githubRepo)
        assertEquals("Draft PlatformIO project config", success.dto.currentHead?.nextMicroStep)
        assertEquals(1, success.dto.nodes.size)
        assertEquals(1, success.dto.commits.size)
        assertEquals("a1b2c3d", success.dto.commits[0].sha)
    }

    @Test
    fun parse_emptyInput_returnsError() {
        val result = parser.parse("")
        assertTrue(result is ParseResult.Error)
    }

    @Test
    fun parse_invalidJson_returnsError() {
        val result = parser.parse("{ invalid_json }")
        assertTrue(result is ParseResult.Error)
    }
}
