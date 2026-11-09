package com.catering.model;

public class MyShift {
    private int assignmentId;
    private String eventName;
    private String venue;
    private String eventDate;
    private String startTime;
    private String endTime;
    private String reportingTime;
    private String attendance;
    private double dailyWage;
    private boolean unavailable;
    private String checkInTime;

    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int v) { this.assignmentId = v; }
    public String getEventName() { return eventName; }
    public void setEventName(String v) { this.eventName = v; }
    public String getVenue() { return venue; }
    public void setVenue(String v) { this.venue = v; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String v) { this.eventDate = v; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String v) { this.startTime = v; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String v) { this.endTime = v; }
    public String getReportingTime() { return reportingTime; }
    public void setReportingTime(String v) { this.reportingTime = v; }
    public String getAttendance() { return attendance; }
    public void setAttendance(String v) { this.attendance = v; }
    public double getDailyWage() { return dailyWage; }
    public void setDailyWage(double v) { this.dailyWage = v; }
    public boolean isUnavailable() { return unavailable; }
    public void setUnavailable(boolean v) { this.unavailable = v; }
    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String v) { this.checkInTime = v; }
}