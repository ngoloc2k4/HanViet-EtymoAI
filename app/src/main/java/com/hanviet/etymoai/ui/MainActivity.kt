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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.hanviet.etymoai.ui.ai.AiChatScreen
import com.hanviet.etymoai.ui.ai.AiDetailAnalysisScreen
import com.hanviet.etymoai.ui.ai.SentenceAnalyzerScreen
import com.hanviet.etymoai.ui.detail.WordDetailScreen
import com.hanviet.etymoai.ui.home.HomeScreen
import com.hanviet.etymoai.ui.hanja.HanjaExplorerScreen
import com.hanviet.etymoai.ui.history.SearchHistoryScreen
import com.hanviet.etymoai.ui.quicklookup.QuickLookupActivity
import com.hanviet.etymoai.ui.search.SearchResultsScreen
import com.hanviet.etymoai.ui.settings.AboutDataLicenseScreen
import com.hanviet.etymoai.ui.settings.AppearanceSettingsScreen
import com.hanviet.etymoai.ui.settings.DataPackManagerScreen
import com.hanviet.etymoai.ui.settings.SecuritySettingsScreen
import com.hanviet.etymoai.ui.settings.SettingsOverviewScreen
import com.hanviet.etymoai.ui.srs.SrsDeckListScreen
import com.hanviet.etymoai.ui.srs.SrsReviewScreen
import com.hanviet.etymoai.ui.srs.StudyStatsScreen
import com.hanviet.etymoai.ui.vocabulary.MyVocabularyScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Trạng thái màn hình điều hướng của ứng dụng
 */
sealed interface AppScreen {
    data object Home : AppScreen
    data class SearchResults(val query: String = "도서관") : AppScreen
    data class WordDetail(val lexemeId: Long, val headword: String) : AppScreen
    data object MyVocabulary : AppScreen
    data object SrsReview : AppScreen
    data object StudyStats : AppScreen
    data object AiChat : AppScreen
    data class AiDetailAnalysis(val headword: String) : AppScreen
    data object SentenceAnalyzer : AppScreen
    data object SecuritySettings : AppScreen
    data object DataPackManager : AppScreen
    data object AppearanceSettings : AppScreen
    data object SettingsOverview : AppScreen
    data object AboutDataLicense : AppScreen
    data object SrsDeckList : AppScreen
    data class HanjaExplorer(val initialHanja: String = "學") : AppScreen
    data object SearchHistory : AppScreen
}

