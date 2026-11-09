package io.github.overtimeevidence.app.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import io.github.overtimeevidence.app.data.Evidence
import io.github.overtimeevidence.app.data.EvidenceKind
import io.github.overtimeevidence.app.data.EvidenceRepository
import io.github.overtimeevidence.app.data.ProofElement
import io.github.overtimeevidence.app.data.missingProofElements
import io.github.overtimeevidence.app.data.VaultSnapshot
import io.github.overtimeevidence.app.data.WorkSession
import io.github.overtimeevidence.app.data.asJson
import io.github.overtimeevidence.app.util.elapsedMinutes
import io.github.overtimeevidence.app.util.formatDateTime
import io.github.overtimeevidence.app.util.formatDuration
import io.github.overtimeevidence.app.util.safeFileName
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class EvidenceExporter(
    private val context: Context,
    private val repository: EvidenceRepository,
) {
    fun export(sessionId: String, snapshot: VaultSnapshot, destination: Uri): Int {
        val session = snapshot.sessions.firstOrNull { it.id == sessionId }
            ?: error("找不到加班记录")
        val evidence = snapshot.evidence.filter { it.sessionId == sessionId }
            .sortedBy { it.importedAt }

        // Fail before creating the user-facing output when any stored file fails verification.
        evidence.forEach { row ->
            val digest = MessageDigest.getInstance("SHA-256")
            repository.openEvidence(row).use { stream ->
                val buffer = ByteArray(32 * 1024)
                while (true) {
                    val n = stream.read(buffer)
                    if (n < 0) break
                    digest.update(buffer, 0, n)
                }
            }
            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            if (hash != row.sha256) throw IOException("文件校验失败：${row.name}")
        }

        val pdf = makePdf(session, evidence)
        val files = evidence.mapIndexed { i, row ->
            row to "originals/${(i + 1).toString().padStart(3, '0')}_${row.id.take(8)}_${safeFileName(row.name)}"
        }
        val manifest = JSONObject().apply {
            put("schemaVersion", 1)
            put("exportedAtDeviceTime", System.currentTimeMillis())
            put("session", session.asJson())
            put("missingProofElements", JSONArray(missingProofElements(evidence).map { it.name }))
            put("evidence", JSONArray().also { a ->
                files.forEach { (row, path) ->
                    a.put(row.asJson().apply { put("zipPath", path) })
                }
            })
            put("note", "文件哈希用于识别保存后的修改；设备时间、导入前截图内容及工作事实需其他证据佐证。")
        }
        val readme = """
            加班留证 - 导出材料

            report.pdf：按事件生成的证据目录（仅作整理，不代表仲裁机关认定）。
            manifest.json：结构化记录，包含SHA-256、导入时间、原件来源、分类及备注。
            originals/：上传时所读取的原始文件字节，导出时解密，不进行二次压缩/编辑。
            SHA256SUMS.txt：各原件的校验值。可使用 sha256sum 或其他工具核对。

            重要说明：
            1. 导入时间来自手机系统时钟，并非司法认可的可信时间戳。
            2. 图片、聊天截图可能缺少上下文。请另行保存原始聊天、考勤、工单等证据。
            2a. 三要素标签、具体内容说明均为用户自行标注，缺项提醒只检查标签，不能证明证据充分或必然被采信。
            3. 本ZIP未设置密码。请妥善保管，分享前审查客户信息、个人信息和商业秘密。
            4. App仅辅助记录和整理，不判断加班费请求是否成立。
        """.trimIndent()

        val stream = context.contentResolver.openOutputStream(destination, "w")
            ?: throw IOException("无法创建导出文件")
        stream.use { out ->
            ZipOutputStream(out.buffered()).use { zip ->
                fun entry(name: String, bytes: ByteArray) {
                    zip.putNextEntry(ZipEntry(name))
                    zip.write(bytes)
                    zip.closeEntry()
                }
                entry("report.pdf", pdf)
                entry("README.txt", readme.toByteArray(Charsets.UTF_8))
                entry("manifest.json", manifest.toString(2).toByteArray(Charsets.UTF_8))
                entry("SHA256SUMS.txt", files.joinToString("\n", postfix = "\n") { (row, path) ->
                    "${row.sha256}  $path"
                }.toByteArray(Charsets.UTF_8))
                files.forEach { (row, path) ->
                    zip.putNextEntry(ZipEntry(path))
                    repository.openEvidence(row).use { it.copyTo(zip, 32 * 1024) }
                    zip.closeEntry()
                }
            }
        }
        return evidence.size
    }

    private fun makePdf(session: WorkSession, list: List<Evidence>): ByteArray {
        val document = PdfDocument()
        val layout = PdfLines(document)
        try {
            layout.line("加班证据目录 / Overtime Evidence", 17f, bold = true)
            layout.line("说明：由用户自行记录，仅供证据整理，不构成事实认定。", 10f)
            layout.space()
            layout.line("任务：${session.task}", 12f, bold = true)
            layout.line("开始：${formatDateTime(session.startedAt)}")
            layout.line("结束：${session.endedAt?.let(::formatDateTime) ?: "进行中"}")
            session.endedAt?.let {
                layout.line("跨度：${formatDuration(elapsedMinutes(session.startedAt, it))}（未自动扣除休息，不等于法定加班时长）")
            }
            if (session.draft) layout.line("标记：导入材料时创建的待整理记录，起止时间不是经核实的工作时段。")
            if (session.note.isNotBlank()) layout.line("说明：${session.note}")
            layout.space()
            layout.line("材料清单（共 ${list.size} 件）", 13f, bold = true)
            list.forEachIndexed { i, item ->
                layout.line("${i + 1}. ${item.name} [${item.category.label}]", 11f, bold = true)
                layout.line("    导入时间：${formatDateTime(item.importedAt)}；来源：${item.source}；大小：${item.bytes} bytes", 9.5f)
                layout.line("    SHA-256: ${item.sha256}", 8.5f)
                layout.line("    三要素：${if (item.proofElements.isEmpty()) "未标注" else item.proofElements.joinToString("、") { it.label }}", 9.5f)
                if (item.note.isNotBlank()) layout.line("    内容说明：${item.note}", 9.5f)
            }
            layout.space()
            layout.line("三要素覆盖提醒（只按用户勾选的标签统计）", 11f, bold = true)
            ProofElement.entries.forEach { element ->
                val count = list.count { element in it.proofElements }
                layout.line("${element.label}：${count} 件已标注")
                if (count == 0) layout.line("    建议：${element.suggestion}", 9f)
            }
            layout.line("提示：已标注不等于事实成立，仍需核实内容、上下文及合法性。", 9f)
            layout.space()
            layout.line("提示：请保留完整上下文、合法取得的原始记录；导出前核对文件和时间线。", 9f)
            layout.line("记录时间来自设备或导入文件元数据，不是第三方可信时间证明。", 9f)
            layout.close() // PdfDocument cannot be written while its final page is open.
            return ByteArrayOutputStream().also { document.writeTo(it) }.toByteArray()
        } finally {
            layout.close()
            document.close()
        }
    }
}

