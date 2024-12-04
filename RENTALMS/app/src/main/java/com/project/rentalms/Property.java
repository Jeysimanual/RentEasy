package com.project.rentalms;

import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.List;

@IgnoreExtraProperties
public class Property {

    private String city;
    private String exteriorImageUrl;
    private String interiorImageUrl;
    private String price;
    private String propertyName;
    private String province;
    private String type;
    private String userId;
    private String barangay;
    private String address;
    private String paymentPeriod;
    private String description;
    private List<String> features;  // Updated to List<String> for array of strings
    private Boolean isFavorite;  // Changed from primitive boolean to Boolean to handle null values
    private String propertyId;

    // Default constructor (required for Firebase)
    public Property() {}

    // Full constructor
    public Property(String city, String exteriorImageUrl, String interiorImageUrl,
                    String price, String paymentPeriod, String propertyName,
                    String province, String type, String barangay,
                    String address, String description, List<String> features, String propertyId) {
        this.city = city;
        this.exteriorImageUrl = exteriorImageUrl;
        this.interiorImageUrl = interiorImageUrl;
        this.price = price;
        this.paymentPeriod = paymentPeriod;
        this.propertyName = propertyName;
        this.province = province;
        this.type = type;
        this.barangay = barangay;
        this.address = address;
        this.propertyId = propertyId;
        this.description = description;
        this.features = features;  // Set features as List<String>
    }

    // Getters and Setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getExteriorImageUrl() { return exteriorImageUrl; }
    public void setExteriorImageUrl(String exteriorImageUrl) { this.exteriorImageUrl = exteriorImageUrl; }

    public String getInteriorImageUrl() { return interiorImageUrl; }
    public void setInteriorImageUrl(String interiorImageUrl) { this.interiorImageUrl = interiorImageUrl; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getFeatures() { return features; }  // Return List<String> for features
    public void setFeatures(List<String> features) { this.features = features; }  // Accept List<String> for features

    public String getPaymentPeriod() { return paymentPeriod; }
    public void setPaymentPeriod(String paymentPeriod) { this.paymentPeriod = paymentPeriod; }

    public String getPropertyName() { return propertyName; }
    public void setPropertyName(String propertyName) { this.propertyName = propertyName; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Boolean getIsFavorite() { return isFavorite; }
    public void setIsFavorite(Boolean isFavorite) { this.isFavorite = isFavorite; }

    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }
}
