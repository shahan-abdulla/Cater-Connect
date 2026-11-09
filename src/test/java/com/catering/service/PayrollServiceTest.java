package com.catering.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PayrollServiceTest {

    private final PayrollService service = new PayrollService();

    @Test
    void presentGetsFullWage() {
        assertEquals(600.0, service.calculateWage(600, "PRESENT"), 0.001);
    }

    @Test
    void lateGetsSeventyFivePercent() {
        assertEquals(450.0, service.calculateWage(600, "LATE"), 0.001);
    }

    @Test
    void lateRoundsToTwoDecimals() {
        assertEquals(374.25, service.calculateWage(499, "LATE"), 0.001);
    }

    @Test
    void absentGetsNothing() {
        assertEquals(0.0, service.calculateWage(600, "ABSENT"), 0.001);
    }

    @Test
    void pendingGetsNothing() {
        assertEquals(0.0, service.calculateWage(600, "PENDING"), 0.001);
    }
}