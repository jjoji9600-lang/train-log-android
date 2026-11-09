package io.github.overtimeevidence.app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
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
        rule.onNodeWithText("＋ 开始加班").performClick()
        rule.onNodeWithTag("new-session-task").performTextInput("自动化夜间留证")
        rule.onNodeWithText("开始记录").performClick()
        rule.waitUntil(20_000) {
            rule.onAllNodesWithText("继续记录：自动化夜间留证")
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("继续记录：自动化夜间留证").performClick()

        rule.onNodeWithText("○ 公司安排 · 0 件已标注").assertExists()
        rule.onNodeWithText("○ 加班事实 · 0 件已标注").assertExists()
        rule.onNodeWithText("○ 时长与工资 · 0 件已标注").assertExists()

        val activity = rule.activity
        val imageFile = File(activity.cacheDir, "captures/ui_share_sample.png").apply {
            parentFile?.mkdirs()
            val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.BLUE)
            outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        val uri = FileProvider.getUriForFile(activity, activity.packageName + ".fileprovider", imageFile)
        val share = Intent(activity, MainActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        rule.runOnUiThread { activity.startActivity(share) }
        rule.onNodeWithText("给材料标注举证要素").assertIsDisplayed()

        // The save action must stay disabled until both a tag and explanation are supplied.
        rule.onNodeWithTag("proof-checkbox-ARRANGED").performClick()
        rule.onNodeWithTag("evidence-description")
            .performTextInput("主管在 21:03 工作群要求晚上继续排障")
        rule.onNodeWithText("确认归档").performClick()

        rule.waitUntil(20_000) {
            rule.onAllNodesWithText("✓ 公司安排 · 1 件已标注")
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("○ 加班事实 · 0 件已标注").assertExists()
        rule.onNodeWithText("○ 时长与工资 · 0 件已标注").assertExists()
        rule.onNodeWithText("结束加班").performClick()
        rule.waitUntil(20_000) {
            rule.onAllNodesWithText("结束加班").fetchSemanticsNodes().isEmpty()
        }
        rule.onNodeWithText("○ 加班事实 · 0 件已标注").assertExists()
        rule.onNodeWithText("○ 时长与工资 · 0 件已标注").assertExists()
    }
}
