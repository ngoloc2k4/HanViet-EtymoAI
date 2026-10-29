package com.hanviet.etymoai.ui.quicklookup

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hanviet.etymoai.di.DatabaseModule
import com.hanviet.etymoai.ui.MainActivity

/**
 * QuickLookupActivity: Điểm chạm toàn hệ thống tiếp nhận văn bản bôi đen qua ACTION_PROCESS_TEXT.
 * Khởi chạy giao diện BottomSheet gọn nhẹ hiển thị từ nguyên trong thời gian cực nhanh.
 */
open class QuickLookupActivity : ComponentActivity() {

    private lateinit var viewModel: QuickLookupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val coreHelper = DatabaseModule.provideCoreDatabaseHelper(applicationContext)
        val wsDb = DatabaseModule.provideWorkspaceDatabase(applicationContext)
        val searchPipeline = DatabaseModule.provideSearchPipeline(coreHelper)
        val etymologyRepo = DatabaseModule.provideEtymologyRepository(coreHelper)
        val userWsRepo = DatabaseModule.provideUserWorkspaceRepository(wsDb)
        val srsRepo = DatabaseModule.provideSrsRepository(wsDb, etymologyRepo)

        viewModel = QuickLookupViewModel(
            searchPipeline = searchPipeline,
            etymologyRepository = etymologyRepo,
            userWorkspaceRepository = userWsRepo,
            srsRepository = srsRepo
        )

        val selectedText = when (intent.action) {
            Intent.ACTION_PROCESS_TEXT -> {
                intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: ""
            }
            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            }
            else -> {
                intent.getStringExtra("query") ?: ""
            }
        }

        if (selectedText.isBlank()) {
            finish()
            return
        }

        viewModel.lookup(selectedText)

        setContent {
            val state by viewModel.uiState.collectAsState()

            MaterialTheme {
                QuickLookupBottomSheetContent(
                    uiState = state,
                    onSaveClick = { viewModel.toggleSave() },
                    onOpenFullApp = { query ->
                        val mainIntent = Intent(this, MainActivity::class.java).apply {
                            putExtra("query", query)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        startActivity(mainIntent)
                        finish()
                    },
                    onDismiss = { finish() }
                )
            }
        }
    }
}
