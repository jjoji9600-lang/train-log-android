package io.github.overtimeevidence.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.overtimeevidence.app.data.EvidenceKind
import io.github.overtimeevidence.app.data.EvidenceRepository
import io.github.overtimeevidence.app.export.EvidenceExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EvidenceViewModel(app: Application) : AndroidViewModel(app) {
    val repository = EvidenceRepository(app)
    val state = repository.state
    private val mutableBusy = MutableStateFlow(false)
    val busy = mutableBusy.asStateFlow()
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
    fun end(id: String) = work("已结束加班，记得补齐证据") { repository.endSession(id) }
    fun editSession(id: String, task: String, note: String) = work("记录已更新") {
        repository.updateSession(id, task, note)
    }
    fun editEvidence(id: String, kind: EvidenceKind, note: String) = work("材料已更新") {
        repository.updateEvidence(id, kind, note)
    }
    fun deleteEvidence(id: String) = work("已删除材料") { repository.deleteEvidence(id) }
    fun deleteSession(id: String) = work("记录及其附件已删除") { repository.deleteSession(id) }

    fun import(
        uris: List<Uri>,
        sessionId: String?,
        kind: EvidenceKind,
        source: String,
        after: (() -> Unit)? = null,
    ) = work(after = after) {
        val n = repository.importUris(uris, sessionId, kind, source)
        mutableMessages.emit("已归档 $n 件材料${if (n < uris.distinct().size) "，部分文件无法读取" else ""}")
    }

    fun export(id: String, destination: Uri) = work("证据包已导出（未加密，请妥善保管）") {
        EvidenceExporter(getApplication(), repository).export(id, state.value, destination)
    }
}
