package com.example.petpatrol;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class AnimalAdvertModel {
    private String title;
    private String animal;
    private String image;
    private String color;
    private String size;
    private String tag;
    private String tagType;
    private LatLng position;
    private static final String TAG = "AnimalAdvertModel";

    public AnimalAdvertModel(){}

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAnimal(String animal) {
        this.animal = animal;
    }

    public void setImage(String imageName) {
        Log.d("AdvertModel", "ImageName: " + imageName);
        String defaultName = "images/test1.jpg";
        if (imageName == null) {
            Log.d("AdvertModel", "ImageName was null.");
            this.image = defaultName;
            return;
        }
        if (imageName.isEmpty()) {
            Log.d("AdvertModel", "ImageName was empty.");
            this.image = defaultName;
            return;
        }
        this.image = imageName;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public void setPosition(HashMap<String, Float> location) {
        if (location == null) {
            Log.d("test", "position is null");
        }

        this.position = new LatLng(location.get("latitude"), location.get("longitude"));
    }

    public String getTitle() {
        return title;
    }

    public String getAnimal() { return animal; }

    public String getImage() {
        return image;
    }

    public String getColor() {
        return this.color;
    }

    public String getSize() {
        return this.size;
    }

    public String getTag() {
        return this.tag;
    }

    public String getTagType() {
        return this.tagType;
    }

    public LatLng getPosition() {
        return position;
    }
}
