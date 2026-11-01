package com.portproof.core.data

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** A clock tests can move forward explicitly. */
class TestClock(
    var now: Instant = Instant.parse("2026-11-01T10:00:00Z"),
    private val zoneId: ZoneId = ZoneOffset.UTC,
) : Clock() {
    override fun getZone(): ZoneId = zoneId
    override fun withZone(zone: ZoneId): Clock = TestClock(now, zone)
    override fun instant(): Instant = now

    fun advanceMinutes(minutes: Long) {
        now = now.plusSeconds(minutes * 60)
    }
}
