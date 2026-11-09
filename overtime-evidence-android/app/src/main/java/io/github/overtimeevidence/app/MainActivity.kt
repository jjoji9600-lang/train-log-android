package io.github.overtimeevidence.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import io.github.overtimeevidence.app.data.EvidenceKind
import io.github.overtimeevidence.app.ui.EvidenceScreen
import io.github.overtimeevidence.app.ui.EvidenceViewModel
import java.io.File
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val vm: EvidenceViewModel by viewModels()
    private var importKind: EvidenceKind = EvidenceKind.PROCESS
    private var importSession: String? = null
    private var exportSession: String? = null
    private var cameraFile: File? = null

    private val picker = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) vm.import(uris, importSession, importKind, "picker")
    }
    private val camera = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = cameraFile
        cameraFile = null
        if (file != null) {
            if (success) {
                val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
                vm.import(listOf(uri), importSession, importKind, "camera") { file.delete() }
            } else {
                file.delete()
            }
        }
    }
    private val export = registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        val id = exportSession
        if (uri != null && id != null) vm.export(id, uri)
        exportSession = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EvidenceScreen(
                viewModel = vm,
                onPick = { id, kind ->
                    importSession = id
                    importKind = kind
                    picker.launch(arrayOf("image/*", "application/pdf", "text/plain", "*/*"))
                },
                onCapture = { id, kind ->
                    importSession = id
                    importKind = kind
                    val dir = File(cacheDir, "captures").apply { mkdirs() }
                    val file = File(dir, "photo_${UUID.randomUUID()}.jpg")
                    cameraFile = file
                    camera.launch(FileProvider.getUriForFile(this, "$packageName.fileprovider", file))
                },
                onExport = { id ->
                    exportSession = id
                    export.launch("overtime_evidence_${System.currentTimeMillis()}.zip")
                },
            )
        }
        // Shares are consumed once, and will not be re-imported on screen rotation.
        receiveSharedFiles(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        receiveSharedFiles(intent)
    }

    private fun receiveSharedFiles(incoming: Intent?) {
        val action = incoming?.action
        if (action != Intent.ACTION_SEND && action != Intent.ACTION_SEND_MULTIPLE) return
        val uris = when (action) {
            Intent.ACTION_SEND -> listOfNotNull(
                IntentCompat.getParcelableExtra(incoming, Intent.EXTRA_STREAM, Uri::class.java)
            )
            else -> IntentCompat.getParcelableArrayListExtra(incoming, Intent.EXTRA_STREAM, Uri::class.java)
                .orEmpty()
        }.ifEmpty {
            (0 until (incoming.clipData?.itemCount ?: 0)).mapNotNull {
                incoming.clipData?.getItemAt(it)?.uri
            }
        }
        if (uris.isNotEmpty()) vm.import(uris, null, EvidenceKind.PROCESS, "share")
        setIntent(Intent(this, MainActivity::class.java))
    }
}
