package com.example.petpatrol;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class AnimalAdvertModel implements Parcelable{
    private String title  = "";
    private String animal  = "";
    private String image  = "";
    private String color  = "";
    private String size  = "";
    private String tag  = "";
    private String tagType = "";
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
        if (tagType == null) {
            this.tagType = "";
        } else {
            this.tagType = tagType;
        }
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
        return tagType;
    }

    public LatLng getPosition() {
        return position;
    }

    @Override
    public int describeContents() {
// ignore for now
        return 0;
    }

    @Override
    public void writeToParcel(Parcel pc, int flags) {
        pc.writeString(title);
        pc.writeString(animal);
        pc.writeString(image);
        pc.writeString(color);
        pc.writeString(size);
        pc.writeString(tag);
        pc.writeString(tagType);
        pc.writeParcelable(position, flags);
    }

    /** Static field used to regenerate object, individually or as arrays */
    public static final Parcelable.Creator<AnimalAdvertModel> CREATOR = new Parcelable.Creator<AnimalAdvertModel>() {
        public AnimalAdvertModel createFromParcel(Parcel pc) {
            return new AnimalAdvertModel(pc);
        }
        public AnimalAdvertModel[] newArray(int size) {
            return new AnimalAdvertModel[size];
        }
    };

    /**Ctor from Parcel, reads back fields IN THE ORDER they were written */
    public AnimalAdvertModel(Parcel pc){
        title = pc.readString();
        animal =  pc.readString();
        image = pc.readString();
        color = pc.readString();
        size = pc.readString();
        tag = pc.readString();
        tagType = pc.readString();
        position = pc.readParcelable(ClassLoader.getSystemClassLoader());
    }
}
