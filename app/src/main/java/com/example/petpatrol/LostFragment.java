package com.example.petpatrol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

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

import java.util.ArrayList;
import java.util.List;

public class LostFragment extends Fragment {

    private RecyclerView fragmentContainer;
    private FirebaseFirestore firestoreDB;
    private boolean isScrolling = false;
    private boolean isLastItemReached = false;
    private DocumentSnapshot lastVisible;
    private int limit = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        firestoreDB = FirebaseFirestore.getInstance();

        View view = inflater.inflate(R.layout.fragment_lost, parent, false);
        fragmentContainer = view.findViewById(R.id.lost_advert_container);

        fragmentContainer.setLayoutManager(new LinearLayoutManager(getContext()));
        final List<AnimalAdvertModel> list = new ArrayList<>();
        final AnimalAdvertAdapter animalAdvertAdapter = new AnimalAdvertAdapter(list);
        fragmentContainer.setAdapter(animalAdvertAdapter);

//        Task<QuerySnapshot> future = firestoreDB.collection("lost").get();

        final CollectionReference animalAdvertRef = firestoreDB.collection("lost");
        Query query = animalAdvertRef.orderBy("title", Query.Direction.ASCENDING).limit(3);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        AnimalAdvertModel animalAdvertModel = document.toObject(AnimalAdvertModel.class);
                        list.add(animalAdvertModel);
                    }
                    animalAdvertAdapter.notifyDataSetChanged();
                    lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                isScrolling = true;
                            }
                        }

                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                            int visibleItemCount = linearLayoutManager.getChildCount();
                            int totalItemCount = linearLayoutManager.getItemCount();

                            if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount) && !isLastItemReached) {
                                isScrolling = false;
                                Query nextQuery = animalAdvertRef.orderBy("title", Query.Direction.ASCENDING).startAfter(lastVisible).limit(limit);
                                nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> t) {
                                        if (t.isSuccessful()) {
                                            for (DocumentSnapshot d : t.getResult()) {
                                                AnimalAdvertModel animalAdvertModel = d.toObject(AnimalAdvertModel.class);
                                                list.add(animalAdvertModel);
                                            }
                                            animalAdvertAdapter.notifyDataSetChanged();
                                            lastVisible = t.getResult().getDocuments().get(t.getResult().size() - 1);

                                            if (t.getResult().size() < limit) {
                                                isLastItemReached = true;
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    };
                    fragmentContainer.addOnScrollListener(onScrollListener);
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