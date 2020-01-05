package com.example.petpatrol;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SearchFilterViewModel extends ViewModel {
    private final MutableLiveData<SearchFilter> filter = new MutableLiveData<>();

    public void setFilter(String animal) {
        SearchFilter filter = new SearchFilter();
        filter.setAnimal(animal);
        this.filter.setValue(filter);
    }

    public MutableLiveData getAnimal() {
        return filter;
    }
}
