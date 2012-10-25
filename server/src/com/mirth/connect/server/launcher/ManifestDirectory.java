package com.mirth.connect.server.launcher;

public class ManifestDirectory implements ManifestEntry {
    private String dir;
    private String[] excludes = new String[0];

    public ManifestDirectory(String dir) {
        this.dir = dir;
    }

    public String getName() {
        return dir;
    }

    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }
}