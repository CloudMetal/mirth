package com.webreach.mirth.model;

public abstract class MetaData {
    private String name;
    private String author;
    private String mirthVersion;
    private String pluginVersion;
    private String url;
    private String description;
    private boolean enabled;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMirthVersion() {
        return mirthVersion;
    }

    public void setMirthVersion(String mirthVersion) {
        this.mirthVersion = mirthVersion;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
