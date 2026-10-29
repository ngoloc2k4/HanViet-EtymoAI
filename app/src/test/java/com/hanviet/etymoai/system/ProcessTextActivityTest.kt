package com.hanviet.etymoai.system

import android.content.Intent
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProcessTextActivityTest {

    @Test
    fun testProcessTextActivity_emptyIntentFinishes() {
        val intent = Intent(Intent.ACTION_PROCESS_TEXT)
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, "")

        val controller = Robolectric.buildActivity(ProcessTextActivity::class.java, intent)
        val activity = controller.create().get()

        assertNotNull(activity)
        assertTrue("Activity phải kết thúc khi chuỗi văn bản rỗng", activity.isFinishing)
    }

    @Test
    fun testProcessTextActivity_withTextRuns() {
        val intent = Intent(Intent.ACTION_PROCESS_TEXT)
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, "기분")

        val controller = Robolectric.buildActivity(ProcessTextActivity::class.java, intent)
        val activity = controller.create().start().resume().get()

        assertNotNull(activity)
        // Activity khởi tạo thành công nhận intent hợp lệ
    }
}
