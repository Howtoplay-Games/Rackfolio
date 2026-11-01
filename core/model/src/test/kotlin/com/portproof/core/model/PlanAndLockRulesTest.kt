package com.portproof.core.model

import org.junit.Test
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlanAndLockRulesTest {

    @Test
    fun freePlanAllowsThreeActiveProjectsAndProHasNoLimit() {
        val free = AppPlan.Free()
        assertEquals(3, free.activeProjectLimit)
        assertTrue(PlanRules.canAddActiveProject(free, activeCount = 2))
        assertFalse(PlanRules.canAddActiveProject(free, activeCount = 3))
        assertNull(AppPlan.Pro.activeProjectLimit)
        assertTrue(PlanRules.canAddActiveProject(AppPlan.Pro, activeCount = 500))
    }

    @Test
    fun pinMustBeSixDigitsAndNotTrivial() {
        assertNull(PinRules.validate("246813"))
        assertNull(PinRules.validate("135792"))
        assertEquals(PinError.FORMAT, PinRules.validate("24681"))
        assertEquals(PinError.FORMAT, PinRules.validate("24681a"))
        assertEquals(PinError.FORMAT, PinRules.validate("２４６８１３")) // full-width digits
        assertEquals(PinError.TOO_SIMPLE, PinRules.validate("000000"))
        assertEquals(PinError.TOO_SIMPLE, PinRules.validate("123456"))
        assertEquals(PinError.TOO_SIMPLE, PinRules.validate("987654"))
    }

    @Test
    fun lockoutStartsAfterFiveWrongPinsAndDoublesUpToSixteenMinutes() {
        assertNull(LockoutPolicy.waitAfter(4))
        assertEquals(Duration.ofSeconds(30), LockoutPolicy.waitAfter(5))
        assertEquals(Duration.ofSeconds(60), LockoutPolicy.waitAfter(6))
        assertEquals(Duration.ofMinutes(16), LockoutPolicy.waitAfter(10))
        assertEquals(Duration.ofMinutes(16), LockoutPolicy.waitAfter(40))
    }
}
