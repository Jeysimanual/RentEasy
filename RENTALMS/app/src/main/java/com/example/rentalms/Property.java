// Property.java
package com.example.rentalms;

public class Property {
    private String city;
    private String exteriorImageUrl;
    private String interiorImageUrl;
    private String price;
    private String propertyName;
    private String province;
    private String type;

    public Property() {}

    public Property(String city, String exteriorImageUrl, String interiorImageUrl,
                    String price, String propertyName, String province, String type) {
        this.city = city;
        this.exteriorImageUrl = exteriorImageUrl;
        this.interiorImageUrl = interiorImageUrl;
        this.price = price;
        this.propertyName = propertyName;
        this.province = province;
        this.type = type;
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
}
