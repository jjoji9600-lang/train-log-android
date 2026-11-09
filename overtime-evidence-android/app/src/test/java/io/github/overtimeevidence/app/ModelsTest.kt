package io.github.overtimeevidence.app

import io.github.overtimeevidence.app.data.Evidence
import io.github.overtimeevidence.app.data.EvidenceKind
import io.github.overtimeevidence.app.data.ProofElement
import io.github.overtimeevidence.app.data.NextAction
import io.github.overtimeevidence.app.data.sessionGuide
import io.github.overtimeevidence.app.data.proofCoverageCount
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

    @Test fun guideAlwaysSuggestsOneCoherentNextAction() {
        val active = WorkSession("s", "夜间故障排查", 1000L)
        val empty = emptyList<Evidence>()
        assertEquals(NextAction.ADD_FIRST, sessionGuide(active, empty).action)
        assertEquals(0, proofCoverageCount(empty))

        val arrangement = Evidence("a", "s", "arranged.png", "image/png",
            EvidenceKind.ARRANGEMENT, "负责人指令", "hash", 5L, 1002L, "picker",
            proofElements = setOf(ProofElement.ARRANGED))
        assertEquals(NextAction.SUPPLEMENT, sessionGuide(active, listOf(arrangement)).action)
        assertEquals(ProofElement.FACT, sessionGuide(active, listOf(arrangement)).missingElement)
        assertEquals(1, proofCoverageCount(listOf(arrangement)))

        val all = arrangement.copy(proofElements = ProofElement.entries.toSet())
        assertEquals(NextAction.END, sessionGuide(active, listOf(all)).action)
        assertEquals(NextAction.EXPORT, sessionGuide(active.copy(endedAt = 2000L), listOf(all)).action)
        assertEquals(3, proofCoverageCount(listOf(all)))
    }

    @Test fun importedDraftExplainsWhyTimesNeedChecking() {
        val draft = WorkSession("draft", "待整理：导入的加班材料", 1000L,
            endedAt = 1000L, draft = true)
        val hint = sessionGuide(draft, emptyList())
        assertEquals(NextAction.REVIEW_EVENT, hint.action)
        assertTrue(hint.reason.contains("不是实际加班时间"))
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
