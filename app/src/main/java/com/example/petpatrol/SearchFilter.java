package com.example.petpatrol;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class SearchFilter {

    private FirebaseFirestore firestoreDB;
    private String title;
    private String animal;
    private String color;
    private String size;
    private String tagType;
    private String tag;
    private LatLng location;

    public SearchFilter(){
        firestoreDB = FirebaseFirestore.getInstance();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAnimal(String animal) {
        this.animal = animal;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public Query getQuery() {
        CollectionReference lostCollection = firestoreDB.collection("lost");

        Query query = lostCollection.whereEqualTo("animal", animal);
        return query;
    }
}
