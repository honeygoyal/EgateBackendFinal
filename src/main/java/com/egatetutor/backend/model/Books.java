package com.egatetutor.backend.model;

import javax.persistence.*;

@Entity
@Table(name = "books")
public class Books {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",  nullable = false, updatable = false)
    private long id;
    private String name;
    private String googlePlayUrl;
    private String imageUrl;
    private String price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGooglePlayUrl() {
        return googlePlayUrl;
    }

    public void setGooglePlayUrl(String googlePlayUrl) {
        this.googlePlayUrl = googlePlayUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
