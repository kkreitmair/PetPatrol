package com.example.petpatrol;

import android.util.Log;

public class AnimalAdvertModel {
    private String title;
    private String animal;
    private String image;
    private String color;
    private String size;

    public AnimalAdvertModel(){}

    public AnimalAdvertModel(String title, String animal, String image, String color, String size) {
        this.title = title;
        this.animal = animal;
        this.color = color;
        this.size = size;
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

    public String getImage() {
        return image;
    }
}
