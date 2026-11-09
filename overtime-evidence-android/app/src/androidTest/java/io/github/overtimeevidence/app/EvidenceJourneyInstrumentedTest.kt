package io.github.overtimeevidence.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.overtimeevidence.app.data.EvidenceKind
import io.github.overtimeevidence.app.data.EvidenceRepository
import io.github.overtimeevidence.app.data.ProofElement
import io.github.overtimeevidence.app.data.missingProofElements
import io.github.overtimeevidence.app.export.EvidenceExporter
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile

/**
 * Runs on an Android emulator: Keystore, ContentResolver, FileProvider,
 * encrypted file storage and PDF/ZIP export are real, not mocked.
 */
@RunWith(AndroidJUnit4::class)
class EvidenceJourneyInstrumentedTest {
    private val app: Context get() = ApplicationProvider.getApplicationContext()

    private fun screenshot(name: String): Pair<File, ByteArray> {
        val directory = File(app.cacheDir, "captures").apply { mkdirs() }
        val file = File(directory, name)
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(20, 98, 170))
        file.outputStream().use { out ->
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out))
        }
        bitmap.recycle()
        return file to file.readBytes()
    }

    private fun contentUri(file: File) =
        FileProvider.getUriForFile(app, app.packageName + ".fileprovider", file)

    private fun sha(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it) }

    @Test fun startCaptureTagRemindSupplementEndExportAndReopen() = runBlocking {
        val repo = EvidenceRepository(app)
        // The emulator test app has isolated data, never actual user evidence.
        repo.state.value.sessions.toList().forEach { repo.deleteSession(it.id) }

        val session = repo.startSession("夜间排查线上故障")
        assertEquals(3, missingProofElements(emptyList()).size)
        assertEquals(null, session.endedAt)

        val (photo1, original1) = screenshot("arrangement.png")
        assertEquals(1, repo.importUris(
            listOf(contentUri(photo1)), session.id, EvidenceKind.ARRANGEMENT, "camera",
            setOf(ProofElement.ARRANGED), "21:03 主管在工作群安排紧急故障排查",
        ))
        var rows = repo.state.value.evidence.filter { it.sessionId == session.id }
        assertEquals(1, rows.size)
        assertArrayEquals(original1, repo.openEvidence(rows.single()).use { it.readBytes() })
        assertEquals(sha(original1), rows.single().sha256)
        assertEquals(listOf(ProofElement.FACT, ProofElement.TIME_PAY), missingProofElements(rows))

        val (photo2, _) = screenshot("working.png")
        repo.importUris(
            listOf(contentUri(photo2)), session.id, EvidenceKind.PROCESS, "picker",
            setOf(ProofElement.FACT), "21:12 至 22:08 工单系统故障处理记录",
        )
        rows = repo.state.value.evidence.filter { it.sessionId == session.id }
        assertEquals(listOf(ProofElement.TIME_PAY), missingProofElements(rows))

        repo.endSession(session.id)
        val endTime = repo.state.value.sessions.single { it.id == session.id }.endedAt
        assertNotNull(endTime)

        // Closed events still allow supplements; the original end time is unchanged.
        val (photo3, _) = screenshot("attendance.png")
        repo.importUris(
            listOf(contentUri(photo3)), session.id, EvidenceKind.ATTENDANCE, "share",
            setOf(ProofElement.TIME_PAY), "上下班考勤和工资条，工资计算依据",
        )
        rows = repo.state.value.evidence.filter { it.sessionId == session.id }
        assertTrue(missingProofElements(rows).isEmpty())
        assertEquals(endTime, repo.state.value.sessions.single { it.id == session.id }.endedAt)

        val reopened = EvidenceRepository(app)
        val reloadedRows = reopened.state.value.evidence.filter { it.sessionId == session.id }
        assertEquals(3, reloadedRows.size)
        assertArrayEquals(original1, reopened.openEvidence(
            reloadedRows.single { it.name == "arrangement.png" }
        ).use { it.readBytes() })

        val zipFile = File(app.cacheDir, "captures/test_evidence_export.zip")
        val exported = EvidenceExporter(app, reopened).export(
            session.id, reopened.state.value, contentUri(zipFile),
        )
        assertEquals(3, exported)
        ZipFile(zipFile).use { zip ->
            assertNotNull(zip.getEntry("report.pdf"))
            assertNotNull(zip.getEntry("manifest.json"))
            assertNotNull(zip.getEntry("SHA256SUMS.txt"))
            val names = mutableListOf<String>()
            val enumeration = zip.entries()
            while (enumeration.hasMoreElements()) names.add(enumeration.nextElement().name)
            assertEquals(3, names.count { it.startsWith("originals/") })

            val manifest = JSONObject(
                zip.getInputStream(zip.getEntry("manifest.json")).bufferedReader().use { it.readText() }
            )
            assertEquals(0, manifest.getJSONArray("missingProofElements").length())
            val exports = manifest.getJSONArray("evidence")
            assertEquals(3, exports.length())
            for (i in 0 until exports.length()) {
                val row = exports.getJSONObject(i)
                val data = zip.getInputStream(zip.getEntry(row.getString("zipPath"))).use { it.readBytes() }
                assertEquals(row.getString("sha256"), sha(data))
                assertTrue(row.getJSONArray("proofElements").length() >= 1)
                assertTrue(row.getString("note").isNotBlank())
            }
            val pdfHeader = zip.getInputStream(zip.getEntry("report.pdf")).use {
                ByteArray(4).also { buffer -> it.read(buffer) }
            }
            assertArrayEquals("%PDF".toByteArray(Charsets.US_ASCII), pdfHeader)
        }

        val attendanceRow = rows.single { ProofElement.TIME_PAY in it.proofElements }
        repo.deleteEvidence(attendanceRow.id)
        assertEquals(
            listOf(ProofElement.TIME_PAY),
            missingProofElements(repo.state.value.evidence.filter { it.sessionId == session.id }),
        )
        assertFalse(File(app.filesDir, "overtime-vault/evidence/" + attendanceRow.id + ".ev").exists())
    }
}
