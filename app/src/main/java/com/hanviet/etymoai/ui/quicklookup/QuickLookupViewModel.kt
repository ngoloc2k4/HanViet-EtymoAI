package com.hanviet.etymoai.ui.quicklookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanviet.etymoai.data.repository.UserWorkspaceRepository
import com.hanviet.etymoai.domain.etymology.EtymologyRepository
import com.hanviet.etymoai.domain.etymology.WordEtymology
import com.hanviet.etymoai.domain.search.SearchPipeline
import com.hanviet.etymoai.domain.search.SearchResult
import com.hanviet.etymoai.domain.srs.SrsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuickLookupUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val result: SearchResult? = null,
    val etymology: WordEtymology? = null,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class QuickLookupViewModel(
    private val searchPipeline: SearchPipeline,
    private val etymologyRepository: EtymologyRepository,
    private val userWorkspaceRepository: UserWorkspaceRepository,
    private val srsRepository: SrsRepository? = null,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
    coroutineScope: kotlinx.coroutines.CoroutineScope? = null
) : ViewModel() {

    private val scope = coroutineScope ?: viewModelScope

    private val _uiState = MutableStateFlow(QuickLookupUiState())
    val uiState: StateFlow<QuickLookupUiState> = _uiState.asStateFlow()

    fun lookup(rawText: String) {
        val query = rawText.trim()
        if (query.isEmpty()) {
            _uiState.value = QuickLookupUiState(query = "", isLoading = false)
            return
        }

        _uiState.value = _uiState.value.copy(query = query, isLoading = true, errorMessage = null)

        scope.launch(ioDispatcher) {
            try {
                val results = searchPipeline.search(query, limit = 5)
                if (results.isNotEmpty()) {
                    val top = results.first()
                    val etymology = etymologyRepository.getEtymologyForWord(top.lexemeId)
                        ?: etymologyRepository.getEtymologyForWordByHeadword(top.headwordNfc)
                    val existing = userWorkspaceRepository.getSavedWordByStableKey(top.stableKey)

                    _uiState.value = QuickLookupUiState(
                        query = query,
                        isLoading = false,
                        result = top,
                        etymology = etymology,
                        isSaved = existing != null
                    )
                } else {
                    _uiState.value = QuickLookupUiState(
                        query = query,
                        isLoading = false,
                        result = null,
                        etymology = null,
                        isSaved = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = QuickLookupUiState(
                    query = query,
                    isLoading = false,
                    errorMessage = e.message ?: "Đã xảy ra lỗi khi tra cứu nhanh."
                )
            }
        }
    }

    fun toggleSave() {
        val current = _uiState.value
        val res = current.result ?: return

        scope.launch(ioDispatcher) {
            val existing = userWorkspaceRepository.getSavedWordByStableKey(res.stableKey)
            if (existing != null) {
                userWorkspaceRepository.removeWord(existing.id)
                _uiState.value = _uiState.value.copy(isSaved = false)
            } else {
                val wordId = userWorkspaceRepository.saveWord(
                    stableKey = res.stableKey,
                    headword = res.headwordNfc,
                    meaning = res.primaryMeaningVi,
                    phonetic = res.phoneticVi,
                    originType = res.originType,
                    isFavorite = true
                )
                srsRepository?.createCardForSavedWord(wordId)
                _uiState.value = _uiState.value.copy(isSaved = true)
            }
        }
    }
}
