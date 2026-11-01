package com.portproof.core.model

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ProjectValidatorTest {

    private fun valid(input: ProjectInput): ProjectDraft =
        assertIs<ProjectValidationResult.Valid>(ProjectValidator.validate(input)).draft

    private fun errors(input: ProjectInput): Map<ProjectField, FieldError> =
        assertIs<ProjectValidationResult.Invalid>(ProjectValidator.validate(input)).errors

    @Test
    fun `name is required`() {
        assertEquals(FieldError.Required, errors(ProjectInput(name = "   \n\t "))[ProjectField.NAME])
    }

    @Test
    fun `values are trimmed and blank optional fields become null`() {
        val draft = valid(ProjectInput(name = "  Main   rack  ", reference = "  ", customerName = " Acme Ltd "))
        assertEquals("Main rack", draft.name)
        assertNull(draft.reference)
        assertEquals("Acme Ltd", draft.customerName)
        assertNull(draft.siteAddress)
    }

    @Test
    fun `control and bidi characters are removed`() {
        val draft = valid(ProjectInput(name = "Site\u0000 A\u202E", description = "Line 1\r\nLine 2\u0007  "))
        assertEquals("Site A", draft.name)
        assertEquals("Line 1\nLine 2", draft.description)
    }

    @Test
    fun `multi-line address keeps line breaks`() {
        val draft = valid(ProjectInput(name = "P", siteAddress = "1 High Street  \nLondon\n"))
        assertEquals("1 High Street\nLondon", draft.siteAddress)
    }

    @Test
    fun `too long values are rejected using code point length`() {
        val emojiName = "\uD83D\uDD0C".repeat(ProjectRules.NAME_MAX_LENGTH) // 120 plugs = 240 UTF-16 units
        valid(ProjectInput(name = emojiName))
        val tooLong = errors(ProjectInput(name = "x".repeat(ProjectRules.NAME_MAX_LENGTH + 1)))
        assertEquals(FieldError.TooLong(ProjectRules.NAME_MAX_LENGTH), tooLong[ProjectField.NAME])
    }

    @Test
    fun `completion date before start date is rejected`() {
        val result = errors(
            ProjectInput(name = "P", startDate = LocalDate.of(2026, 5, 10), endDate = LocalDate.of(2026, 5, 9)),
        )
        assertEquals(FieldError.EndBeforeStart, result[ProjectField.END_DATE])
        valid(ProjectInput(name = "P", startDate = LocalDate.of(2026, 5, 10), endDate = LocalDate.of(2026, 5, 10)))
    }

    @Test
    fun `status codes are stable`() {
        assertEquals(listOf("PLANNED", "IN_PROGRESS", "ON_HOLD", "COMPLETED"), ProjectStatus.entries.map { it.code })
        assertNull(ProjectStatus.fromCode("UNKNOWN"))
    }
}
