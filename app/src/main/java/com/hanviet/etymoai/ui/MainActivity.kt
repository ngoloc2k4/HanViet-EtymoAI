package com.hanviet.etymoai.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.data.db.workspace.entity.SavedWordEntity
import com.hanviet.etymoai.di.DatabaseModule
import com.hanviet.etymoai.domain.etymology.HanjaSegment
import com.hanviet.etymoai.domain.search.SearchResult
import com.hanviet.etymoai.domain.srs.ReviewRating
import com.hanviet.etymoai.domain.srs.SrsCard
import com.hanviet.etymoai.domain.srs.SrsFlashcardData
import com.hanviet.etymoai.ui.detail.WordDetailScreen
import com.hanviet.etymoai.ui.home.HomeScreen
import com.hanviet.etymoai.ui.quicklookup.QuickLookupActivity
import com.hanviet.etymoai.ui.srs.SrsReviewScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Trạng thái màn hình điều hướng của ứng dụng
 */
sealed interface AppScreen {
    data object Home : AppScreen
    data class WordDetail(val lexemeId: Long, val headword: String) : AppScreen
    data object SrsReview : AppScreen
}

class MainActivity : ComponentActivity() {

    private val coreDbHelper by lazy { DatabaseModule.provideCoreDatabaseHelper(this) }
    private val workspaceDb by lazy { DatabaseModule.provideWorkspaceDatabase(this) }
    private val etymologyRepository by lazy { DatabaseModule.provideEtymologyRepository(coreDbHelper) }
    private val searchPipeline by lazy { DatabaseModule.provideSearchPipeline(coreDbHelper) }
    private val srsRepository by lazy { DatabaseModule.provideSrsRepository(workspaceDb, etymologyRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF4F7FB)
                ) {
                    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
                    var searchQuery by remember { mutableStateOf("") }
                    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
                    var showSettingsDialog by remember { mutableStateOf(false) }
                    var showQuickLookupGuide by remember { mutableStateOf(false) }

                    val scope = rememberCoroutineScope()

                    // Debounce & live search khi người dùng gõ từ khóa
                    LaunchedEffect(searchQuery) {
                        if (searchQuery.isBlank()) {
                            searchResults = emptyList()
                        } else {
                            withContext(Dispatchers.IO) {
                                try {
                                    val results = searchPipeline.search(searchQuery.trim(), limit = 20)
                                    withContext(Dispatchers.Main) {
                                        searchResults = results
                                    }
                                } catch (e: Exception) {
                                    // Bắt lỗi nếu core db đang khởi tạo
                                    e.printStackTrace()
                                }
                            }
                        }
                    }

                    // Xử lý nút Back của Android
                    BackHandler(enabled = currentScreen !is AppScreen.Home) {
                        currentScreen = AppScreen.Home
                    }

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            is AppScreen.Home -> {
                                HomeScreen(
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { searchQuery = it },
                                    searchResults = searchResults,
                                    onSearchResultClick = { result ->
                                        currentScreen = AppScreen.WordDetail(
                                            lexemeId = result.lexemeId,
                                            headword = result.headwordNfc
                                        )
                                    },
                                    onWordClick = { word ->
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                val etymo = etymologyRepository.getEtymologyForWordByHeadword(word)
                                                withContext(Dispatchers.Main) {
                                                    if (etymo != null) {
                                                        currentScreen = AppScreen.WordDetail(
                                                            lexemeId = etymo.lexemeId,
                                                            headword = etymo.headword
                                                        )
                                                    } else {
                                                        // Nếu chưa có trong core db, chuyển sang lexemeId giả định để xem giao diện
                                                        currentScreen = AppScreen.WordDetail(
                                                            lexemeId = 1L,
                                                            headword = word
                                                        )
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    currentScreen = AppScreen.WordDetail(
                                                        lexemeId = 1L,
                                                        headword = word
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onNavigateToDictionary = {
                                        searchQuery = " " // Kích hoạt hiển thị kết quả
                                    },
                                    onNavigateToHanja = {
                                        searchQuery = "學"
                                    },
                                    onNavigateToMorphology = {
                                        searchQuery = "들었어요"
                                    },
                                    onNavigateToAi = {
                                        showSettingsDialog = true
                                    },
                                    onNavigateToSrs = {
                                        currentScreen = AppScreen.SrsReview
                                    },
                                    onNavigateToQuickLookupGuide = {
                                        showQuickLookupGuide = true
                                    },
                                    onOpenSettings = {
                                        showSettingsDialog = true
                                    }
                                )
                            }

                            is AppScreen.WordDetail -> {
                                WordDetailScreen(
                                    lexemeId = screen.lexemeId,
                                    repository = etymologyRepository,
                                    onBackClick = { currentScreen = AppScreen.Home },
                                    onWordClick = { nextLexemeId ->
                                        currentScreen = AppScreen.WordDetail(
                                            lexemeId = nextLexemeId,
                                            headword = ""
                                        )
                                    }
                                )
                            }

                            is AppScreen.SrsReview -> {
                                val sampleCards = remember {
                                    listOf(
                                        SrsFlashcardData(
                                            card = SrsCard(cardId = 1L, savedWordId = 101L),
                                            word = SavedWordEntity(
                                                savedId = 101L,
                                                headword = "경제",
                                                phoneticVi = "Kinh tế",
                                                originType = "SINO_KOREAN",
                                                primaryMeaning = "Nền kinh tế, hoạt động tài chính sản xuất"
                                            ),
                                            hanjaSegments = listOf(
                                                HanjaSegment(
                                                    hangulSyllable = "경",
                                                    hanjaChar = "經",
                                                    sinoVietnamese = "Kinh",
                                                    meaningVi = "Kinh bang tế thế, kinh qua, đường dọc",
                                                    radical = "糸",
                                                    strokeCount = 14
                                                ),
                                                HanjaSegment(
                                                    hangulSyllable = "제",
                                                    hanjaChar = "濟",
                                                    sinoVietnamese = "Tế",
                                                    meaningVi = "Cứu giúp, tế thế, qua sông",
                                                    radical = "氵",
                                                    strokeCount = 17
                                                )
                                            ),
                                            exampleSentenceKo = "한국의 경제가 빠르게 발전했습니다.",
                                            exampleSentenceVi = "Nền kinh tế Hàn Quốc đã phát triển nhanh chóng."
                                        ),
                                        SrsFlashcardData(
                                            card = SrsCard(cardId = 2L, savedWordId = 102L),
                                            word = SavedWordEntity(
                                                savedId = 102L,
                                                headword = "선생님",
                                                phoneticVi = "Tiên sinh",
                                                originType = "SINO_KOREAN",
                                                primaryMeaning = "Thầy giáo, cô giáo, người đi trước"
                                            ),
                                            hanjaSegments = listOf(
                                                HanjaSegment(
                                                    hangulSyllable = "선",
                                                    hanjaChar = "先",
                                                    sinoVietnamese = "Tiên",
                                                    meaningVi = "Đi trước, sớm hơn",
                                                    radical = "儿",
                                                    strokeCount = 6
                                                ),
                                                HanjaSegment(
                                                    hangulSyllable = "생",
                                                    hanjaChar = "生",
                                                    sinoVietnamese = "Sinh",
                                                    meaningVi = "Sinh nở, người sinh ra",
                                                    radical = "生",
                                                    strokeCount = 5
                                                )
                                            ),
                                            exampleSentenceKo = "선생님, 질문이 있습니다.",
                                            exampleSentenceVi = "Thưa thầy, em có một câu hỏi ạ."
                                        )
                                    )
                                }

                                SrsReviewScreen(
                                    cards = sampleCards,
                                    onRatingSelected = { card, rating ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Đã đánh giá: ${rating.labelVi}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onBackClick = { currentScreen = AppScreen.Home }
                                )
                            }
                        }
                    }

                    // Dialog Cài đặt & BYOK AI
                    if (showSettingsDialog) {
                        AlertDialog(
                            onDismissRequest = { showSettingsDialog = false },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFF1E3A8A)
                                )
                            },
                            title = {
                                Text(
                                    text = "Cài Đặt & Trí Tuệ Nhân Tạo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            },
                            text = {
                                Column {
                                    Text(
                                        text = "HanViet-EtymoAI v1.0.0 (Beta)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "• Kiến trúc: Offline-First SQLite FTS5\n• Bộ từ nguyên: NIKL & Viện Nghiên cứu Hán Nôm\n• Hỗ trợ BYOK (Google Gemini, OpenAI, Claude)\n• Không quảng cáo, bảo mật dữ liệu tuyệt đối.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF475569),
                                        lineHeight = 18.sp
                                    )
                                }
                            },
                            confirmButton = {
                                Button(onClick = { showSettingsDialog = false }) {
                                    Text("Đóng")
                                }
                            }
                        )
                    }

                    // Dialog Hướng dẫn Tra Cứu Nhanh Ngoài Màn Hình
                    if (showQuickLookupGuide) {
                        AlertDialog(
                            onDismissRequest = { showQuickLookupGuide = false },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB)
                                )
                            },
                            title = {
                                Text(
                                    text = "Tra Cứu Toàn Hệ Thống",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            },
                            text = {
                                Text(
                                    text = "Bạn có thể bôi đen bất kỳ từ tiếng Hàn nào trong Chrome, KakaoTalk, PDF Reader hoặc YouTube, rồi chọn 'Tra Hán-Việt' trong menu bôi đen để mở nhanh bảng tra cứu mà không cần rời ứng dụng!",
                                    fontSize = 13.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 19.sp
                                )
                            },
                            confirmButton = {
                                Button(onClick = { showQuickLookupGuide = false }) {
                                    Text("Đã hiểu")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