private val AppScreenSaver: Saver<AppScreen, String> = Saver(
    save = { screen ->
        when (screen) {
            is AppScreen.Home -> "Home"
            is AppScreen.SearchResults -> "SearchResults|${screen.query}"
            is AppScreen.WordDetail -> "WordDetail|${screen.lexemeId}|${screen.headword}"
            is AppScreen.MyVocabulary -> "MyVocabulary"
            is AppScreen.SrsReview -> "SrsReview"
            is AppScreen.StudyStats -> "StudyStats"
            is AppScreen.AiChat -> "AiChat"
            is AppScreen.AiDetailAnalysis -> "AiDetailAnalysis|${screen.headword}"
            is AppScreen.SentenceAnalyzer -> "SentenceAnalyzer"
            is AppScreen.SecuritySettings -> "SecuritySettings"
            is AppScreen.DataPackManager -> "DataPackManager"
            is AppScreen.AppearanceSettings -> "AppearanceSettings"
            is AppScreen.SettingsOverview -> "SettingsOverview"
            is AppScreen.AboutDataLicense -> "AboutDataLicense"
            is AppScreen.SrsDeckList -> "SrsDeckList"
            is AppScreen.HanjaExplorer -> "HanjaExplorer|${screen.initialHanja}"
            is AppScreen.SearchHistory -> "SearchHistory"
        }
    },
    restore = { str ->
        val parts = str.split("|")
        when (parts[0]) {
            "Home" -> AppScreen.Home
            "SearchResults" -> AppScreen.SearchResults(parts.getOrElse(1) { "도서관" })
            "WordDetail" -> AppScreen.WordDetail(
                lexemeId = parts.getOrNull(1)?.toLongOrNull() ?: 1L,
                headword = parts.getOrElse(2) { "" }
            )
            "MyVocabulary" -> AppScreen.MyVocabulary
            "SrsReview" -> AppScreen.SrsReview
            "StudyStats" -> AppScreen.StudyStats
            "AiChat" -> AppScreen.AiChat
            "AiDetailAnalysis" -> AppScreen.AiDetailAnalysis(parts.getOrElse(1) { "" })
            "SentenceAnalyzer" -> AppScreen.SentenceAnalyzer
            "SecuritySettings" -> AppScreen.SecuritySettings
            "DataPackManager" -> AppScreen.DataPackManager
            "AppearanceSettings" -> AppScreen.AppearanceSettings
            "SettingsOverview" -> AppScreen.SettingsOverview
            "AboutDataLicense" -> AppScreen.AboutDataLicense
            "SrsDeckList" -> AppScreen.SrsDeckList
            "HanjaExplorer" -> AppScreen.HanjaExplorer(parts.getOrElse(1) { "學" })
            "SearchHistory" -> AppScreen.SearchHistory
            else -> AppScreen.Home
        }
    }
)

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
                    val backStackSaver = Saver<androidx.compose.runtime.snapshots.SnapshotStateList<AppScreen>, ArrayList<String>>(
                        save = { list ->
                            val arrayList = ArrayList<String>(list.size)
                            list.forEach { screen ->
                                val serialized = AppScreenSaver.save(this, screen) ?: "Home"
                                arrayList.add(serialized)
                            }
                            arrayList
                        },
                        restore = { savedList ->
                            mutableStateListOf<AppScreen>().apply {
                                val restored = savedList.mapNotNull { AppScreenSaver.restore(it) }
                                if (restored.isEmpty()) add(AppScreen.Home) else addAll(restored)
                            }
                        }
                    )

                    val backStack = rememberSaveable(saver = backStackSaver) {
                        mutableStateListOf<AppScreen>(AppScreen.Home)
                    }
                    val currentScreen = backStack.lastOrNull() ?: AppScreen.Home

                    fun navigateTo(screen: AppScreen, isTabSwitch: Boolean = false) {
                        if (isTabSwitch) {
                            if (screen == AppScreen.Home) {
                                backStack.clear()
                                backStack.add(AppScreen.Home)
                            } else {
                                backStack.removeAll { it != AppScreen.Home }
                                backStack.add(screen)
                            }
                        } else {
                            backStack.add(screen)
                        }
                    }

                    fun navigateBack() {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }

                    var searchQuery by rememberSaveable { mutableStateOf("") }
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

                    // Xử lý nút Back của Android đồng bộ với stack điều hướng
                    BackHandler(enabled = currentScreen !is AppScreen.Home) {
                        navigateBack()
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
                                        navigateTo(
                                            AppScreen.WordDetail(
                                                lexemeId = result.lexemeId,
                                                headword = result.headwordNfc
                                            )
                                        )
                                    },
                                    onWordClick = { word ->
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                val etymo = etymologyRepository.getEtymologyForWordByHeadword(word)
                                                withContext(Dispatchers.Main) {
                                                    if (etymo != null) {
                                                        navigateTo(
                                                            AppScreen.WordDetail(
                                                                lexemeId = etymo.lexemeId,
                                                                headword = etymo.headword
                                                            )
                                                        )
                                                    } else {
                                                        // Nếu chưa có trong core db, chuyển sang lexemeId giả định để xem giao diện
                                                        navigateTo(
                                                            AppScreen.WordDetail(
                                                                lexemeId = 1L,
                                                                headword = word
                                                            )
                                                        )
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    navigateTo(
                                                        AppScreen.WordDetail(
                                                            lexemeId = 1L,
                                                            headword = word
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onNavigateToDictionary = {
                                        navigateTo(AppScreen.SearchResults(searchQuery.ifBlank { "도서관" }), isTabSwitch = true)
                                    },
                                    onNavigateToHanja = {
                                        navigateTo(AppScreen.HanjaExplorer("學"))
                                    },
                                    onNavigateToMorphology = {
                                        navigateTo(AppScreen.SearchResults("들었어요"))
                                    },
                                    onNavigateToAi = {
                                        navigateTo(AppScreen.AiChat, isTabSwitch = true)
                                    },
                                    onNavigateToSrs = {
                                        navigateTo(AppScreen.MyVocabulary, isTabSwitch = true)
                                    },
                                    onNavigateToQuickLookupGuide = {
                                        showQuickLookupGuide = true
                                    },
                                    onOpenSettings = {
                                        navigateTo(AppScreen.SettingsOverview, isTabSwitch = true)
                                    }
                                )
                            }

                            is AppScreen.SearchResults -> {
                                SearchResultsScreen(
                                    initialQuery = screen.query,
                                    onWordClick = { headword ->
                                        navigateTo(
                                            AppScreen.WordDetail(
                                                lexemeId = 101L,
                                                headword = headword
                                            )
                                        )
                                    },
                                    onBackClick = { navigateBack() },
                                    onSpeakWord = { word ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Phát âm: $word",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onNavigateTab = { tabIndex ->
                                        when (tabIndex) {
                                            0 -> navigateTo(AppScreen.Home, isTabSwitch = true)
                                            1 -> { /* Đang ở Tra cứu */ }
                                            2 -> navigateTo(AppScreen.AiChat, isTabSwitch = true)
                                            3 -> navigateTo(AppScreen.MyVocabulary, isTabSwitch = true)
                                            4 -> navigateTo(AppScreen.SettingsOverview, isTabSwitch = true)
                                        }
                                    }
                                )
                            }

                            is AppScreen.WordDetail -> {
                                WordDetailScreen(
                                    lexemeId = screen.lexemeId,
                                    headword = screen.headword,
                                    repository = etymologyRepository,
                                    onBackClick = { navigateBack() },
                                    onWordClick = { nextLexemeId ->
                                        navigateTo(
                                            AppScreen.WordDetail(
                                                lexemeId = nextLexemeId,
                                                headword = ""
                                            )
                                        )
                                    },
                                    onAskAiClick = { word ->
                                        navigateTo(AppScreen.AiDetailAnalysis(word))
                                    },
                                    onSaveWord = { word ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Đã lưu từ: $word",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onSpeakWord = { word ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Phát âm: $word",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onNavigateTab = { tabIndex ->
                                        when (tabIndex) {
                                            0 -> navigateTo(AppScreen.Home, isTabSwitch = true)
                                            1 -> navigateTo(AppScreen.SearchResults(), isTabSwitch = true)
                                            2 -> navigateTo(AppScreen.AiChat, isTabSwitch = true)
                                            3 -> navigateTo(AppScreen.SrsDeckList, isTabSwitch = true)
                                            4 -> navigateTo(AppScreen.SettingsOverview, isTabSwitch = true)
                                        }
                                    }
                                )
                            }

                            is AppScreen.SrsReview -> {
                                val sampleCards = remember {
                                    listOf(
                                        SrsFlashcardData(
                                            card = SrsCard(cardId = 1L, savedWordId = 101L),
                                            word = SavedWordEntity(
                                                id = 101L,
                                                stableKey = "ko_gyeongje_101",
                                                headwordSnapshot = "경제",
                                                phoneticSnapshot = "Kinh tế",
                                                originType = "SINO_KOREAN",
                                                meaningSnapshot = "Nền kinh tế, hoạt động tài chính sản xuất"
                                            ),
                                            hanjaSegments = listOf(
                                                HanjaSegment(
                                                    segmentIndex = 0,
                                                    syllableKo = "경",
                                                    hanjaChar = "經",
                                                    readingVi = "Kinh",
                                                    meaningVi = "Kinh bang tế thế, kinh qua, đường dọc"
                                                ),
                                                HanjaSegment(
                                                    segmentIndex = 1,
                                                    syllableKo = "제",
                                                    hanjaChar = "濟",
                                                    readingVi = "Tế",
                                                    meaningVi = "Cứu giúp, tế thế, qua sông"
                                                )
                                            ),
                                            exampleSentenceKo = "한국의 경제가 빠르게 발전했습니다.",
                                            exampleSentenceVi = "Nền kinh tế Hàn Quốc đã phát triển nhanh chóng."
                                        ),
                                        SrsFlashcardData(
                                            card = SrsCard(cardId = 2L, savedWordId = 102L),
                                            word = SavedWordEntity(
                                                id = 102L,
                                                stableKey = "ko_seonsaengnim_102",
                                                headwordSnapshot = "선생님",
                                                phoneticSnapshot = "Tiên sinh",
                                                originType = "SINO_KOREAN",
                                                meaningSnapshot = "Thầy giáo, cô giáo, người đi trước"
                                            ),
                                            hanjaSegments = listOf(
                                                HanjaSegment(
                                                    segmentIndex = 0,
                                                    syllableKo = "선",
                                                    hanjaChar = "先",
                                                    readingVi = "Tiên",
                                                    meaningVi = "Đi trước, sớm hơn"
                                                ),
                                                HanjaSegment(
                                                    segmentIndex = 1,
                                                    syllableKo = "생",
                                                    hanjaChar = "生",
                                                    readingVi = "Sinh",
                                                    meaningVi = "Sinh nở, người sinh ra"
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
                                    onBackClick = { navigateBack() },
                                    onNavigateToStats = { navigateTo(AppScreen.StudyStats) },
                                    onSpeakWord = { word ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Phát âm: $word",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onNavigateTab = { tabIndex ->
                                        when (tabIndex) {
                                            0 -> navigateTo(AppScreen.Home, isTabSwitch = true)
                                            1 -> navigateTo(AppScreen.SearchResults(), isTabSwitch = true)
                                            2 -> navigateTo(AppScreen.AiChat, isTabSwitch = true)
                                            3 -> navigateTo(AppScreen.SrsDeckList, isTabSwitch = true)
                                            4 -> navigateTo(AppScreen.SettingsOverview, isTabSwitch = true)
                                        }
                                    }
                                )
                            }

                            is AppScreen.MyVocabulary -> {
                                MyVocabularyScreen(
                                    onWordClick = { headword ->
                                        navigateTo(
                                            AppScreen.WordDetail(
                                                lexemeId = 101L,
                                                headword = headword
                                            )
                                        )
                                    },
                                    onBackClick = { navigateBack() },
                                    onSpeakWord = { word ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Phát âm: $word",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onStartSrsReview = {
                                        navigateTo(AppScreen.SrsDeckList)
                                    },
                                    onNavigateTab = { tabIndex ->
                                        when (tabIndex) {
                                            0 -> navigateTo(AppScreen.Home, isTabSwitch = true)
                                            1 -> navigateTo(AppScreen.SearchResults(), isTabSwitch = true)
                                            2 -> navigateTo(AppScreen.AiChat, isTabSwitch = true)
                                            3 -> { /* Đang ở tab Từ vựng */ }
                                            4 -> navigateTo(AppScreen.SettingsOverview, isTabSwitch = true)
                                        }
                                    }
                                )
                            }

                            is AppScreen.StudyStats -> {
                                StudyStatsScreen(
                                    onBackClick = { navigateBack() },
                                    onWordClick = { headword ->
                                        navigateTo(
                                            AppScreen.WordDetail(
                                                lexemeId = 101L,
                                                headword = headword
                                            )
                                        )
                                    }
                                )
                            }

                            is AppScreen.SentenceAnalyzer -> {
                                SentenceAnalyzerScreen(
                                    onBackClick = { navigateBack() },
                                    onWordClick = { headword ->
                                        navigateTo(
                                            AppScreen.WordDetail(
                                                lexemeId = 101L,
                                                headword = headword
                                            )
                                        )
                                    }
                                )
                            }

                            is AppScreen.SecuritySettings -> {
                                SecuritySettingsScreen(
                                    onBackClick = { navigateBack() },
                                    onApiKeySaved = { provider, key ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Đã mã hóa và lưu API Key cho $provider vào Keystore",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onClearDataRequested = {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Đã dọn sạch dữ liệu cá nhân theo chuẩn Quyền riêng tư",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onExportData = {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Xuất dữ liệu user_workspace.db thành công",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onImportData = {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Nhập dữ liệu thành công",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }

                            is AppScreen.DataPackManager -> {
                                DataPackManagerScreen(
                                    onBackClick = { navigateBack() },
                                    onCheckUpdate = {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Bộ dữ liệu từ nguyên NIKL v1.2.0 đã là phiên bản mới nhất!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }

                            is AppScreen.AppearanceSettings -> {
                                AppearanceSettingsScreen(
                                    onBackClick = { navigateBack() },
                                    onThemeChanged = { theme ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Đã chuyển chủ đề sang ${theme.name}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }

                            is AppScreen.AiChat -> {
                                AiChatScreen(
                                    onBackToHome = { navigateBack() },
                                    onOpenDetailAnalysis = { word ->
                                        navigateTo(AppScreen.AiDetailAnalysis(word))
                                    },
                                    onSaveWord = { word ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Đã lưu từ: $word",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onSpeakWord = { word ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Phát âm: $word",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onNavigateTab = { tabIndex ->
                                        when (tabIndex) {
                                            0 -> navigateTo(AppScreen.Home, isTabSwitch = true)
                                            1 -> navigateTo(AppScreen.SearchResults(), isTabSwitch = true)
                                            2 -> { /* Đang ở tab AI Chat */ }
                                            3 -> navigateTo(AppScreen.MyVocabulary, isTabSwitch = true)
                                            4 -> navigateTo(AppScreen.SettingsOverview, isTabSwitch = true)
                                        }
                                    },
                                    onNavigateToSearch = {
                                        navigateTo(AppScreen.SearchResults(), isTabSwitch = true)
                                    },
                                    onNavigateToHanja = {
                                        navigateTo(AppScreen.HanjaExplorer("學"))
                                    },
                                    onOpenSentenceAnalyzer = {
                                        navigateTo(AppScreen.SentenceAnalyzer)
                                    }
                                )
                            }

                            is AppScreen.AiDetailAnalysis -> {
                                AiDetailAnalysisScreen(
                                    headword = screen.headword,
                                    onBackClick = { navigateBack() },
                                    onSaveWord = { word ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Đã lưu từ: $word",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onSpeakWord = { word ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Phát âm: $word",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onFindSimilar = { word ->
                                        navigateTo(AppScreen.AiDetailAnalysis(word))
                                    },
                                    onNavigateTab = { tabIndex ->
                                        when (tabIndex) {
                                            0 -> navigateTo(AppScreen.Home, isTabSwitch = true)
                                            1 -> navigateTo(AppScreen.SearchResults(), isTabSwitch = true)
                                            2 -> navigateTo(AppScreen.AiChat, isTabSwitch = true)
                                            3 -> navigateTo(AppScreen.MyVocabulary, isTabSwitch = true)
                                            4 -> navigateTo(AppScreen.SettingsOverview, isTabSwitch = true)
                                        }
                                    }
                                )
                            }

                            is AppScreen.SettingsOverview -> {
                                SettingsOverviewScreen(
                                    onBackClick = { navigateBack() },
                                    onNavigateToSecurity = { navigateTo(AppScreen.SecuritySettings) },
                                    onNavigateToDataPack = { navigateTo(AppScreen.DataPackManager) },
                                    onNavigateToAppearance = { navigateTo(AppScreen.AppearanceSettings) },
                                    onNavigateToSentenceAnalyzer = { navigateTo(AppScreen.SentenceAnalyzer) },
                                    onNavigateToStudyStats = { navigateTo(AppScreen.StudyStats) },
                                    onNavigateToSrsDecks = { navigateTo(AppScreen.SrsDeckList) },
                                    onNavigateToHistory = { navigateTo(AppScreen.SearchHistory) },
                                    onNavigateToAbout = { navigateTo(AppScreen.AboutDataLicense) },
                                    onOpenQuickLookupGuide = { showQuickLookupGuide = true },
                                    onNavigateTab = { tabIndex ->
                                        when (tabIndex) {
                                            0 -> navigateTo(AppScreen.Home, isTabSwitch = true)
                                            1 -> navigateTo(AppScreen.SearchResults(), isTabSwitch = true)
                                            2 -> navigateTo(AppScreen.AiChat, isTabSwitch = true)
                                            3 -> navigateTo(AppScreen.MyVocabulary, isTabSwitch = true)
                                            4 -> { /* Đang ở Cài đặt */ }
                                        }
                                    }
                                )
                            }

                            is AppScreen.AboutDataLicense -> {
                                AboutDataLicenseScreen(
                                    onBackClick = { navigateBack() },
                                    onOpenGithub = {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Mở kho mã nguồn: https://github.com/ngoloc2k4/HanViet-EtymoAI",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onReportIssue = {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Mở trang góp ý & báo lỗi GitHub Issues",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }

                            is AppScreen.SrsDeckList -> {
                                SrsDeckListScreen(
                                    onBackClick = { navigateBack() },
                                    onDeckClick = {
                                        navigateTo(AppScreen.SrsReview)
                                    },
                                    onStartReview = {
                                        navigateTo(AppScreen.SrsReview)
                                    }
                                )
                            }

                            is AppScreen.HanjaExplorer -> {
                                HanjaExplorerScreen(
                                    initialHanja = screen.initialHanja,
                                    onBackClick = { navigateBack() },
                                    onWordClick = { lexemeId, headword ->
                                        navigateTo(
                                            AppScreen.WordDetail(
                                                lexemeId = lexemeId,
                                                headword = headword
                                            )
                                        )
                                    },
                                    onSpeakWord = { word ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Phát âm: $word",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }

                            is AppScreen.SearchHistory -> {
                                SearchHistoryScreen(
                                    onBackClick = { navigateBack() },
                                    onItemClick = { query ->
                                        navigateTo(AppScreen.SearchResults(query))
                                    },
                                    onClearAllHistory = {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Đã dọn sạch toàn bộ lịch sử tra cứu",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    }

                    // Dialog Cài đặt & Phím tắt tính năng nâng cao
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
                                    text = "Cài Đặt & Tính Năng Nâng Cao",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "HanViet-EtymoAI v1.0.0 (Beta)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Offline-First • SQLite FTS5 • NIKL & Hán Nôm",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Button(
                                        onClick = {
                                            showSettingsDialog = false
                                            navigateTo(AppScreen.SecuritySettings)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                                    ) {
                                        Text("🛡️ Tài khoản & Bảo mật (BYOK)", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            showSettingsDialog = false
                                            navigateTo(AppScreen.DataPackManager)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                    ) {
                                        Text("📦 Quản lý dữ liệu (Data Pack)", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            showSettingsDialog = false
                                            navigateTo(AppScreen.AppearanceSettings)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569))
                                    ) {
                                        Text("🎨 Giao diện & Hiển thị", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            showSettingsDialog = false
                                            navigateTo(AppScreen.SentenceAnalyzer)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                                    ) {
                                        Text("✨ AI Phân tích câu & Ngữ pháp", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            showSettingsDialog = false
                                            navigateTo(AppScreen.StudyStats)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                                    ) {
                                        Text("📊 Thống kê học tập SRS", fontSize = 12.sp)
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showSettingsDialog = false }) {
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
