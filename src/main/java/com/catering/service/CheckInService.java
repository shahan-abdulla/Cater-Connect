package com.catering.service;

import com.catering.model.MyShift;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CheckInService {

    public static final int EARLY_MINUTES = 60;   // check-in opens this long before reporting time
    public static final int GRACE_MINUTES = 10;   // after this many minutes past reporting time = LATE

    // Returns null if check-in is allowed, otherwise the reason it is not
    public String validate(MyShift s, LocalDateTime now) {
        if (s.isUnavailable()) return "You reported that you are unavailable for this event.";
        if (!s.getAttendance().equals("PENDING")) return "You have already checked in.";

        LocalDate date = LocalDate.parse(s.getEventDate());
        LocalTime report = LocalTime.parse(s.getReportingTime());
        LocalTime end = LocalTime.parse(s.getEndTime());
        LocalTime opens = report.minusMinutes(EARLY_MINUTES);

        if (!now.toLocalDate().equals(date)) return "Check-in is only possible on the event date (" + date + ").";
        if (now.toLocalTime().isBefore(opens)) return "Check-in opens at " + opens + ".";
        if (now.toLocalTime().isAfter(end)) return "This event has already ended.";
        return null;
    }

    public String statusFor(MyShift s, LocalDateTime now) {
        LocalTime report = LocalTime.parse(s.getReportingTime());
        return now.toLocalTime().isAfter(report.plusMinutes(GRACE_MINUTES)) ? "LATE" : "PRESENT";
    }
}