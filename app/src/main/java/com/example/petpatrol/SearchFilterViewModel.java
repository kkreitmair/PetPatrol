package com.example.petpatrol;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SearchFilterViewModel extends ViewModel {
    private final MutableLiveData<SearchFilter> filter = new MutableLiveData<>();

    public void setFilter(Context context, String animal) {
        SearchFilter filter = new SearchFilter(context);
        filter.setAnimal(animal);
        this.filter.setValue(filter);
    }

    public MutableLiveData getFilter() {
        return filter;
    }
}