private class PdfLines(private val pdf: PdfDocument) {
    private val width = 595
    private val height = 842
    private val left = 40f
    private val maxTextWidth = width - 80f
    private var pageNumber = 0
    private var page: PdfDocument.Page? = null
    private var canvas: Canvas? = null
    private var y = 55f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }

    init { newPage() }

    private fun newPage() {
        page?.also { pdf.finishPage(it) }
        pageNumber++
        page = pdf.startPage(PdfDocument.PageInfo.Builder(width, height, pageNumber).create())
        canvas = page!!.canvas
        y = 55f
    }

    fun space() { y += 12f }

    fun line(text: String, size: Float = 10.5f, bold: Boolean = false) {
        paint.textSize = size
        paint.typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
        val rows = text.split('\n')
        for (row in rows) {
            var remaining = row.ifEmpty { " " }
            while (remaining.isNotEmpty()) {
                val count = paint.breakText(remaining, true, maxTextWidth, null).coerceAtLeast(1)
                val part = remaining.substring(0, count)
                if (y + size > height - 40) newPage()
                canvas!!.drawText(part, left, y, paint)
                y += size + 7f
                remaining = remaining.drop(count)
            }
        }
    }

    fun close() {
        page?.also { pdf.finishPage(it) }
        page = null
    }
}
