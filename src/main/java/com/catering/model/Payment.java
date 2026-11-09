package com.catering.model;

public class Payment {
    private int id;
    private int staffId;
    private String staffName;
    private double amount;
    private String payDate;
    private String note;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPayDate() { return payDate; }
    public void setPayDate(String payDate) { this.payDate = payDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}