package com.catering.service;

public class PayrollService {

    public static final double LATE_FACTOR = 0.75;

    public double calculateWage(double dailyWage, String attendance) {
        switch (attendance) {
            case "PRESENT":
                return dailyWage;
            case "LATE":
                return Math.round(dailyWage * LATE_FACTOR * 100.0) / 100.0;
            default:
                return 0;   // ABSENT or PENDING
        }
    }
}