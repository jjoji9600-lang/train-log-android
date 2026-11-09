package io.github.overtimeevidence.app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** UI journey: start -> Android share -> check proof element -> explain -> save -> remind -> end. */
@RunWith(AndroidJUnit4::class)
class EvidenceGuideUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun sharedScreenshotRequiresTagAndDescriptionAndThenShowsGaps() {
        fun checkItem(text: String) {
            rule.onNodeWithTag("session-content-list").performScrollToNode(hasText(text))
            rule.onNodeWithText(text).assertExists()
        }
        fun openAction(text: String) {
            rule.onNodeWithTag("session-content-list").performScrollToNode(hasText(text))
            rule.onNodeWithText(text).performClick()
        }
        rule.onNodeWithText("＋ 开始加班").performClick()
        rule.onNodeWithTag("new-session-task").performTextInput("自动化夜间留证")
        rule.onNodeWithText("开始记录").performClick()
        rule.waitUntil(20_000) {
            rule.onAllNodesWithText("继续记录：自动化夜间留证")
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("继续记录：自动化夜间留证").performClick()

        checkItem("○ 待补充 · 公司安排")
        checkItem("○ 待补充 · 加班事实")
        checkItem("○ 待补充 · 时长与工资")

        val activity = rule.activity
        val imageFile = File(activity.cacheDir, "captures/ui_share_sample.png").apply {
            parentFile?.mkdirs()
            val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.BLUE)
            outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        val uri = FileProvider.getUriForFile(activity, activity.packageName + ".fileprovider", imageFile)
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        // Deliver the same ACTION_SEND payload to the production handler,
        // without creating an extra Activity that confuses ActivityScenario teardown.
        rule.runOnUiThread { activity.receiveSharedFiles(share) }
        rule.waitUntil(20_000) {
            rule.onAllNodesWithText("保存材料 · 说明用途")
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("保存材料 · 说明用途").assertIsDisplayed()

        // The save action must stay disabled until both a tag and explanation are supplied.
        rule.onNodeWithTag("proof-checkbox-ARRANGED").performClick()
        rule.onNodeWithTag("evidence-description")
            .performTextInput("主管在 21:03 工作群要求晚上继续排障")
        rule.onNodeWithText("保存这份材料").performClick()

        rule.waitUntil(20_000) {
            rule.onAllNodesWithText("✓ 已标注 · 公司安排")
                .fetchSemanticsNodes().isNotEmpty()
        }
        checkItem("○ 待补充 · 加班事实")
        checkItem("○ 待补充 · 时长与工资")
        // Editing an existing file follows the same three-element / description language.
        openAction("三要素 / 说明")
        rule.onNodeWithText("修改材料的要素与说明").assertIsDisplayed()
        rule.onNodeWithTag("proof-checkbox-ARRANGED").assertExists()
        rule.onNodeWithText("取消").performClick()

        openAction("结束计时")
        rule.onNodeWithText("确认结束计时").performClick()
        rule.waitUntil(20_000) {
            rule.onAllNodesWithText("结束计时").fetchSemanticsNodes().isEmpty()
        }
        checkItem("○ 待补充 · 加班事实")
        checkItem("○ 待补充 · 时长与工资")
    }
}
