package com.example.petpatrol;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ImageDialogFragment extends DialogFragment {

    public interface ImageDialogListener {
        public void onTakePictureClick(DialogFragment dialog);
        public void onChoosePictureClick(DialogFragment dialog);
    }

    ImageDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            Log.d("ImageDialogListener", "parent Fragment: "
                    + getTargetFragment().toString());
            listener = (ImageDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ImageDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Image from:")
                .setItems(R.array.get_image_selection, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("ImageDialogFragment", "id: " + id);
                        if (id == 0) {
                            listener.onTakePictureClick(ImageDialogFragment.this);
                        }
                        if (id == 1) {
                            listener.onChoosePictureClick(ImageDialogFragment.this);
                        }
                    }
                });
        return builder.create();
    }
}
