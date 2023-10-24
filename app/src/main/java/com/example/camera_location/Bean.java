package com.example.camera_location;

import android.graphics.Bitmap;

public class Bean {
    private String name;
    private int id;
    private Bitmap previewImg;

    public Bean(int id, String name, Bitmap img) {
        this.id = id;
        this.name = name;
        this.previewImg = img;
    }
    public Bean(){}

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public Bitmap getPreviewImg() {
        return this.previewImg;
    }
}
