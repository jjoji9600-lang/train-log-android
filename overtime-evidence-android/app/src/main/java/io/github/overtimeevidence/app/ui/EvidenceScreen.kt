package io.github.overtimeevidence.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.overtimeevidence.app.data.Evidence
import io.github.overtimeevidence.app.data.EvidenceKind
import io.github.overtimeevidence.app.data.ProofElement
import io.github.overtimeevidence.app.data.missingProofElements
import io.github.overtimeevidence.app.data.WorkSession
import io.github.overtimeevidence.app.util.elapsedMinutes
import io.github.overtimeevidence.app.util.formatDateTime
import io.github.overtimeevidence.app.util.formatDuration
import kotlinx.coroutines.flow.collect
import java.util.Calendar

@Composable
fun EvidenceScreen(
    viewModel: EvidenceViewModel,
    onPick: (String?, Set<ProofElement>) -> Unit,
    onCapture: (String?, Set<ProofElement>) -> Unit,
    onExport: (String) -> Unit,
) {
    val vault by viewModel.state.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val pending by viewModel.pendingImport.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var newSession by remember { mutableStateOf(false) }
    var task by remember { mutableStateOf("") }
    val selected = vault.sessions.firstOrNull { it.id == selectedId }
    val active = vault.sessions.firstOrNull { it.endedAt == null }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }

    MaterialTheme {
        Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { insets ->
            if (selected == null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(insets).padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        Text("加班留证", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("平时随手留存，需要时按事件导出。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    item {
                        val month = Calendar.getInstance()
                        val monthlyMinutes = vault.sessions.filter { session ->
                            val c = Calendar.getInstance().apply { timeInMillis = session.startedAt }
                            c.get(Calendar.YEAR) == month.get(Calendar.YEAR) &&
                                c.get(Calendar.MONTH) == month.get(Calendar.MONTH)
                        }.sumOf { session ->
                            session.endedAt?.let { elapsedMinutes(session.startedAt, it) } ?: 0L
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("本月已结束的记录跨度", style = MaterialTheme.typography.labelLarge)
                                Text(formatDuration(monthlyMinutes), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                                Text("共 ${vault.sessions.size} 条事件，${vault.evidence.size} 件材料。时长不自动扣除休息，也不等于依法认定的加班。", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    item {
                        if (active != null) {
                            Button(onClick = { selectedId = active.id }, modifier = Modifier.fillMaxWidth()) {
                                Text("继续记录：${active.task}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        } else {
                            Button(onClick = { task = ""; newSession = true }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                                Text("＋ 开始加班")
                            }
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onPick(active?.id, emptySet()) }, enabled = !busy, modifier = Modifier.weight(1f)) {
                                Text("导入截图/文件")
                            }
                            OutlinedButton(onClick = { onCapture(active?.id, emptySet()) }, enabled = !busy, modifier = Modifier.weight(1f)) {
                                Text("拍照留证")
                            }
                        }
                    }
                    item {
                        Text("加班记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    }
                    if (vault.sessions.isEmpty()) {
                        item {
                            HintCard("尚无记录。可以开始加班，也可以先导入已有的聊天截图、考勤文件。没有正在进行的记录时，会建立一条待整理事件。")
                        }
                    }
                    items(vault.sessions, key = { it.id }) { session ->
                        OutlinedCard(modifier = Modifier.fillMaxWidth().clickable { selectedId = session.id }) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(session.task, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("${formatDateTime(session.startedAt)} · ${if (session.endedAt == null) "进行中" else "已结束"}", style = MaterialTheme.typography.bodySmall)
                                Text("材料 ${vault.evidence.count { it.sessionId == session.id }} 件" +
                                    (session.endedAt?.let { " · 跨度 ${formatDuration(elapsedMinutes(session.startedAt, it))}" } ?: ""),
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val missing = missingProofElements(vault.evidence.filter { it.sessionId == session.id })
                                Text(if (missing.isEmpty()) "三要素均已标注（请核实证据内容）"
                                    else "待补充：${missing.joinToString("、") { it.label }}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (missing.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error)
                                if (session.draft) Text("待整理：请核实真实工作时间", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("隐私提示：默认不联网，附件在应用私有目录加密保存。请不要采集无关的客户隐私和商业秘密；请自行定期导出备份。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                    }
                }
            } else {
                SessionDetails(
                    session = selected,
                    evidence = vault.evidence.filter { it.sessionId == selected.id }.sortedBy { it.importedAt },
                    busy = busy,
                    onBack = { selectedId = null },
                    onEnd = { viewModel.end(selected.id) },
                    onEdit = { newTask, note -> viewModel.editSession(selected.id, newTask, note) },
                    onPick = { elements -> onPick(selected.id, elements) },
                    onCapture = { elements -> onCapture(selected.id, elements) },
                    onExport = { onExport(selected.id) },
                    onEditEvidence = { id, kind, note, elements -> viewModel.editEvidence(id, kind, note, elements) },
                    onDeleteEvidence = { viewModel.deleteEvidence(it) },
                    onDeleteSession = { viewModel.deleteSession(selected.id); selectedId = null },
                    modifier = Modifier.padding(insets),
                )
            }
        }
    }

    pending?.let { staged ->
        var checked by remember(staged.token) { mutableStateOf(staged.suggested) }
        var description by remember(staged.token) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.cancelPending() },
            title = { Text("给材料标注举证要素") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("已选择 ${staged.uris.size} 件材料。请勾选它能帮助证明的事项（可多选），并注明具体内容。保存后才计入提示。")
                    ProofChecklist(checked, onChange = { checked = it })
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("材料内容 / 证明什么（必填）") },
                        placeholder = { Text("例如：21:03主管在工作群指派故障排查，22:15回复处理结果") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("evidence-description"),
                    )
                    Text("分类标签只是用户说明，不等于司法机关已认定证据有效。", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmPending(checked, description) },
                    enabled = !busy && checked.isNotEmpty() && description.isNotBlank(),
                ) { Text(if (busy) "正在归档…" else "确认归档") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelPending() }, enabled = !busy) { Text("取消") }
            },
        )
    }

    if (newSession) {
        AlertDialog(
            onDismissRequest = { newSession = false },
            title = { Text("开始加班") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("记录时间使用手机系统时钟，不能单独证明公司安排或工作事实。")
                    OutlinedTextField(value = task, onValueChange = { task = it }, label = { Text("工作任务") }, singleLine = true,
                        modifier = Modifier.testTag("new-session-task"))
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.start(task); newSession = false }, enabled = !busy) { Text("开始记录") }
            },
            dismissButton = { TextButton(onClick = { newSession = false }) { Text("取消") } },
        )
    }
}

@Composable
private fun SessionDetails(
    session: WorkSession,
    evidence: List<Evidence>,
    busy: Boolean,
    onBack: () -> Unit,
    onEnd: () -> Unit,
    onEdit: (String, String) -> Unit,
    onPick: (Set<ProofElement>) -> Unit,
    onCapture: (Set<ProofElement>) -> Unit,
    onExport: () -> Unit,
    onEditEvidence: (String, EvidenceKind, String, Set<ProofElement>) -> Unit,
    onDeleteEvidence: (String) -> Unit,
    onDeleteSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editSession by remember { mutableStateOf(false) }
    var task by remember(session.id, session.task) { mutableStateOf(session.task) }
    var note by remember(session.id, session.note) { mutableStateOf(session.note) }
    var editingEvidence by remember { mutableStateOf<Evidence?>(null) }
    var pendingDeleteEvidence by remember { mutableStateOf<Evidence?>(null) }
    var pendingDeleteSession by remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onBack) { Text("← 返回记录") }
            Text(session.task, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("开始：${formatDateTime(session.startedAt)}", style = MaterialTheme.typography.bodyMedium)
            Text("结束：${session.endedAt?.let(::formatDateTime) ?: "正在记录"}", style = MaterialTheme.typography.bodyMedium)
            if (session.endedAt != null) {
                Text("记录跨度：${formatDuration(elapsedMinutes(session.startedAt, session.endedAt))}（未扣除休息）", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            if (session.note.isNotEmpty()) Text("备注：${session.note}")
            if (session.draft) Text("待整理事件的时间由导入动作生成，并非工作起止时间。请在备注中补充真实时间及出处。", color = MaterialTheme.colorScheme.error)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { editSession = true }, modifier = Modifier.weight(1f)) { Text("编辑说明") }
                if (session.endedAt == null) Button(onClick = onEnd, enabled = !busy, modifier = Modifier.weight(1f)) { Text("结束加班") }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("新增材料", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text("拍照或导入之后勾选三要素，并填写材料内容说明。",
                        style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onPick(emptySet()) }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("导入截图/文件") }
                        OutlinedButton(onClick = { onCapture(emptySet()) }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("拍照") }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("三要素留证提醒", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold)
                ProofElement.entries.forEach { element ->
                    val count = evidence.count { element in it.proofElements }
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("${if (count > 0) "✓" else "○"} ${element.label} · ${count} 件已标注",
                                style = MaterialTheme.typography.titleMedium)
                            if (count == 0) {
                                Text("尚无已提交的此类材料。建议：${element.suggestion}",
                                    style = MaterialTheme.typography.bodySmall)
                                OutlinedButton(onClick = { onPick(setOf(element)) }, enabled = !busy) {
                                    Text("补充这项材料")
                                }
                            } else {
                                Text("已标注不代表充分，建议核对原始记录及上下文。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("证据清单（${evidence.size}）", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = onExport, enabled = !busy) { Text("导出 ZIP") }
            }
        }
        if (evidence.isEmpty()) item { HintCard("尚未添加材料。优先保留工作安排、实际处理过程和完成反馈等带上下文的记录。") }
        items(evidence, key = { it.id }) { row ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(row.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${row.category.label} · 导入于 ${formatDateTime(row.importedAt)}", style = MaterialTheme.typography.bodySmall)
                    Text(if (row.proofElements.isEmpty()) "三要素：未标注（旧版材料请补标）"
                        else "三要素：${row.proofElements.joinToString("、") { it.label }}",
                        style = MaterialTheme.typography.bodySmall)
                    if (row.note.isNotBlank()) Text(row.note, style = MaterialTheme.typography.bodyMedium)
                    Text("SHA-256：${row.sha256.take(24)}… · ${row.bytes} 字节", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { editingEvidence = row }) { Text("三要素 / 说明") }
                        TextButton(onClick = { pendingDeleteEvidence = row }) { Text("删除") }
                    }
                }
            }
        }
        item {
            HorizontalDivider()
            Text("导出 ZIP 未加密，分享前请检查敏感资料。哈希只能检测保存后是否发生变化，不能证明截图最初真实。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { pendingDeleteSession = true }) { Text("删除本条加班及全部附件") }
            Spacer(Modifier.height(12.dp))
        }
    }
    if (editSession) {
        AlertDialog(
            onDismissRequest = { editSession = false },
            title = { Text("编辑事件") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = task, onValueChange = { task = it }, label = { Text("任务") }, maxLines = 2)
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注：工作地点、休息时长、任务来源等") }, minLines = 3)
                }
            },
            confirmButton = { TextButton(onClick = { onEdit(task, note); editSession = false }) { Text("保存") } },
            dismissButton = { TextButton(onClick = { editSession = false }) { Text("取消") } },
        )
    }
    editingEvidence?.let { item ->
        var editKind by remember(item.id) { mutableStateOf(item.category) }
        var editNote by remember(item.id) { mutableStateOf(item.note) }
        var editElements by remember(item.id) { mutableStateOf(item.proofElements) }
        AlertDialog(
            onDismissRequest = { editingEvidence = null },
            title = { Text("材料说明") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProofChecklist(editElements, onChange = { editElements = it })
                    KindSelector(editKind, onChange = { editKind = it })
                    OutlinedTextField(value = editNote, onValueChange = { editNote = it }, label = { Text("这份材料的具体内容（必填）") }, minLines = 2)
                }
            },
            confirmButton = { TextButton(onClick = { onEditEvidence(item.id, editKind, editNote, editElements); editingEvidence = null },
                enabled = !busy && editElements.isNotEmpty() && editNote.isNotBlank()) { Text("保存") } },
            dismissButton = { TextButton(onClick = { editingEvidence = null }) { Text("取消") } },
        )
    }
    pendingDeleteEvidence?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDeleteEvidence = null },
            title = { Text("删除材料？") },
            text = { Text("“${item.name}”将从本机证据库删除。此操作不可撤销。") },
            confirmButton = { TextButton(onClick = { onDeleteEvidence(item.id); pendingDeleteEvidence = null }) { Text("删除") } },
            dismissButton = { TextButton(onClick = { pendingDeleteEvidence = null }) { Text("取消") } },
        )
    }
    if (pendingDeleteSession) {
        AlertDialog(
            onDismissRequest = { pendingDeleteSession = false },
            title = { Text("永久删除加班记录？") },
            text = { Text("这条加班记录及其所有附件都将从应用私有存储删除。请确认已备份。") },
            confirmButton = { TextButton(onClick = { onDeleteSession(); pendingDeleteSession = false }) { Text("确认删除") } },
            dismissButton = { TextButton(onClick = { pendingDeleteSession = false }) { Text("取消") } },
        )
    }
}

@Composable
private fun ProofChecklist(selected: Set<ProofElement>, onChange: (Set<ProofElement>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("举证三要素（可多选）", style = MaterialTheme.typography.labelLarge)
        ProofElement.entries.forEach { element ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    modifier = Modifier.testTag("proof-checkbox-" + element.name),
                    checked = element in selected,
                    onCheckedChange = { checked ->
                        onChange(if (checked) selected + element else selected - element)
                    },
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(element.label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun KindSelector(selected: EvidenceKind, onChange: (EvidenceKind) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text("证据分类", style = MaterialTheme.typography.labelMedium)
        OutlinedButton(onClick = { expanded = true }) { Text("${selected.label} ▾") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            EvidenceKind.entries.forEach { kind ->
                DropdownMenuItem(text = { Text(kind.label) }, onClick = { onChange(kind); expanded = false })
            }
        }
    }
}

@Composable
private fun HintCard(message: String) {
    Surface(shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Text(message, modifier = Modifier.fillMaxWidth().padding(14.dp), style = MaterialTheme.typography.bodyMedium)
    }
}