package com.irctc.model;

/** Welcome-banner text and image chosen in Website & Dashboard Management. */
public class BannerContent {
    private String title = "Welcome Back";
    private String description = "";
    private String badge = "Employee Dashboard";
    private String image = "/assets/images/train-banner.jpg";
    private int activityLimit = 5;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getActivityLimit() {
        return activityLimit;
    }

    public void setActivityLimit(int activityLimit) {
        this.activityLimit = activityLimit;
    }
}
