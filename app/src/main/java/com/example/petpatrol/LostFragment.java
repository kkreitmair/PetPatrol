package com.example.petpatrol;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class LostFragment extends Fragment {

    private ScrollView lostContainer;
    private RecyclerView advertContainer;
    private FirebaseFirestore firestoreDB;
    private FirebaseStorage fbStorage;
    private boolean isUpdateRunning = false;
    private boolean isLastItemReached = false;
    private DocumentSnapshot lastVisibleItem;
    private int limit = 3;
    private String Tag = "LostFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        firestoreDB = FirebaseFirestore.getInstance();
        fbStorage = FirebaseStorage.getInstance();

        View view = inflater.inflate(R.layout.fragment_lost, parent, false);
        advertContainer = view.findViewById(R.id.lost_advert_container);


        advertContainer.setLayoutManager(new LinearLayoutManager(getContext()){
            @Override
            public void onLayoutCompleted(final RecyclerView.State state) {
                super.onLayoutCompleted(state);
                isUpdateRunning = false;
            }
        });

        final List<AnimalAdvertModel> adverts = new ArrayList<>();
        final AnimalAdvertAdapter advertAdapter = new AnimalAdvertAdapter(adverts);
        advertContainer.setAdapter(advertAdapter);

        lostContainer = view.findViewById(R.id.lost_fragment_container);

        final CollectionReference lostCollection = firestoreDB.collection("lost");
        Query query = lostCollection.limit(limit);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        AnimalAdvertModel animalAdvertModel = document.toObject(AnimalAdvertModel.class);
                        adverts.add(animalAdvertModel);
                    }
                    advertAdapter.notifyDataSetChanged();
                    lastVisibleItem = task.getResult().getDocuments().get(task.getResult().size() - 1);

                    final OnCompleteListener completeListener = new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot documents = task.getResult();
                                for (DocumentSnapshot document : documents) {
                                    AnimalAdvertModel animalAdvertModel = document.toObject(AnimalAdvertModel.class);
                                    adverts.add(animalAdvertModel);
                                }
                                advertAdapter.notifyDataSetChanged();
                                if (!documents.getDocuments().isEmpty()) {
                                    if (lastVisibleItem.equals(documents.getDocuments().get(documents.size() - 1)))
                                    {
                                        Log.d(Tag, lastVisibleItem.getId());
                                    }
                                    lastVisibleItem = documents.getDocuments().get(documents.size() - 1);
                                } else {
                                    isLastItemReached = true;
                                }

                                if (task.getResult().size() < limit) {
                                    isLastItemReached = true;
                                }
                            }
                        }
                    };

                    ViewTreeObserver.OnScrollChangedListener scrollListener = new ViewTreeObserver
                            .OnScrollChangedListener() {
                        @Override
                        public void onScrollChanged() {
                            Log.d(Tag, "isLastItemReached: " + isLastItemReached);
                            int bottomOfChild = lostContainer.getChildAt(0).getBottom();
                            int scrollPosition = (lostContainer.getHeight() + lostContainer.getScrollY());
                            if ((bottomOfChild <= scrollPosition)) {
                                Log.d(Tag, "view is at bottom");
                                if (!isLastItemReached) {
                                    if (!isUpdateRunning) {
                                        isUpdateRunning = true;

                                        Query query = lostCollection
                                                .startAfter(lastVisibleItem)
                                                .limit(limit);

                                        query.get().addOnCompleteListener(completeListener);
                                    }
                                }
                            } else {
                                Log.d(Tag, "view not at botton");
                            }
                        }
                    };

                    lostContainer.getViewTreeObserver()
                            .addOnScrollChangedListener(scrollListener);
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
//                advertContainer.addView(advert, advertContainer.getChildCount());
//            }
//        });
    }
}