package com.portproof.feature.projects.detail

import androidx.lifecycle.SavedStateHandle
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectStatus
import com.portproof.core.testing.MainDispatcherRule
import com.portproof.core.testing.TestActivityRepository
import com.portproof.core.testing.TestProjectRepository
import com.portproof.core.testing.testProject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProjectRepository(listOf(testProject(id = "p1")))
    private val clock = Clock.fixed(Instant.parse("2026-11-01T10:00:00Z"), ZoneOffset.UTC)

    private fun viewModel(id: String = "p1") =
        ProjectDetailViewModel(SavedStateHandle(mapOf("projectId" to id)), repository, TestActivityRepository(), clock)

    @Test
    fun missingProjectIsReportedAsNotFound() = runTest {
        val vm = viewModel("missing")
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        assertEquals(ProjectDetailUiState.NotFound, vm.uiState.value)
    }

    @Test
    fun statusChangeIsPersisted() = runTest {
        val vm = viewModel()
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        vm.setStatus(ProjectStatus.ON_HOLD)
        advanceUntilIdle()
        val state = assertIs<ProjectDetailUiState.Success>(vm.uiState.value)
        assertEquals(ProjectStatus.ON_HOLD, state.project.status)
    }

    @Test
    fun archiveShowsMessageAndDeleteSignalsNavigation() = runTest {
        val vm = viewModel()
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        vm.archive()
        advanceUntilIdle()
        assertEquals(ProjectDetailMessage.Archived, vm.message.value)
        assertTrue(assertIs<ProjectDetailUiState.Success>(vm.uiState.value).project.isArchived)

        vm.delete()
        assertTrue(vm.deleted.value)
        assertEquals(null, repository.getProject(ProjectId("p1")))
    }

    @Test
    fun storageFailureBecomesAMessage() = runTest {
        repository.failWrites = true
        val vm = viewModel()
        vm.archive()
        assertEquals(ProjectDetailMessage.Error, vm.message.value)
    }
}
