package com.example.petpatrol;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AnimalAdvertAdapter extends RecyclerView.Adapter<AnimalAdvertViewHolder> {
    private List<AnimalAdvertModel> list;

    AnimalAdvertAdapter(List<AnimalAdvertModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public AnimalAdvertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.advert, parent, false);
        return new AnimalAdvertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalAdvertViewHolder animalAdvertViewHolder, int position) {
        String advertTitle = list.get(position).getTitle();
        String imageName = list.get(position).getImage();
        animalAdvertViewHolder.setTitle(advertTitle);
        animalAdvertViewHolder.setImage(imageName);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
