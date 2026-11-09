package io.github.overtimeevidence.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.overtimeevidence.app.data.EvidenceKind
import io.github.overtimeevidence.app.data.EvidenceRepository
import io.github.overtimeevidence.app.data.ProofElement
import io.github.overtimeevidence.app.data.missingProofElements
import io.github.overtimeevidence.app.export.EvidenceExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class PendingImport(
    val token: String = UUID.randomUUID().toString(),
    val uris: List<Uri>,
    val sessionId: String?,
    val suggested: Set<ProofElement>,
    val source: String,
    val cleanup: (() -> Unit)? = null,
)

class EvidenceViewModel(app: Application) : AndroidViewModel(app) {
    val repository = EvidenceRepository(app)
    val state = repository.state
    private val mutableBusy = MutableStateFlow(false)
    val busy = mutableBusy.asStateFlow()
    private val mutablePending = MutableStateFlow<PendingImport?>(null)
    val pendingImport = mutablePending.asStateFlow()
    private val mutableMessages = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val messages = mutableMessages.asSharedFlow()

    private fun work(success: String? = null, after: (() -> Unit)? = null, action: suspend () -> Unit) {
        viewModelScope.launch {
            mutableBusy.value = true
            try {
                withContext(Dispatchers.IO) { action() }
                if (success != null) mutableMessages.emit(success)
            } catch (e: Exception) {
                mutableMessages.emit("操作失败：${e.localizedMessage ?: e.javaClass.simpleName}")
            } finally {
                mutableBusy.value = false
                after?.invoke()
            }
        }
    }

    fun start(task: String) = work("加班记录已开始") { repository.startSession(task) }
    fun end(id: String) = work {
        repository.endSession(id)
        val missing = missingProofElements(state.value.evidence.filter { it.sessionId == id })
        mutableMessages.emit(if (missing.isEmpty()) "已结束加班。三项均有标注材料，仍请核实证据内容。"
            else "已结束加班，建议补充：${missing.joinToString("、") { it.label }}")
    }
    fun editSession(id: String, task: String, note: String) = work("记录已更新") {
        repository.updateSession(id, task, note)
    }
    fun editEvidence(id: String, note: String, proofElements: Set<ProofElement>) =
        work("材料已更新") {
            repository.updateEvidence(id, inferredKind(proofElements), note, proofElements)
        }
    fun deleteEvidence(id: String) = work("已删除材料") { repository.deleteEvidence(id) }
    fun deleteSession(id: String) = work("记录及其附件已删除") { repository.deleteSession(id) }

    /** Keep the legacy export category automatically in sync with the visible proof tags. */
    private fun inferredKind(proofElements: Set<ProofElement>): EvidenceKind = when {
        ProofElement.ARRANGED in proofElements -> EvidenceKind.ARRANGEMENT
        ProofElement.TIME_PAY in proofElements -> EvidenceKind.ATTENDANCE
        else -> EvidenceKind.PROCESS
    }

    /** Stage incoming files and ask for proof tags / explanation before writing to vault. */
    fun stageImport(
        uris: List<Uri>,
        sessionId: String?,
        suggested: Set<ProofElement>,
        source: String,
        cleanup: (() -> Unit)? = null,
    ) {
        if (uris.isEmpty()) { cleanup?.invoke(); return }
        if (mutablePending.value != null) {
            mutableMessages.tryEmit("请先保存或取消当前待归档材料")
            cleanup?.invoke()
            return
        }
        mutablePending.value = PendingImport(
            uris = uris.distinct(), sessionId = sessionId,
            suggested = suggested, source = source, cleanup = cleanup,
        )
    }

    fun cancelPending() {
        if (mutableBusy.value) return
        val previous = mutablePending.value ?: return
        mutablePending.value = null
        previous.cleanup?.invoke()
    }

    fun confirmPending(proofElements: Set<ProofElement>, note: String) {
        val current = mutablePending.value ?: return
        if (mutableBusy.value) return
        if (proofElements.isEmpty() || note.isBlank()) {
            mutableMessages.tryEmit("请选择至少一个要素，并写明材料内容")
            return
        }
        val kind = inferredKind(proofElements)
        viewModelScope.launch {
            mutableBusy.value = true
            try {
                val count = withContext(Dispatchers.IO) {
                    repository.importUris(current.uris, current.sessionId, kind, current.source, proofElements, note)
                }
                mutablePending.value = null
                current.cleanup?.invoke()
                mutableMessages.emit("已归档 $count 件材料" +
                    if (count < current.uris.size) "，部分文件无法读取" else "")
            } catch (e: Exception) {
                // Keep the pending content so the user can retry rather than losing the photo.
                mutableMessages.emit("归档失败，请重试：${e.localizedMessage ?: e.javaClass.simpleName}")
            } finally {
                mutableBusy.value = false
            }
        }
    }

    fun export(id: String, destination: Uri) = work("证据包已导出（未加密，请妥善保管）") {
        EvidenceExporter(getApplication(), repository).export(id, state.value, destination)
    }
}
