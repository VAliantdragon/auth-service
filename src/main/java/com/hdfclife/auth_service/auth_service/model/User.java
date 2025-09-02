package com.hdfclife.auth_service.auth_service.model;

public class User {
    private String username;
    private String password; // This will store the BCrypt hashed password

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}