package com.catering.model;

public class Assignment {
    private int id;
    private int eventId;
    private int staffId;
    private String staffName;
    private String staffRole;
    private String phone;
    private String attendance;
    private double wageAmount;
    private boolean paid;
    private double dailyWage;
    private String checkInTime;        // HH:mm or null
    private boolean unavailable;
    private String unavailableReason;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public String getStaffRole() { return staffRole; }
    public void setStaffRole(String staffRole) { this.staffRole = staffRole; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAttendance() { return attendance; }
    public void setAttendance(String attendance) { this.attendance = attendance; }
    public double getWageAmount() { return wageAmount; }
    public void setWageAmount(double wageAmount) { this.wageAmount = wageAmount; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public double getDailyWage() { return dailyWage; }
    public void setDailyWage(double dailyWage) { this.dailyWage = dailyWage; }
    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
    public boolean isUnavailable() { return unavailable; }
    public void setUnavailable(boolean unavailable) { this.unavailable = unavailable; }
    public String getUnavailableReason() { return unavailableReason; }
    public void setUnavailableReason(String unavailableReason) { this.unavailableReason = unavailableReason; }
}