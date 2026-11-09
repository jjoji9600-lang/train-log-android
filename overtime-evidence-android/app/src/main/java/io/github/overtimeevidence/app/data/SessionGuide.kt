package io.github.overtimeevidence.app.data

/**
 * One main next action per session. Derived only from saved metadata.
 * These are collection prompts, never a legal judgement on sufficiency.
 */
enum class NextAction { REVIEW_EVENT, ADD_FIRST, SUPPLEMENT, END, EXPORT }

data class SessionGuide(
    val action: NextAction,
    val title: String,
    val reason: String,
    val button: String,
    val missingElement: ProofElement? = null,
)

fun proofCoverageCount(evidence: List<Evidence>): Int =
    ProofElement.entries.size - missingProofElements(evidence).size

fun sessionGuide(session: WorkSession, evidence: List<Evidence>): SessionGuide {
    if (session.draft && session.task.startsWith("待整理")) {
        return SessionGuide(
            NextAction.REVIEW_EVENT,
            "先说明这次加班是什么",
            "这是导入材料时自动创建的待整理记录；当前起止时间是导入时间，不是实际加班时间。请先在任务和备注中写明日期、工作内容及材料来源。",
            "补充事件说明",
        )
    }
    if (evidence.isEmpty()) {
        return SessionGuide(
            NextAction.ADD_FIRST,
            "先保存第一份证据",
            "仅记录起止时间不能证明工作事实。优先保存领导安排、工单或实际处理工作的原始截图。",
            "选择截图或文件",
        )
    }
    val missing = missingProofElements(evidence)
    if (missing.isNotEmpty()) {
        val first = missing.first()
        return SessionGuide(
            NextAction.SUPPLEMENT,
            "下一步：补充${first.label}",
            "尚无材料标注这一事项。建议：${first.suggestion}",
            "补充${first.label}材料",
            first,
        )
    }
    if (session.endedAt == null) {
        return SessionGuide(
            NextAction.END,
            "加班结束后，记得停止计时",
            "三类事项均已有标注材料，但这不代表证据充分。结束加班会记录停止时间，之后仍可补充证据。",
            "结束本次加班",
        )
    }
    return SessionGuide(
        NextAction.EXPORT,
        "建议导出备份",
        "三类事项均已有标注材料。导出的 ZIP 包含原件与目录，便于备份和以后核查；ZIP 未加密，请妥善保存。",
        "导出证据包",
    )
}
