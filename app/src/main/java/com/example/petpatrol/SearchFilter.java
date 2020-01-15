package com.example.petpatrol;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import android.content.Context;
import android.util.Log;

import ch.hsr.geohash.GeoHash;

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
    private String geoHash;
    private String collection;
    private static final String TAG = "SearchFilter";

    public SearchFilter(Context context, String title, String animal, String color, String size,
                        String tagType, String tag, LatLng location, String collection){
        this.context = context;
        firestoreDB = FirebaseFirestore.getInstance();
        setTitle(title);
        setAnimal(animal);
        setColor(color);
        setSize(size);
        setTagType(tagType);
        setTag(tag);
        setLocation(location);
        setCollection(collection);
    }

    public void setTitle(String title) {
        this.title = null;
    }

    public void setAnimal(String animal) {
        if (spinnerInputIsValid(animal, R.string.add_animal_hint_animal, R.array.selection_animal)) {
            this.animal = animal;
        } else {
            this.animal = null;
        }
    }

    public void setColor(String color) {
        if (spinnerInputIsValid(color, R.string.add_animal_hint_color, R.array.selection_color)) {
            this.color = color;
        } else {
            this.color = null;
        }
    }

    public void setSize(String size) {
        if (spinnerInputIsValid(size, R.string.add_animal_hint_size, R.array.selection_size)) {
            this.size = size;
        } else {
            this.size = null;
        }
    }

    public void setTagType(String tagType) {
        if (spinnerInputIsValid(tagType, R.string.add_animal_hint_tag_type, R.array.selection_tag_type)) {
            this.tagType = tagType;
        } else {
            this.tagType = null;
        }
    }

    public void setTag(String tag) {
        this.tag = null;
    }

    public void setLocation(LatLng location) {
        this.location = location;
        setGeoHash();
    }

    public void setGeoHash() {
        if (location != null) {
            GeoHash geoHash = GeoHash.withCharacterPrecision(location.latitude, location.longitude, 12);
            this.geoHash = geoHash.toBase32();
        } else {
            this.geoHash = null;
        }
    }

    public void setCollection(String collection) {
        if (collection != null) {
            if (collection.equals("lost")) {
                this.collection = "lost";
            } else {
                this.collection = "found";
            }
        } else {
            this.collection = "lost";
        }
    }

    public Query getQuery() {
        CollectionReference lostCollection = firestoreDB.collection(collection);
        Query query = lostCollection;
        if (title != null) {
            Log.d(TAG, "added to filter: title");
            query = query.whereEqualTo("title", title);
        }
        if (animal != null) {
            Log.d(TAG, "added to filter: animal");
             query = query.whereEqualTo("animal", animal);
        }
        if (color != null) {
            Log.d(TAG, "added to filter: color");
            query = query.whereEqualTo("color", color);
        }
        if (size != null) {
            Log.d(TAG, "added to filter: size");
            query = query.whereEqualTo("size", size);
        }
        if (tagType != null) {
            Log.d(TAG, "added to filter: tagType");
            query = query.whereEqualTo("tag_type", tagType);
        }
        if (tag != null) {
            Log.d(TAG, "added to filter: tag");
            query = query.whereEqualTo("tag", tag);
        }
        if (geoHash != null) {
            Log.d(TAG, "added to filter: location");
            query = query.orderBy("geohash").startAt(geoHash.substring(0, 5)).endAt(geoHash.substring(0, 5) + "~");
        }
        return query;
    }

    private boolean spinnerInputIsValid(String input, int hintResource, int valuesResource) {
        String hintText = context.getResources().getString(hintResource);

        if (input != null) {
            if (!input.equals(hintText)) {
                String[] values = getValidValues(valuesResource);
                for (String value : values) {
                    if (input.equals(value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String[] getValidValues(int selctionId) {
        return context.getResources().getStringArray(selctionId);
    }
}
