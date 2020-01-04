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

    public AnimalAdvertModel(String title, String animal, String image, String color, String size,
                             String tag, String tagType, HashMap position) {
        this.title = title;
        this.animal = animal;
        this.color = color;
        this.size = size;
        this.tag = tag;
        this.tagType = tagType;
        setPosition(position);
        setImage(image);
    }

    private void setImage(String imageName) {
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

    public String getAnimal() {
        return animal;
    }

    public String getTitle() {
        return title;
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

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(HashMap<String, Float> location) {
        if (location == null) {
            Log.d("test", "position is null");
        }

        this.position = new LatLng(location.get("latitude"), location.get("longitude"));
    }

    public String getImage() {
        return image;
    }
}
