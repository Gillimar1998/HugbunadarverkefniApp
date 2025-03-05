package com.example.hugbunadarverkefni.model;

import java.io.Serializable;

public class Image implements Serializable { // Implements Serializable
    private Long id;
    private String name;
    private String url;
    private boolean deleted;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public boolean isDeleted() { return deleted; }
}
