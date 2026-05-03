package com.example.homeservice.models;

public class Booking {
    public static final String STATUS_UPCOMING = "Upcoming";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_MISSED = "Missed";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String STATUS_RATED = "Rated";

    private int id;
    private int serviceId;
    private String serviceName;
    private String date;
    private String time;
    private double price;
    private String status;
    private int rating;
    private String userId;

    public Booking(int id, int serviceId, String serviceName, String date, String time,
                   double price, String status, int rating, String userId) {
        this.id = id;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.date = date;
        this.time = time;
        this.price = price;
        this.status = status;
        this.rating = rating;
        this.userId = userId;
    }

    // Getters
    public int getId() { return id; }
    public int getServiceId() { return serviceId; }
    public String getServiceName() { return serviceName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public int getRating() { return rating; }
    public String getUserId() { return userId; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setRating(int rating) { this.rating = rating; }
}