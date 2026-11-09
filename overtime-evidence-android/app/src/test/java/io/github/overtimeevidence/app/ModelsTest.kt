package io.github.overtimeevidence.app

import io.github.overtimeevidence.app.data.Evidence
import io.github.overtimeevidence.app.data.EvidenceKind
import io.github.overtimeevidence.app.data.ProofElement
import io.github.overtimeevidence.app.data.missingProofElements
import io.github.overtimeevidence.app.data.VaultSnapshot
import io.github.overtimeevidence.app.data.WorkSession
import io.github.overtimeevidence.app.data.asJson
import io.github.overtimeevidence.app.data.asSnapshot
import io.github.overtimeevidence.app.util.elapsedMinutes
import io.github.overtimeevidence.app.util.safeFileName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {
    @Test fun snapshotRoundTripPreservesMetadata() {
        val session = WorkSession("s1", "夜间排障", 1000L, endedAt = 61000L, note = "工单 #123", createdAt = 1000L)
        val evidence = Evidence("e1", "s1", "聊天截图.png", "image/png", EvidenceKind.ARRANGEMENT,
            "经理安排", "abc123", 123L, 2000L, "share", proofElements = setOf(ProofElement.ARRANGED, ProofElement.FACT))
        val source = VaultSnapshot(listOf(session), listOf(evidence))
        assertEquals(source, source.asJson().asSnapshot())
    }

    @Test fun missingTagsReflectOnlySavedAssignments() {
        val onlyArrangement = Evidence("e1", "s1", "聊天截图.png", "image/png",
            EvidenceKind.ARRANGEMENT, "主管工作群消息", "abc", 20, 1000, "picker",
            proofElements = setOf(ProofElement.ARRANGED))
        assertEquals(listOf(ProofElement.FACT, ProofElement.TIME_PAY),
            missingProofElements(listOf(onlyArrangement)))
        assertEquals(ProofElement.entries.toList(), missingProofElements(emptyList()))
    }

    @Test fun oldManifestWithoutThreeElementTagsStillLoads() {
        val oldJson = org.json.JSONObject()
            .put("schemaVersion", 1)
            .put("sessions", org.json.JSONArray())
            .put("evidence", org.json.JSONArray().put(org.json.JSONObject()
                .put("id", "e1").put("sessionId", "s1").put("name", "old.png")
                .put("sha256", "abc").put("bytes", 3).put("importedAt", 1000)))
        val legacy = oldJson.asSnapshot().evidence.single()
        assertTrue(legacy.proofElements.isEmpty())
        assertEquals(3, missingProofElements(listOf(legacy)).size)
    }

    @Test fun durationNeverNegative() {
        assertEquals(2L, elapsedMinutes(1000L, 121000L))
        assertEquals(0L, elapsedMinutes(121000L, 1000L))
    }

    @Test fun exportFilenameRemovesPathCharacters() {
        assertFalse(safeFileName("../a/b\\c.txt").contains('/'))
        assertFalse(safeFileName("../a/b\\c.txt").contains('\\'))
        assertTrue(safeFileName("打卡.png").endsWith(".png"))
    }
}
