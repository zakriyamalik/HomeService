package com.example.homeservice.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String profilePicUrl;

    public User(String uid, String name, String email, String phone, String profilePicUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profilePicUrl = profilePicUrl;
    }

    public String getUid() { return uid; }
    public String getName() { return name != null ? name : "User"; }
    public String getEmail() { return email != null ? email : ""; }
    public String getPhone() { return phone != null ? phone : ""; }
    public String getProfilePicUrl() { return profilePicUrl != null ? profilePicUrl : ""; }
}