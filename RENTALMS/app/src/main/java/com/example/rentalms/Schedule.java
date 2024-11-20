package com.example.rentalms;

public class Schedule {
    private String date;
    private String time;
    private String status;
    private String propertyName;
    private String barangay;
    private String address;
    private String city;

    public Schedule(String date, String time, String status, String propertyName, String barangay, String address, String city) {
        this.date = date;
        this.time = time;
        this.status = status;
        this.propertyName = propertyName;
        this.barangay = barangay;
        this.address = address;
        this.city = city;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getBarangay() {
        return barangay;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }
}

