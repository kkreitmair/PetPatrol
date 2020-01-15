package com.example.petpatrol;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

public class SearchFilterViewModel extends ViewModel {
    private final MutableLiveData<SearchFilter> filter = new MutableLiveData<>();

    public void setFilter(Context context, String title, String animal, String color, String size,
                          String tagType, String tag, LatLng location, String collection) {
        SearchFilter filter = new SearchFilter(context, title, animal, color, size, tagType, tag,
                location, collection);
        this.filter.setValue(filter);
    }

    public MutableLiveData getFilter() {
        return filter;
    }
}
