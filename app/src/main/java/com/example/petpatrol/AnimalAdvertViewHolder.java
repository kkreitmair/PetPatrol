package com.example.petpatrol;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class AnimalAdvertViewHolder extends RecyclerView.ViewHolder {
    private View view;

    AnimalAdvertViewHolder(View itemView) {
        super(itemView);
        view = itemView;
    }

    void setTitle(String advertTitle) {
        TextView textView = view.findViewById(R.id.advert_title_view);
        textView.setText(advertTitle);
    }
}
