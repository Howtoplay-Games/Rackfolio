package com.portproof.feature.search

import androidx.lifecycle.SavedStateHandle
import com.portproof.core.testing.MainDispatcherRule
import com.portproof.core.testing.TestProjectRepository
import com.portproof.core.testing.testProject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProjectRepository(
        listOf(testProject(id = "1", name = "Hospital access control"), testProject(id = "2", name = "Depot CCTV")),
    )

    @Test
    fun blankQueryIsIdleAndTypingSearches() = runTest {
        val handle = SavedStateHandle()
        val vm = SearchViewModel(handle, repository, Clock.fixed(Instant.parse("2026-11-01T10:00:00Z"), ZoneOffset.UTC))
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        assertEquals(SearchUiState.Idle, vm.uiState.value)

        vm.onQueryChange("cctv")
        advanceUntilIdle()
        val results = assertIs<SearchUiState.Results>(vm.uiState.value)
        assertEquals(listOf("2"), results.projects.map { it.id.value })
        assertEquals("cctv", handle.get<String>("search.query"))
    }
}
