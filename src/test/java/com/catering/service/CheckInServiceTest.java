package com.catering.service;

import static org.junit.jupiter.api.Assertions.*;

import com.catering.model.MyShift;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class CheckInServiceTest {

    private final CheckInService service = new CheckInService();

    private MyShift shift() {
        MyShift s = new MyShift();
        s.setEventDate("2026-11-15");
        s.setReportingTime("17:30");
        s.setEndTime("23:00");
        s.setAttendance("PENDING");
        return s;
    }

    @Test
    void onTimeIsPresent() {
        assertEquals("PRESENT", service.statusFor(shift(), LocalDateTime.of(2026, 11, 15, 17, 38)));
    }

    @Test
    void afterGraceIsLate() {
        assertEquals("LATE", service.statusFor(shift(), LocalDateTime.of(2026, 11, 15, 17, 45)));
    }

    @Test
    void wrongDayIsRejected() {
        assertNotNull(service.validate(shift(), LocalDateTime.of(2026, 11, 14, 17, 30)));
    }

    @Test
    void tooEarlyIsRejected() {
        assertNotNull(service.validate(shift(), LocalDateTime.of(2026, 11, 15, 15, 0)));
    }

    @Test
    void validWindowIsAccepted() {
        assertNull(service.validate(shift(), LocalDateTime.of(2026, 11, 15, 17, 0)));
    }

    @Test
    void unavailableCannotCheckIn() {
        MyShift s = shift();
        s.setUnavailable(true);
        assertNotNull(service.validate(s, LocalDateTime.of(2026, 11, 15, 17, 30)));
    }
}