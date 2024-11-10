package com.example.rentalms;

public class Property {
    private String city;
    private String exteriorImageUrl;
    private String interiorImageUrl;
    private String price;
    private String propertyName;
    private String province;
    private String type;
    private String userId;
    private String barangay;       // New field for barangay location
    private String address;        // New field for address

    // Default constructor
    public Property() {}

    // Constructor with all fields
    public Property(String city, String exteriorImageUrl, String interiorImageUrl,
                    String price, String propertyName, String province, String type,
                    String userId, String barangay, String address) {
        this.city = city;
        this.exteriorImageUrl = exteriorImageUrl;
        this.interiorImageUrl = interiorImageUrl;
        this.price = price;
        this.propertyName = propertyName;
        this.province = province;
        this.type = type;
        this.userId = userId;
        this.barangay = barangay;
        this.address = address;
    }

    // Getters
    public String getCity() {
        return city;
    }

    public String getExteriorImageUrl() {
        return exteriorImageUrl;
    }

    public String getInteriorImageUrl() {
        return interiorImageUrl;
    }

    public String getPrice() {
        return price;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getProvince() {
        return province;
    }

    public String getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public String getBarangay() {   // Getter for barangay
        return barangay;
    }

    public String getAddress() {    // Getter for address
        return address;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setBarangay(String barangay) {    // Setter for barangay
        this.barangay = barangay;
    }

    public void setAddress(String address) {      // Setter for address
        this.address = address;
    }
}
