package com.irctc.model;

public class PageLink {
    private final String label;
    private final String url;
    private final boolean current;
    private final boolean disabled;

    public PageLink(String label, String url, boolean current, boolean disabled) {
        this.label = label;
        this.url = url;
        this.current = current;
        this.disabled = disabled;
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }

    public boolean isCurrent() {
        return current;
    }

    public boolean isDisabled() {
        return disabled;
    }
}
