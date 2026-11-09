package com.catering.model;

public class User {
    private int id;
    private String username;
    private String role;
    private int staffId;

    public User(int id, String username, String role) { this(id, username, role, 0); }

    public User(int id, String username, String role, int staffId) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.staffId = staffId;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public int getStaffId() { return staffId; }
}