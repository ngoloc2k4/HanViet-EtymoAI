package com.hanviet.etymoai.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.hanviet.etymoai.data.translation.UnifiedTranslationService
import com.hanviet.etymoai.di.DatabaseModule
import com.hanviet.etymoai.ui.theme.HanVietTheme
import com.hanviet.etymoai.ui.theme.ThemeModeOption
import com.hanviet.etymoai.ui.theme.ThemePreferences
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
import com.hanviet.etymoai.ui.setup.FirstRunSetupScreen
import com.hanviet.etymoai.ui.vocabulary.MyVocabularyScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Trạng thái màn hình điều hướng của ứng dụng
 */
sealed interface AppScreen {
    data object Home : AppScreen
    data class SearchResults(val query: String = "") : AppScreen
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

private fun serializeScreen(screen: AppScreen): String = when (screen) {
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

private fun deserializeScreen(str: String): AppScreen {
    val parts = str.split("|")
    return when (parts[0]) {
        "Home" -> AppScreen.Home
        "SearchResults" -> AppScreen.SearchResults(parts.getOrElse(1) { "" })
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

class MainActivity : ComponentActivity() {

    private val coreDbHelper by lazy { DatabaseModule.provideCoreDatabaseHelper(this) }
    private val workspaceDb by lazy { DatabaseModule.provideWorkspaceDatabase(this) }
    private val etymologyRepository by lazy { DatabaseModule.provideEtymologyRepository(coreDbHelper) }
    private val searchPipeline by lazy { DatabaseModule.provideSearchPipeline(coreDbHelper) }
    private val srsRepository by lazy { DatabaseModule.provideSrsRepository(workspaceDb, etymologyRepository) }
    private val userWorkspaceRepository by lazy { DatabaseModule.provideUserWorkspaceRepository(workspaceDb) }
    private val apiKeyStorage by lazy { DatabaseModule.provideApiKeyStorage(this) }
    private val translationService by lazy { UnifiedTranslationService(apiKeyStorage) }
    private val ttsManager by lazy { com.hanviet.etymoai.util.TtsManager.getInstance(applicationContext) }

    override fun onDestroy() {
        ttsManager.shutdown()
        super.onDestroy()
    }

    private fun isFirstRunSetupNeeded(): Boolean {
        val prefs = getSharedPreferences("hanviet_setup", android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean("is_first_run", true)
    }

    private fun markFirstRunCompleted() {
        val prefs = getSharedPreferences("hanviet_setup", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_first_run", false).apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kích hoạt sớm bộ đọc TTS tiếng Hàn trong nền để sẵn sàng phát âm ngay khi người dùng chạm
        ttsManager

        setContent {
            var currentThemeMode by remember {
                mutableStateOf(ThemePreferences.getThemeMode(this@MainActivity))
            }

            var showFirstRunSetup by rememberSaveable {
                mutableStateOf(isFirstRunSetupNeeded())
            }

            if (showFirstRunSetup) {
                HanVietTheme(themeMode = currentThemeMode) {
                    FirstRunSetupScreen(
                        onSetupComplete = {
                            markFirstRunCompleted()
                            showFirstRunSetup = false
                        }
                    )
                }
                return@setContent
            }

            HanVietTheme(themeMode = currentThemeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val backStackSaver = Saver<androidx.compose.runtime.snapshots.SnapshotStateList<AppScreen>, ArrayList<String>>(
                        save = { list ->
                            val arrayList = ArrayList<String>(list.size)
                            list.forEach { screen ->
                                arrayList.add(serializeScreen(screen))
                            }
                            arrayList
                        },
                        restore = { savedList ->
                            mutableStateListOf<AppScreen>().apply {
                                val restored = savedList.map { deserializeScreen(it) }
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

                    // Thu thập lịch sử tìm kiếm và kho từ vựng thực tế từ Workspace Database
                    val recentSearches by userWorkspaceRepository.getSearchHistoryFlow(limit = 10)
                        .collectAsState(initial = emptyList())
                    val savedWordsWithTags by userWorkspaceRepository.getAllSavedWordsFlow()
                        .collectAsState(initial = emptyList())
                    val savedWordsCount = savedWordsWithTags.size
                    val dueCards by srsRepository.getDueCardsFlow()
                        .collectAsState(initial = emptyList())
                    val dueCardsCount = dueCards.size

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

                    val currentTabIndex = when (currentScreen) {
                        is AppScreen.Home -> 0
                        is AppScreen.SearchResults, is AppScreen.WordDetail, is AppScreen.HanjaExplorer, is AppScreen.SearchHistory -> 1
                        is AppScreen.AiChat, is AppScreen.AiDetailAnalysis, is AppScreen.SentenceAnalyzer -> 2
                        is AppScreen.MyVocabulary, is AppScreen.SrsReview, is AppScreen.SrsDeckList, is AppScreen.StudyStats -> 3
                        is AppScreen.SettingsOverview, is AppScreen.SecuritySettings, is AppScreen.DataPackManager, is AppScreen.AppearanceSettings, is AppScreen.AboutDataLicense -> 4
                    }

                    val onTabSelected: (Int) -> Unit = { tabIndex ->
                        when (tabIndex) {
                            0 -> navigateTo(AppScreen.Home, isTabSwitch = true)
                            1 -> navigateTo(AppScreen.SearchResults(searchQuery.trim()), isTabSwitch = true)
                            2 -> navigateTo(AppScreen.AiChat, isTabSwitch = true)
                            3 -> navigateTo(AppScreen.MyVocabulary, isTabSwitch = true)
                            4 -> navigateTo(AppScreen.SettingsOverview, isTabSwitch = true)
                        }
                    }

                    Scaffold(
                        bottomBar = {
                            AppBottomNavigationBar(
                                selectedTab = currentTabIndex,
                                onTabSelected = onTabSelected
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Crossfade(
                                targetState = currentScreen,
                                animationSpec = tween(durationMillis = 150),
                                label = "ScreenCrossfade"
                            ) { screen ->
                                when (screen) {
                            is AppScreen.Home -> {
                                HomeScreen(
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { searchQuery = it },
                                    searchResults = searchResults,
                                    recentSearches = recentSearches,
                                    onClearRecentSearches = {
                                        scope.launch(Dispatchers.IO) {
                                            userWorkspaceRepository.clearSearchHistory()
                                        }
                                    },
                                    onTranslate = { text, engine, src, tgt ->
                                        translationService.translate(text, engine, src, tgt)
                                    },
                                    onNavigateToSentenceAnalyzer = {
                                        navigateTo(AppScreen.SentenceAnalyzer)
                                    },
                                    onSearchResultClick = { result ->
                                        scope.launch(Dispatchers.IO) {
                                            userWorkspaceRepository.recordSearch(result.headwordNfc)
                                        }
                                        navigateTo(
                                            AppScreen.WordDetail(
                                                lexemeId = result.lexemeId,
                                                headword = result.headwordNfc
                                            )
                                        )
                                    },
                                    onWordClick = { word ->
                                        scope.launch(Dispatchers.IO) {
                                            userWorkspaceRepository.recordSearch(word)
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
                                        navigateTo(AppScreen.SearchResults(searchQuery.trim()), isTabSwitch = true)
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
                                    },
                                    dueCardsCount = dueCardsCount,
                                    onNavigateToAbout = {
                                        navigateTo(AppScreen.AboutDataLicense)
                                    },
                                    onNavigateToHistory = {
                                        navigateTo(AppScreen.SearchHistory)
                                    },
                                    onDeleteRecentSearch = { historyId ->
                                        scope.launch(Dispatchers.IO) {
                                            userWorkspaceRepository.deleteSearchHistoryItem(historyId)
                                        }
                                    },
                                    onBookmarkWord = { word ->
                                        scope.launch(Dispatchers.IO) {
                                            userWorkspaceRepository.saveWord(
                                                stableKey = "saved_${word}_${System.currentTimeMillis()}",
                                                headword = word,
                                                meaning = "Từ đã lưu từ lịch sử",
                                                isFavorite = true
                                            )
                                        }
                                    },
                                    onSpeakWord = { word ->
                                        ttsManager.speak(word, ThemePreferences.getTtsSpeed(this@MainActivity))
                                    }
                                )
                            }

                            is AppScreen.SearchResults -> {
                                SearchResultsScreen(
                                    initialQuery = screen.query,
                                    searchPipeline = searchPipeline,
                                    onRecordHistory = { query ->
                                        scope.launch(Dispatchers.IO) {
                                            userWorkspaceRepository.recordSearch(query)
                                        }
                                    },
                                    onWordClick = { lexemeId, headword ->
                                        scope.launch(Dispatchers.IO) {
                                            userWorkspaceRepository.recordSearch(headword)
                                        }
                                        navigateTo(
                                            AppScreen.WordDetail(
                                                lexemeId = lexemeId,
                                                headword = headword
                                            )
                                        )
                                    },
                                    onBackClick = { navigateBack() },
                                    onSpeakWord = { word ->
                                        ttsManager.speak(word, ThemePreferences.getTtsSpeed(this@MainActivity))
                                    },
                                    onNavigateToAi = { query ->
                                        navigateTo(AppScreen.AiChat, isTabSwitch = true)
                                    },
                                    onNavigateTab = onTabSelected
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
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                val etymo = etymologyRepository.getEtymologyForWordByHeadword(word)
                                                userWorkspaceRepository.saveWord(
                                                    stableKey = "word_${word.hashCode()}",
                                                    headword = word,
                                                    meaning = etymo?.meaningVi ?: "Từ vựng tiếng Hàn",
                                                    phonetic = etymo?.phoneticVi,
                                                    originType = etymo?.originType?.name ?: "SINO_KOREAN",
                                                    isFavorite = true
                                                )
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Đã lưu từ vào kho từ vựng: $word",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onSpeakWord = { word ->
                                        ttsManager.speak(word, ThemePreferences.getTtsSpeed(this@MainActivity))
                                    },
                                    onNavigateTab = onTabSelected
                                )
                            }

                            is AppScreen.SrsReview -> {
                                // Tự động enroll toàn bộ từ đã lưu vào SRS nếu chưa được tạo thẻ
                                LaunchedEffect(savedWordsWithTags) {
                                    withContext(Dispatchers.IO) {
                                        srsRepository.enrollAllSavedWords()
                                    }
                                }

                                val realCards = remember(dueCards, savedWordsWithTags) {
                                    if (dueCards.isNotEmpty()) {
                                        dueCards
                                    } else {
                                        // Nếu chưa có thẻ đến hạn hôm nay, hiển thị toàn bộ từ đã lưu để ôn tập
                                        savedWordsWithTags.mapIndexed { index, item ->
                                            SrsFlashcardData(
                                                card = SrsCard(
                                                    cardId = (index + 1).toLong(),
                                                    savedWordId = item.word.id,
                                                    repetitions = 0,
                                                    intervalDays = 0,
                                                    dueTimestamp = System.currentTimeMillis()
                                                ),
                                                word = item.word,
                                                hanjaSegments = emptyList()
                                            )
                                        }
                                    }
                                }

                                SrsReviewScreen(
                                    cards = realCards,
                                    onRatingSelected = { card, rating ->
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                srsRepository.processReview(card, rating)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    },
                                    onBackClick = { navigateBack() },
                                    onNavigateToStats = { navigateTo(AppScreen.StudyStats) },
                                    onSpeakWord = { word ->
                                        ttsManager.speak(word, ThemePreferences.getTtsSpeed(this@MainActivity))
                                    },
                                    onNavigateTab = onTabSelected
                                )
                            }

                            is AppScreen.MyVocabulary -> {
                                MyVocabularyScreen(
                                    userWorkspaceRepository = userWorkspaceRepository,
                                    onWordClick = { headword ->
                                        scope.launch(Dispatchers.IO) {
                                            val etymo = try {
                                                etymologyRepository.getEtymologyForWordByHeadword(headword)
                                            } catch (e: Exception) {
                                                null
                                            }
                                            withContext(Dispatchers.Main) {
                                                navigateTo(
                                                    AppScreen.WordDetail(
                                                        lexemeId = etymo?.lexemeId ?: 1L,
                                                        headword = headword
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    onBackClick = { navigateBack() },
                                    onSpeakWord = { word ->
                                        ttsManager.speak(word, ThemePreferences.getTtsSpeed(this@MainActivity))
                                    },
                                    onStartSrsReview = {
                                        navigateTo(AppScreen.SrsDeckList)
                                    },
                                    onNavigateTab = onTabSelected
                                )
                            }

                            is AppScreen.StudyStats -> {
                                StudyStatsScreen(
                                    userWorkspaceRepository = userWorkspaceRepository,
                                    srsRepository = srsRepository,
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
                                        currentThemeMode = theme
                                        ThemePreferences.setThemeMode(this@MainActivity, theme)
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Đã chuyển chủ đề sang ${theme.titleVi}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }

                            is AppScreen.AiChat -> {
                                AiChatScreen(
                                    etymologyRepository = etymologyRepository,
                                    searchPipeline = searchPipeline,
                                    apiKeyStorage = apiKeyStorage,
                                    onBackToHome = { navigateBack() },
                                    onOpenDetailAnalysis = { word ->
                                        navigateTo(AppScreen.AiDetailAnalysis(word))
                                    },
                                    onOpenSecuritySettings = {
                                        navigateTo(AppScreen.SecuritySettings)
                                    },
                                    onSaveWord = { word ->
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                val etymo = etymologyRepository.getEtymologyForWordByHeadword(word)
                                                userWorkspaceRepository.saveWord(
                                                    stableKey = "word_${word.hashCode()}",
                                                    headword = word,
                                                    meaning = etymo?.meaningVi ?: "Từ vựng tiếng Hàn",
                                                    phonetic = etymo?.phoneticVi,
                                                    originType = etymo?.originType?.name ?: "SINO_KOREAN",
                                                    isFavorite = true
                                                )
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Đã lưu từ: $word",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onSpeakWord = { word ->
                                        ttsManager.speak(word, ThemePreferences.getTtsSpeed(this@MainActivity))
                                    },
                                    onNavigateTab = onTabSelected,
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
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                val etymo = etymologyRepository.getEtymologyForWordByHeadword(word)
                                                userWorkspaceRepository.saveWord(
                                                    stableKey = "word_${word.hashCode()}",
                                                    headword = word,
                                                    meaning = etymo?.meaningVi ?: "Từ vựng tiếng Hàn",
                                                    phonetic = etymo?.phoneticVi,
                                                    originType = etymo?.originType?.name ?: "SINO_KOREAN",
                                                    isFavorite = true
                                                )
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Đã lưu từ: $word",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onSpeakWord = { word ->
                                        ttsManager.speak(word, ThemePreferences.getTtsSpeed(this@MainActivity))
                                    },
                                    onFindSimilar = { word ->
                                        navigateTo(AppScreen.AiDetailAnalysis(word))
                                    },
                                    onNavigateTab = onTabSelected
                                )
                            }

                            is AppScreen.SettingsOverview -> {
                                SettingsOverviewScreen(
                                    savedWordsCount = savedWordsCount,
                                    dueCardsCount = dueCardsCount,
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
                                    onNavigateTab = onTabSelected
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
                                    userWorkspaceRepository = userWorkspaceRepository,
                                    srsRepository = srsRepository,
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
                                        ttsManager.speak(word, ThemePreferences.getTtsSpeed(this@MainActivity))
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
                                    },
                                    onSpeakWord = { word ->
                                        ttsManager.speak(word, ThemePreferences.getTtsSpeed(this@MainActivity))
                                    }
                                )
                            }
                        }
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
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Offline-First • SQLite FTS5 • NIKL & Hán Nôm",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

/**
 * Thanh điều hướng đáy đồng bộ toàn ứng dụng (Unified Persistent Bottom Navigation Bar)
 * Cố định ở cấp gốc MainActivity, loại bỏ triệt để hiện tượng nháy màn hình khi đổi tab.
 */
@Composable
private fun AppBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.height(64.dp)
    ) {
        val navItems = listOf(
            Triple("Trang chủ", Icons.Default.Home, 0),
            Triple("Tra cứu", Icons.Default.Search, 1),
            Triple("AI Chat", Icons.Default.Psychology, 2),
            Triple("Từ vựng", Icons.Default.BookmarkBorder, 3),
            Triple("Cài đặt", Icons.Default.Person, 4)
        )

        navItems.forEach { (label, icon, index) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
