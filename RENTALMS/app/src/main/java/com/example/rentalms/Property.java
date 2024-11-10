package com.example.rentalms;

public class Property {
    private String city;
    private String exteriorImageUrl;
    private String interiorImageUrl;
    private String price;
    private String propertyName;
    private String province;
    private String type;
    private String userId;    // Ensures 'userId' is stored in each Property instance
    private String barangay;  // New field for barangay
    private String address;   // New field for address

    private String paymentPeriod;
    private boolean isFavorite;
    private String propertyId;

    // Default constructor
    public Property() {}

    // Constructor with parameters (fixed to include propertyId in the constructor)
    public Property(String city, String exteriorImageUrl, String interiorImageUrl,
                    String price,String paymentPeriod, String propertyName, String province, String type,
                    String barangay, String address, String propertyId) {
        this.city = city;
        this.exteriorImageUrl = exteriorImageUrl;
        this.interiorImageUrl = interiorImageUrl;
        this.paymentPeriod = paymentPeriod;
        this.price = price;
        this.propertyName = propertyName;
        this.province = province;
        this.type = type;
        this.barangay = barangay;
        this.address = address;
        this.propertyId = propertyId;  // Correctly set propertyId here
    }

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

    public String getPaymentPeriod() {
        return paymentPeriod;
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

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getter and Setter for barangay
    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    // Getter and Setter for address
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
