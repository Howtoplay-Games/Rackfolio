package com.portproof.feature.projects.editor

import androidx.lifecycle.SavedStateHandle
import com.portproof.core.model.AppPlan
import com.portproof.core.model.FieldError
import com.portproof.core.model.ProjectField
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectStatus
import com.portproof.core.testing.MainDispatcherRule
import com.portproof.core.testing.TestPlanRepository
import com.portproof.core.testing.TestProjectRepository
import com.portproof.core.testing.testProject
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProjectEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProjectRepository()
    private val plans = TestPlanRepository(AppPlan.Pro)

    private fun newEditor(handle: SavedStateHandle = SavedStateHandle()) = ProjectEditorViewModel(handle, repository, plans)

    private fun editorFor(id: String, handle: SavedStateHandle = SavedStateHandle(mapOf("projectId" to id))) =
        ProjectEditorViewModel(handle, repository, plans)

    @Test
    fun savingWithoutNameShowsErrorAndCreatesNothing() = runTest {
        val vm = newEditor()
        vm.save()
        assertEquals(FieldError.Required, vm.errors[ProjectField.NAME])
        assertEquals(EditorMessage.FixFields, vm.message)
        assertTrue(repository.created.isEmpty())
        assertNull(vm.savedProjectId)
    }

    @Test
    fun editingAFieldClearsItsError() = runTest {
        val vm = newEditor()
        vm.save()
        vm.onNameChange("Car park ANPR")
        assertFalse(ProjectField.NAME in vm.errors)
    }

    @Test
    fun validProjectIsCreatedWithNormalisedValues() = runTest {
        val vm = newEditor()
        vm.onNameChange("  Car park   ANPR ")
        vm.onCustomerNameChange("Harbour Council")
        vm.onStatusChange(ProjectStatus.IN_PROGRESS)
        assertTrue(vm.hasUnsavedChanges)

        vm.save()

        val created = repository.created.single()
        assertEquals("Car park ANPR", created.name)
        assertEquals(ProjectStatus.IN_PROGRESS, created.status)
        assertEquals(ProjectId("created-1"), vm.savedProjectId)
        assertFalse(vm.hasUnsavedChanges)
    }

    @Test
    fun completionDateBeforeStartIsRejected() = runTest {
        val vm = newEditor()
        vm.onNameChange("Lobby AV")
        vm.onStartDateChange(LocalDate.of(2026, 11, 10))
        vm.onEndDateChange(LocalDate.of(2026, 11, 1))
        vm.save()
        assertEquals(FieldError.EndBeforeStart, vm.errors[ProjectField.END_DATE])
    }

    @Test
    fun editLoadsExistingProjectWithoutMarkingItDirty() = runTest {
        repository.setProjects(listOf(testProject(id = "p1", name = "Server room")))
        val vm = editorFor("p1")

        assertEquals(EditorLoadState.Ready, vm.loadState)
        assertEquals("Server room", vm.form.name)
        assertFalse(vm.hasUnsavedChanges)

        vm.onNameChange("Server room rack 2")
        assertTrue(vm.hasUnsavedChanges)
        vm.save()
        assertEquals("Server room rack 2", repository.getProject(ProjectId("p1"))?.name)
        assertEquals(ProjectId("p1"), vm.savedProjectId)
    }

    @Test
    fun editingAMissingProjectReportsNotFound() = runTest {
        val vm = editorFor("missing")
        assertEquals(EditorLoadState.NotFound, vm.loadState)
    }

    @Test
    fun storageFailureKeepsTheFormAndReportsIt() = runTest {
        repository.failWrites = true
        val vm = newEditor()
        vm.onNameChange("Gatehouse access control")
        vm.save()
        assertEquals(EditorMessage.SaveFailed, vm.message)
        assertNull(vm.savedProjectId)
        assertEquals("Gatehouse access control", vm.form.name)
        assertFalse(vm.isSaving)
    }

    @Test
    fun formSurvivesProcessDeathAndWinsOverStoredRecord() = runTest {
        repository.setProjects(listOf(testProject(id = "p1", name = "Original")))
        val handle = SavedStateHandle(mapOf("projectId" to "p1"))
        editorFor("p1", handle).onNameChange("Edited before process death")

        val restored = editorFor("p1", handle) // same SavedStateHandle contents, new ViewModel
        assertEquals("Edited before process death", restored.form.name)
        assertTrue(restored.hasUnsavedChanges)
    }

    @Test
    fun freePlanAtItsLimitShowsTheNoticeOnlyForProjectsThatWouldBeActive() = runTest {
        plans.current.value = AppPlan.Free(activeProjectLimit = 1)
        repository.setProjects(listOf(testProject(id = "busy", status = ProjectStatus.IN_PROGRESS)))
        val vm = newEditor()
        advanceUntilIdle()
        assertEquals(1, vm.limitNotice)

        // A project recorded as completed does not count towards the limit.
        vm.onStatusChange(ProjectStatus.COMPLETED)
        assertNull(vm.limitNotice)

        plans.current.value = AppPlan.Pro
        vm.onStatusChange(ProjectStatus.PLANNED)
        advanceUntilIdle()
        assertNull(vm.limitNotice)
    }

    @Test
    fun saveRefusedByThePlanLimitKeepsTheFormAndSaysWhy() = runTest {
        plans.current.value = AppPlan.Free(activeProjectLimit = 1)
        repository.activeLimit = 1
        repository.setProjects(listOf(testProject(id = "busy", status = ProjectStatus.IN_PROGRESS)))
        val vm = newEditor()
        vm.onNameChange("Second site")
        vm.save()
        advanceUntilIdle()
        assertEquals(EditorMessage.LimitReached, vm.message)
        assertNull(vm.savedProjectId)
        assertEquals("Second site", vm.form.name)
        assertTrue(vm.hasUnsavedChanges)
    }
}
