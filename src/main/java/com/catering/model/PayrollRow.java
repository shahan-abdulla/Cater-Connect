package com.catering.model;

public class PayrollRow {
    private int staffId;
    private String name;
    private String phone;
    private int shifts;
    private double pendingAmount;

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getShifts() { return shifts; }
    public void setShifts(int shifts) { this.shifts = shifts; }

    public double getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(double pendingAmount) { this.pendingAmount = pendingAmount; }
}