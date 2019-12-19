package com.example.petpatrol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LostFragment extends Fragment {

    private LinearLayout fragmentContainer;
    private FirebaseFirestore firestoreDB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        firestoreDB = FirebaseFirestore.getInstance();

        View view = inflater.inflate(R.layout.fragment_lost, parent, false);
        fragmentContainer = view.findViewById(R.id.lost_advert_container);

        firestoreDB.collection("lost").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for(DocumentSnapshot doc : task.getResult()) {
                        View advert = getLayoutInflater().inflate(R.layout.advert, null);
                        TextView text = advert.findViewById(R.id.textView);
                        text.setText(doc.getString("title"));
                        fragmentContainer.addView(advert, fragmentContainer.getChildCount() - 1);
                    }
                }
            }
        });
       return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
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