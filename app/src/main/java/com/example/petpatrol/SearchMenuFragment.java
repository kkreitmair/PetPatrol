package com.example.petpatrol;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SearchMenuFragment extends Fragment {

    AddAnimalFragment.ResetButtons _mResetListener;

    public interface ResetButtons {
        public void resetButtons();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            _mResetListener = (AddAnimalFragment.ResetButtons) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement resetButtons");
        }
    }

    private boolean tagEnabled = false;
    private FloatingActionButton addButton;
    private static final String TAG = "SearchMenuFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.search_filter, parent, false);

        initSpinners(view);

        return view;
    }

    private void initSpinners(View view) {
        initSpinner(view.findViewById(R.id.search_animal), R.array.selection_animal);
        initSpinner(view.findViewById(R.id.search_color), R.array.selection_color);
        initSpinner(view.findViewById(R.id.search_size), R.array.selection_size);

        Spinner tagSpinner = initSpinner(view.findViewById(R.id.search_tag_type), R.array.selection_tag_type);
        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout foundLayout = (LinearLayout) parent.getParent();
                if ((parent.getCount() - 1) != id) {
                    Log.d(TAG, "Tag Type selected.");
                    tagEnabled = true;
                    foundLayout.findViewById(R.id.search_tag).setEnabled(true);
                } else {
                    tagEnabled = false;
                    foundLayout.findViewById(R.id.search_tag).setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "No Tag Type selected.");
            }
        });
    }

    private Spinner initSpinner(View spinner, int selectionId) {
        Spinner menu = (Spinner) spinner;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                selectionId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        menu.setAdapter(adapter);
        menu.setSelection(adapter.getCount() - 1);

        return menu;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        FrameLayout layout = (FrameLayout) view.getParent();
        ConstraintLayout mainLayout = (ConstraintLayout) layout.getParent();
//        addButton = layout.findViewById(R.id.addButton);
//        addButton.hide();
//        mainLayout.findViewById(R.id.lostButton).setVisibility(View.INVISIBLE);
//        mainLayout.findViewById(R.id.foundButton).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _mResetListener.resetButtons();
    }
}
