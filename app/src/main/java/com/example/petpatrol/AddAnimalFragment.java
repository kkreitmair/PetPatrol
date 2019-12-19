package com.example.petpatrol;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class AddAnimalFragment extends Fragment {

    ResetButtons _mResetListener;
    private FirebaseFirestore firestoreDB;


    public interface ResetButtons {
        public void resetButtons();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            _mResetListener = (ResetButtons) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement resetButtons");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_animal, parent, false);
        firestoreDB = FirebaseFirestore.getInstance();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getActivity().findViewById(R.id.lostButton).setVisibility(View.GONE);
        getActivity().findViewById(R.id.foundButton).setVisibility(View.GONE);

        final FrameLayout foundLayout = (FrameLayout) view.getParent();
        FloatingActionButton addButton = foundLayout.findViewById(R.id.floatingAddButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("AddAnimalFragment", "addButton");

                EditText advertTitle = foundLayout.findViewById(R.id.animal_advert_title);
                Map<String, Object> docData = new HashMap<>();
                docData.put("title", advertTitle.getText().toString());
                docData.put("animal", "Katze");

                firestoreDB.collection("lost")
                        .add(docData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("AddAnimal", "Event document added - id: "
                                        + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("AddAnimal", "Error adding event document", e);
                            }
                        });

                getActivity().findViewById(R.id.lostButton).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.foundButton).setVisibility(View.VISIBLE);
                getFragmentManager().popBackStack();
            }
        });

        FloatingActionButton exitButton = foundLayout.findViewById(R.id.floatingExitButton);
        exitButton.show();
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("AddAnimalFragment", "exitButton");
                getActivity().findViewById(R.id.lostButton).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.foundButton).setVisibility(View.VISIBLE);
                getFragmentManager().popBackStack();
            }
        });

        fillUpSpinner(view.findViewById(R.id.spinner_animal), R.array.selection_animal);
        fillUpSpinner(view.findViewById(R.id.spinner_color), R.array.selection_color);
        fillUpSpinner(view.findViewById(R.id.spinner_size), R.array.selection_size);
        fillUpSpinner(view.findViewById(R.id.spinner_tag_type), R.array.selection_tag_type);



    }

    private void fillUpSpinner(View spinner, int selectionId) {
        Spinner menu = (Spinner) spinner;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                selectionId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        menu.setAdapter(adapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _mResetListener.resetButtons();
    }
}
