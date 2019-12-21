package com.example.petpatrol;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class AnimalAdvertViewHolder extends RecyclerView.ViewHolder {
    private View view;

    AnimalAdvertViewHolder(View itemView) {
        super(itemView);
        view = itemView;
        view.findViewById(R.id.advert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Test", "card clicked");
            }
        });
    }

    void setTitle(String advertTitle) {
        TextView textView = view.findViewById(R.id.advert_title_view);
        textView.setText(advertTitle);
    }
}
