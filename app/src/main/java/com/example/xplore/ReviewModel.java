package com.example.xplore;

public class ReviewModel {

    private String review;
    private String id;
    private String date_created;

    // Constructor
    public ReviewModel(String review, String id, String date_created) {
        this.review = review;
        this.id = id;
        this.date_created = date_created;
    }

    // Getter and Setter
    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }
}