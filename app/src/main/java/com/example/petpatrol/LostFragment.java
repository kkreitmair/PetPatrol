package com.example.petpatrol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;

import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class LostFragment extends Fragment {

    private RecyclerView advertContainer;
    private FirebaseFirestore firestoreDB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        firestoreDB = FirebaseFirestore.getInstance();

        View view = inflater.inflate(R.layout.fragment_lost, parent, false);
        advertContainer = view.findViewById(R.id.lost_advert_container);

        advertContainer.setLayoutManager(new LinearLayoutManager(getContext()){
            @Override
            public void onLayoutCompleted(final RecyclerView.State state) {
                super.onLayoutCompleted(state);
            }
        });

        final CollectionReference lostCollection = firestoreDB.collection("lost");
        Query query = lostCollection;
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(3)
                .setPageSize(6)
                .build();

        FirestorePagingOptions<AnimalAdvertModel> options = new FirestorePagingOptions.Builder<AnimalAdvertModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, AnimalAdvertModel.class)
                .build();

        FirestorePagingAdapter<AnimalAdvertModel, AnimalAdvertViewHolder> adapter =
                new FirestorePagingAdapter<AnimalAdvertModel, AnimalAdvertViewHolder>(options) {
                    @NonNull
                    @Override
                    public AnimalAdvertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.advert, parent, false);
                        return new AnimalAdvertViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final AnimalAdvertViewHolder holder,
                                                    int position,
                                                    @NonNull AnimalAdvertModel model) {
                        String advertTitle = model.getTitle();
                        String imageName = model.getImage();
                        holder.setTitle(advertTitle);
                        holder.setImage(imageName);
                    }
                };
        advertContainer.setAdapter(adapter);
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