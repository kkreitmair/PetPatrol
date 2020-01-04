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
    private ImageView imageView;
    private Bitmap bitmap;
    private StorageReference storageRef;
    private FirebaseStorage fbStorage;
    private static final String TAG = "AnimalAdvertViewHolder";

    AnimalAdvertViewHolder(View itemView) {
        super(itemView);
        view = itemView;
        imageView = view.findViewById(R.id.advert_image_view);
        fbStorage = FirebaseStorage.getInstance();
        storageRef = fbStorage.getReference();
    }

    View getAdvertView() {
        return this.view.findViewById(R.id.advert);
    }

    void setTitle(String advertTitle) {
        TextView textView = view.findViewById(R.id.advert_title_view);
        textView.setText(advertTitle);
    }

    void setImage(String imageName) {
        Log.d(TAG, "ImageName is: " + imageName);
        final StorageReference imageRef = storageRef.child(imageName);

        final long ONE_MEGABYTE = 512 * 512;
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap == null) {
                    Log.d(TAG, "bitmap could not be loaded");
                }
                imageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Image could not be downloaded." + exception.getMessage());
            }
        });
    }

    Bitmap getImageBitmap() {
        return this.bitmap;
    }
}
