package io.github.overtimeevidence.app.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.AtomicFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.UUID

class EvidenceRepository(private val context: Context) {
    private val root = File(context.filesDir, "overtime-vault").apply { mkdirs() }
    private val evidenceDir = File(root, "evidence").apply { mkdirs() }
    private val atomicFile = AtomicFile(File(root, "manifest.json"))
    private val crypto = CryptoVault()
    private val lock = Mutex()

    private val mutable = MutableStateFlow(load())
    val state = mutable.asStateFlow()

    private fun load(): VaultSnapshot {
        // Never replace a damaged manifest with empty state: doing so risks losing evidence.
        return try {
            JSONObject(atomicFile.openRead().bufferedReader(Charsets.UTF_8).use { it.readText() }).asSnapshot()
        } catch (_: FileNotFoundException) {
            VaultSnapshot()
        }
    }

    private fun save(next: VaultSnapshot) {
        val out = atomicFile.startWrite()
        try {
            out.write(next.asJson().toString(2).toByteArray(Charsets.UTF_8))
            atomicFile.finishWrite(out)
            mutable.value = next
        } catch (e: Exception) {
            atomicFile.failWrite(out)
            throw e
        }
    }

    suspend fun startSession(task: String): WorkSession = lock.withLock {
        check(mutable.value.sessions.none { it.endedAt == null }) { "请先结束当前加班" }
        val now = System.currentTimeMillis()
        val session = WorkSession(UUID.randomUUID().toString(), task.trim().ifBlank { "加班工作" }, now)
        save(mutable.value.copy(sessions = listOf(session) + mutable.value.sessions))
        session
    }

    suspend fun endSession(id: String) = lock.withLock {
        val current = mutable.value
        check(current.sessions.any { it.id == id && it.endedAt == null }) { "记录不存在或已经结束" }
        save(current.copy(sessions = current.sessions.map {
            if (it.id == id) it.copy(endedAt = System.currentTimeMillis()) else it
        }))
    }

    suspend fun updateSession(id: String, task: String, note: String) = lock.withLock {
        val current = mutable.value
        check(current.sessions.any { it.id == id }) { "记录不存在" }
        save(current.copy(sessions = current.sessions.map {
            if (it.id == id) it.copy(task = task.trim().ifBlank { "加班工作" }, note = note) else it
        }))
    }

    suspend fun updateEvidence(id: String, kind: EvidenceKind, note: String) = lock.withLock {
        val current = mutable.value
        check(current.evidence.any { it.id == id }) { "材料不存在" }
        save(current.copy(evidence = current.evidence.map {
            if (it.id == id) it.copy(category = kind, note = note) else it
        }))
    }

    suspend fun deleteEvidence(id: String) = lock.withLock {
        val current = mutable.value
        if (current.evidence.none { it.id == id }) return@withLock
        save(current.copy(evidence = current.evidence.filterNot { it.id == id }))
        File(evidenceDir, "$id.ev").delete()
    }

    suspend fun deleteSession(id: String) = lock.withLock {
        val current = mutable.value
        val removed = current.evidence.filter { it.sessionId == id }
        save(current.copy(
            sessions = current.sessions.filterNot { it.id == id },
            evidence = current.evidence.filterNot { it.sessionId == id },
        ))
        removed.forEach { File(evidenceDir, "${it.id}.ev").delete() }
    }

    /** The URI is read using the temporary permission granted by Android's picker/sharesheet. */
    suspend fun importUris(
        uris: List<Uri>,
        requestedSessionId: String?,
        kind: EvidenceKind,
        source: String,
    ): Int = lock.withLock {
        if (uris.isEmpty()) return@withLock 0
        var current = mutable.value
        val sessionId = requestedSessionId?.takeIf { id -> current.sessions.any { it.id == id } }
            ?: current.sessions.firstOrNull { it.endedAt == null }?.id
            ?: run {
                val now = System.currentTimeMillis()
                val draft = WorkSession(
                    id = UUID.randomUUID().toString(),
                    task = "待整理：导入的加班材料",
                    startedAt = now,
                    endedAt = now,
                    draft = true,
                )
                current = current.copy(sessions = listOf(draft) + current.sessions)
                save(current)
                draft.id
            }

        var count = 0
        var firstError: Exception? = null
        for (uri in uris.distinct()) {
            try {
                val meta = lookupMetadata(uri)
                val id = UUID.randomUUID().toString()
                val tmp = File(evidenceDir, "$id.tmp")
                val final = File(evidenceDir, "$id.ev")
                val (hash, size) = context.contentResolver.openInputStream(uri).use { input ->
                    if (input == null) throw FileNotFoundException("无法读取所选文件")
                    crypto.encrypt(input, tmp)
                }
                if (!tmp.renameTo(final)) {
                    tmp.delete()
                    throw IllegalStateException("无法保存证据文件")
                }
                val row = Evidence(
                    id = id,
                    sessionId = sessionId,
                    name = meta.first,
                    mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream",
                    category = kind,
                    sha256 = hash,
                    bytes = size,
                    importedAt = System.currentTimeMillis(),
                    source = source,
                    originalModifiedAt = meta.second,
                )
                try {
                    val next = current.copy(evidence = listOf(row) + current.evidence)
                    save(next)
                    current = next
                    count++
                } catch (e: Exception) {
                    final.delete()
                    throw e
                }
            } catch (e: Exception) {
                if (firstError == null) firstError = e
            }
        }
        if (count == 0 && firstError != null) throw firstError
        count
    }

    private fun lookupMetadata(uri: Uri): Pair<String, Long?> {
        var name = uri.lastPathSegment?.substringAfterLast('/') ?: "附件"
        var modified: Long? = null
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: name
                    val modifiedIndex = cursor.getColumnIndex("last_modified")
                    if (modifiedIndex >= 0 && !cursor.isNull(modifiedIndex)) {
                        modified = cursor.getLong(modifiedIndex).takeIf { it > 0 }
                    }
                }
            }
        } catch (_: SecurityException) { /* Non-document providers may deny metadata queries. */ }
        return name to modified
    }

    fun openEvidence(evidence: Evidence): InputStream =
        crypto.open(File(evidenceDir, "${evidence.id}.ev"))
}
