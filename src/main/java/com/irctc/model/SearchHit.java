package com.irctc.model;

public class SearchHit {
    private String type;
    private String title;
    private String subtitle;
    private String url;

    public SearchHit(String type, String title, String subtitle, String url) {
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getUrl() {
        return url;
    }
}
