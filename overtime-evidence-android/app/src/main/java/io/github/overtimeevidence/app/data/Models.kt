package io.github.overtimeevidence.app.data

import org.json.JSONArray
import org.json.JSONObject

enum class EvidenceKind(val label: String) {
    ARRANGEMENT("公司安排"),
    PROCESS("工作过程"),
    RESULT("工作结果"),
    ATTENDANCE("考勤记录"),
    PAY("工资依据"),
    OTHER("其他材料");

    companion object {
        fun parse(s: String) = entries.firstOrNull { it.name == s } ?: OTHER
    }
}

/** The three practical proof questions. Tags are user assertions, not legal findings. */
enum class ProofElement(val label: String, val suggestion: String) {
    FACT("加班事实", "补充下班后工单、工作邮件、任务处理日志或成果截图，并保留上下文。"),
    ARRANGED("公司安排", "补充领导指令、排班通知、审批记录，或公司知情并要求继续处理任务的消息。"),
    TIME_PAY("时长与工资", "补充上下班考勤、任务开始与结束记录、休息时间，以及工资条或劳动合同。");

    companion object {
        fun parse(value: String): ProofElement? = entries.firstOrNull { it.name == value }
    }
}

fun missingProofElements(evidence: List<Evidence>): List<ProofElement> =
    ProofElement.entries.filter { type -> evidence.none { type in it.proofElements } }

data class WorkSession(
    val id: String,
    val task: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val draft: Boolean = false,
)

data class Evidence(
    val id: String,
    val sessionId: String,
    val name: String,
    val mimeType: String,
    val category: EvidenceKind,
    val note: String = "",
    val sha256: String,
    val bytes: Long,
    val importedAt: Long,
    val source: String,
    val originalModifiedAt: Long? = null,
    val proofElements: Set<ProofElement> = emptySet(),
)

data class VaultSnapshot(
    val sessions: List<WorkSession> = emptyList(),
    val evidence: List<Evidence> = emptyList(),
)

fun WorkSession.asJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("task", task)
    put("startedAt", startedAt)
    put("endedAt", endedAt ?: JSONObject.NULL)
    put("note", note)
    put("createdAt", createdAt)
    put("draft", draft)
}

fun Evidence.asJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("sessionId", sessionId)
    put("name", name)
    put("mimeType", mimeType)
    put("category", category.name)
    put("note", note)
    put("sha256", sha256)
    put("bytes", bytes)
    put("importedAt", importedAt)
    put("source", source)
    put("originalModifiedAt", originalModifiedAt ?: JSONObject.NULL)
    put("proofElements", JSONArray().also { arr -> proofElements.sortedBy { it.ordinal }.forEach { arr.put(it.name) } })
}

fun VaultSnapshot.asJson(): JSONObject = JSONObject().apply {
    put("schemaVersion", 1)
    put("sessions", JSONArray().also { a -> sessions.forEach { a.put(it.asJson()) } })
    put("evidence", JSONArray().also { a -> evidence.forEach { a.put(it.asJson()) } })
}

fun JSONObject.asSession() = WorkSession(
    id = getString("id"),
    task = optString("task", "加班记录"),
    startedAt = getLong("startedAt"),
    endedAt = if (isNull("endedAt")) null else getLong("endedAt"),
    note = optString("note", ""),
    createdAt = optLong("createdAt", getLong("startedAt")),
    draft = optBoolean("draft", false),
)

fun JSONObject.asEvidence() = Evidence(
    id = getString("id"),
    sessionId = getString("sessionId"),
    name = getString("name"),
    mimeType = optString("mimeType", "application/octet-stream"),
    category = EvidenceKind.parse(optString("category", "OTHER")),
    note = optString("note", ""),
    sha256 = getString("sha256"),
    bytes = getLong("bytes"),
    importedAt = getLong("importedAt"),
    source = optString("source", "unknown"),
    originalModifiedAt = if (isNull("originalModifiedAt")) null else getLong("originalModifiedAt"),
    proofElements = optJSONArray("proofElements")?.let { arr ->
        (0 until arr.length()).mapNotNull { ProofElement.parse(arr.optString(it)) }.toSet()
    } ?: emptySet(),
)

fun JSONObject.asSnapshot(): VaultSnapshot {
    require(optInt("schemaVersion", 1) == 1) { "不支持的数据版本" }
    val sessions = getJSONArray("sessions").let { a ->
        (0 until a.length()).map { a.getJSONObject(it).asSession() }
    }
    val evidence = getJSONArray("evidence").let { a ->
        (0 until a.length()).map { a.getJSONObject(it).asEvidence() }
    }
    return VaultSnapshot(sessions, evidence)
}
