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

    public Property() {}

    public Property(String city, String exteriorImageUrl, String interiorImageUrl,
                    String price, String propertyName, String province, String type,
                    String barangay, String address) {
        this.city = city;
        this.exteriorImageUrl = exteriorImageUrl;
        this.interiorImageUrl = interiorImageUrl;
        this.price = price;
        this.propertyName = propertyName;
        this.province = province;
        this.type = type;
        this.barangay = barangay;
        this.address = address;
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
