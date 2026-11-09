package com.catering.model;

public class Staff {
    private int id;
    private String name;
    private String phone;
    private String address;
    private int age;
    private String role = "SERVER";
    private double dailyWage;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getDailyWage() { return dailyWage; }
    public void setDailyWage(double dailyWage) { this.dailyWage = dailyWage; }
}