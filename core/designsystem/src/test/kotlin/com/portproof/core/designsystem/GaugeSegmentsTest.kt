package com.portproof.core.designsystem

import com.portproof.core.designsystem.component.gaugeFilledSegments
import org.junit.Test
import kotlin.test.assertEquals

class GaugeSegmentsTest {

    @Test
    fun anyProgressShowsOneSegmentAndOnlyCompletionFillsTheGauge() {
        assertEquals(0, gaugeFilledSegments(0f, 11))
        assertEquals(1, gaugeFilledSegments(0.01f, 11))
        assertEquals(7, gaugeFilledSegments(0.625f, 11)) // reference: 1250 of 2000 kcal
        assertEquals(10, gaugeFilledSegments(0.99f, 11))
        assertEquals(11, gaugeFilledSegments(1f, 11))
    }
}
