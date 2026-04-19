package com.example.homeservice.models;

public class Booking {
    private int id;
    private int serviceId;
    private String serviceName;
    private String date;
    private String time;
    private double price;
    private String status;

    public Booking(int id, int serviceId, String serviceName, String date, String time, double price, String status) {
        this.id = id;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.date = date;
        this.time = time;
        this.price = price;
        this.status = status;
    }

    public int getId() { return id; }
    public int getServiceId() { return serviceId; }
    public String getServiceName() { return serviceName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}