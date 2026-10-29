package com.hanviet.etymoai.ui.quicklookup

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.hanviet.etymoai.data.db.core.CoreDatabaseHelper
import com.hanviet.etymoai.data.db.workspace.WorkspaceDatabase
import com.hanviet.etymoai.data.repository.UserWorkspaceRepository
import com.hanviet.etymoai.domain.etymology.DefaultEtymologyRepository
import com.hanviet.etymoai.domain.search.SearchPipeline
import com.hanviet.etymoai.domain.srs.SrsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class QuickLookupViewModelTest {

    private lateinit var coreHelper: CoreDatabaseHelper
    private lateinit var wsDb: WorkspaceDatabase
    private lateinit var searchPipeline: SearchPipeline
    private lateinit var etymologyRepo: DefaultEtymologyRepository
    private lateinit var userWsRepo: UserWorkspaceRepository
    private lateinit var srsRepo: SrsRepository
    private lateinit var viewModel: QuickLookupViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = File("src/main/assets/dictionary_core.db")
        assertTrue("dictionary_core.db phải tồn tại", dbFile.exists())
        coreHelper = CoreDatabaseHelper.fromFile(dbFile)

        wsDb = WorkspaceDatabase.createInMemory(context)
        searchPipeline = SearchPipeline(coreHelper)
        etymologyRepo = DefaultEtymologyRepository(coreHelper)
        userWsRepo = UserWorkspaceRepository(wsDb)
        srsRepo = SrsRepository(wsDb, etymologyRepo)

        viewModel = QuickLookupViewModel(
            searchPipeline = searchPipeline,
            etymologyRepository = etymologyRepo,
            userWorkspaceRepository = userWsRepo,
            srsRepository = srsRepo,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        coreHelper.close()
        wsDb.close()
    }

    @Test
    fun testLookup_exactSinoKoreanWord() = runTest(testDispatcher) {
        viewModel.lookup("기분")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.result)
        assertEquals("기분", state.result!!.headwordNfc)
        assertNotNull(state.etymology)
        assertEquals(2, state.etymology!!.segments.size)
        assertEquals("氣", state.etymology!!.segments[0].hanjaChar)
        assertEquals("分", state.etymology!!.segments[1].hanjaChar)
        assertFalse(state.isSaved)
    }

    @Test
    fun testLookup_andToggleSave() = runTest(testDispatcher) {
        viewModel.lookup("학생")
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertNotNull(state.result)
        assertFalse(state.isSaved)

        // Lưu vào sổ tay
        viewModel.toggleSave()
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertTrue("Sau khi lưu thì isSaved phải là true", state.isSaved)
        assertEquals(1, userWsRepo.getWordCount())

        // Bỏ lưu khỏi sổ tay
        viewModel.toggleSave()
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertFalse("Sau khi bỏ lưu thì isSaved phải là false", state.isSaved)
        assertEquals(0, userWsRepo.getWordCount())
    }

    @Test
    fun testLookup_nonExistentWord() = runTest(testDispatcher) {
        viewModel.lookup("zzzzzzzz")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.result)
        assertNull(state.etymology)
    }
}
