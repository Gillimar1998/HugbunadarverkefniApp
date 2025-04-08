package com.example.hugbunadarverkefni.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Image implements Serializable { // Implements Serializable
    private Long id;
    private String name;
    private String url;
    private boolean deleted;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public void setUrl(String url){
        this.url = url;
    }
    public boolean isDeleted() { return deleted; }

    public void loadImage(Context context, ImageView imageView) {
        if (url != null) {
            Glide.with(context)
                    .load(url) // This can be a URL or a file path
                    .into(imageView); // Set the ImageView with the image
        }
    }
}
