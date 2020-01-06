package com.example.petpatrol;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import android.content.Context;

public class SearchFilter {

    private FirebaseFirestore firestoreDB;
    private Context context;
    private String title;
    private String animal;
    private String color;
    private String size;
    private String tagType;
    private String tag;
    private LatLng location;

    public SearchFilter(Context context){
        this.context = context;
        firestoreDB = FirebaseFirestore.getInstance();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAnimal(String animal) {
        boolean found = false;
        String hintText = context.getResources().getString(R.string.add_animal_hint_animal);

        if (!animal.equals(hintText)) {
            String[] values = getValues(R.array.selection_animal);
            for (String value : values) {
                if (animal.equals(value)) {
                    found = true;
                }
            }
        }

        if (found) {
            this.animal = animal;
        } else {
            this.animal = "";
        }
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
        Query query;
        if (!animal.equals("")) {
             query = lostCollection.whereEqualTo("animal", animal);
        } else {
            query = lostCollection;
        }

        return query;
    }

    private String[] getValues(int selctionId) {
        return context.getResources().getStringArray(selctionId);
    }
}
