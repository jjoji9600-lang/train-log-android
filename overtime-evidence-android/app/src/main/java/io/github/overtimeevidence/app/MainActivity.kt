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
import io.github.overtimeevidence.app.data.ProofElement
import io.github.overtimeevidence.app.ui.EvidenceScreen
import io.github.overtimeevidence.app.ui.EvidenceViewModel
import java.io.File
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val vm: EvidenceViewModel by viewModels()
    private var importSuggested: Set<ProofElement> = emptySet()
    private var importSession: String? = null
    private var exportSession: String? = null
    private var cameraFile: File? = null

    private val picker = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) vm.stageImport(uris, importSession, importSuggested, "picker")
    }
    private val camera = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = cameraFile
        cameraFile = null
        if (file != null) {
            if (success) {
                val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
                vm.stageImport(listOf(uri), importSession, importSuggested, "camera") { file.delete() }
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
        // Android may recreate this Activity while a camera, picker, or document UI is open.
        // Keep the requested session and camera filename so returned photos are not silently lost.
        savedInstanceState?.let { state ->
            importSession = state.getString("importSession")
            importSuggested = state.getStringArrayList("importSuggested")
                ?.mapNotNull(ProofElement::parse)?.toSet().orEmpty()
            exportSession = state.getString("exportSession")
            cameraFile = state.getString("cameraFileName")?.let { fileName ->
                File(File(cacheDir, "captures"), File(fileName).name)
            }
        }
        setContent {
            EvidenceScreen(
                viewModel = vm,
                onPick = { id, elements ->
                    importSession = id
                    importSuggested = elements
                    picker.launch(arrayOf("image/*", "application/pdf", "text/plain", "*/*"))
                },
                onCapture = { id, elements ->
                    importSession = id
                    importSuggested = elements
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("importSession", importSession)
        outState.putStringArrayList("importSuggested", ArrayList(importSuggested.map { it.name }))
        outState.putString("exportSession", exportSession)
        outState.putString("cameraFileName", cameraFile?.name)
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        receiveSharedFiles(intent)
    }

    private fun receiveSharedFiles(incoming: Intent?) {
        val received = incoming ?: return
        val action = received.action
        if (action != Intent.ACTION_SEND && action != Intent.ACTION_SEND_MULTIPLE) return
        val uris = when (action) {
            Intent.ACTION_SEND -> listOfNotNull(
                IntentCompat.getParcelableExtra(received, Intent.EXTRA_STREAM, Uri::class.java)
            )
            else -> IntentCompat.getParcelableArrayListExtra(received, Intent.EXTRA_STREAM, Uri::class.java)
                .orEmpty()
        }.ifEmpty {
            (0 until (received.clipData?.itemCount ?: 0)).mapNotNull {
                received.clipData?.getItemAt(it)?.uri
            }
        }
        if (uris.isNotEmpty()) vm.stageImport(uris, null, emptySet(), "share")
        setIntent(Intent(this, MainActivity::class.java))
    }
}