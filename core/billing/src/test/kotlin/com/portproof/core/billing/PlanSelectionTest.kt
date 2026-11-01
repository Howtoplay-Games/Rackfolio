package com.portproof.core.billing

import org.junit.Test
import java.time.Period
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlanSelectionTest {

    private fun phase(price: Long, period: String, recurring: Boolean = true, formatted: String = "€$price") =
        PhaseSnapshot(formatted, price, "EUR", period, infiniteRecurring = recurring)

    @Test
    fun picksTrialOfferWhenEligibleAndSortsMonthlyFirst() {
        val plans = PlanSelection.select(
            listOf(
                OfferSnapshot("yearly", null, "y-base", listOf(phase(39_990_000, "P1Y"))),
                OfferSnapshot("monthly", null, "m-base", listOf(phase(4_990_000, "P1M"))),
                OfferSnapshot("monthly", "trial", "m-trial", listOf(phase(0, "P7D", recurring = false), phase(4_990_000, "P1M"))),
            ),
        )
        assertEquals(listOf("monthly", "yearly"), plans.map { it.basePlanId })
        assertEquals("m-trial", plans[0].offerToken)
        assertEquals(Period.ofDays(7), plans[0].freeTrial)
        assertEquals(4_990_000, plans[0].priceMicros)
        assertNull(plans[1].freeTrial)
        assertEquals(true, plans[1].isYearly)
    }

    @Test
    fun offersWithoutARecurringPhaseAreSkipped() {
        val plans = PlanSelection.select(listOf(OfferSnapshot("odd", null, "t", listOf(phase(100, "P1M", recurring = false)))))
        assertEquals(emptyList(), plans)
    }

    @Test
    fun yearlySavingComparesTwelveMonthsInTheSameCurrency() {
        val monthly = PlanSelection.select(listOf(OfferSnapshot("monthly", null, "m", listOf(phase(5_000_000, "P1M"))))).single()
        val yearly = PlanSelection.select(listOf(OfferSnapshot("yearly", null, "y", listOf(phase(40_000_000, "P1Y"))))).single()
        assertEquals(33, PlanSelection.yearlySavingPercent(monthly, yearly))
        assertNull(PlanSelection.yearlySavingPercent(monthly, yearly.copy(currencyCode = "USD")))
        assertNull(PlanSelection.yearlySavingPercent(monthly, yearly.copy(priceMicros = 70_000_000)))
        assertNull(PlanSelection.yearlySavingPercent(null, yearly))
    }
}
