package com.example.petpatrol;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    void setImage(String imageName) {
        FirebaseStorage fbStorage = FirebaseStorage.getInstance();
        StorageReference storageRef = fbStorage.getReference();
        Log.d("ViewHolder", "ImageName is: " + imageName);
        StorageReference imageRef = storageRef.child(imageName);

        final ImageView imageView = view.findViewById(R.id.advert_image_view);

        final long ONE_MEGABYTE = 512 * 512;
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap == null) {
                    Log.d("TEST", "bitmap could not be loaded");
                }
                imageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("TEST", "Image could not be downloaded.");
            }
        });
    }
}
