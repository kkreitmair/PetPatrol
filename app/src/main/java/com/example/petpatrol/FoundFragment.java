package com.example.petpatrol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class FoundFragment extends Fragment {

    private LinearLayout fragmentContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_found, parent, false);
        fragmentContainer = view.findViewById(R.id.found_advert_container);
        for (int index=0; index <=4; index++) {
            View advert = getLayoutInflater().inflate(R.layout.advert, null);
            TextView text = advert.findViewById(R.id.advert_title_view);
            text.setText("test");
            fragmentContainer.addView(advert, fragmentContainer.getChildCount() - 1);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
//        FrameLayout foundLayout = (FrameLayout) view.getParent();
//        FloatingActionButton addButton = foundLayout.findViewById(R.id.floatingAddButton);
//        addButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                LayoutInflater inflater = getActivity().getLayoutInflater();
//                View advert = inflater.inflate(R.layout.advert, null);
//                TextView text = advert.findViewById(R.id.textView);
//                text.setText("new test (add Button)");
//                fragmentContainer.addView(advert, fragmentContainer.getChildCount());
//            }
//        });
    }
}