package com.example.petpatrol;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FoundFragment extends Fragment {

    private RecyclerView advertContainer;
    private FirebaseFirestore firestoreDB;
    private Fragment searchMenu = new SearchMenuFragment();
    private FirestorePagingAdapter<AnimalAdvertModel, AnimalAdvertViewHolder> adapter;
    private String collection;
    private SearchFilterViewModel model;
    private static final String TAG = "LostFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        model = ViewModelProviders.of(getActivity()).get(SearchFilterViewModel.class);

        collection = "found";

        model.getFilter().observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                Log.d(TAG, "Search filter has changed.");
                SearchFilter filter = (SearchFilter) o;
                adapter = getAdapter(filter.getQuery());
                advertContainer.getLayoutManager().removeAllViews();
                advertContainer.setAdapter(adapter);
            }
        });

        firestoreDB = FirebaseFirestore.getInstance();

        View view = inflater.inflate(R.layout.fragment_lost, parent, false);

        advertContainer = view.findViewById(R.id.lost_advert_container);

        advertContainer.setLayoutManager(new LinearLayoutManager(getContext()){
            @Override
            public void onLayoutCompleted(final RecyclerView.State state) {
                super.onLayoutCompleted(state);
            }
        });
        final CollectionReference lostCollection = firestoreDB.collection(collection);
        Query query = lostCollection;
        adapter = getAdapter(query);
        advertContainer.setAdapter(adapter);

        return view;
    }

    private FirestorePagingAdapter<AnimalAdvertModel, AnimalAdvertViewHolder> getAdapter(Query query) {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(3)
                .setPageSize(6)
                .build();

        FirestorePagingOptions<AnimalAdvertModel> options = new FirestorePagingOptions.Builder<AnimalAdvertModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, AnimalAdvertModel.class)
                .build();

        FirestorePagingAdapter<AnimalAdvertModel, AnimalAdvertViewHolder> newAdapter =
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
                                                    @NonNull final AnimalAdvertModel model) {
                        Log.d(TAG,"Holder: new Holder created.");
                        String advertTitle = model.getTitle();
                        String imageName = model.getImage();
                        holder.setTitle(advertTitle);
                        holder.setImage(imageName);

                        holder.getAdvertView().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG,"card clicked");
                                Bundle arguments = new Bundle();

                                arguments.putParcelable("advert", model);
                                arguments.putParcelable("image", holder.getImageBitmap());

                                Fragment detailedAdvert = new DetailedAnimalFragment();

                                detailedAdvert.setArguments(arguments);

                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.add(R.id.fragmentContainer, detailedAdvert);
                                ft.addToBackStack(null);
                                ft.commit();
                            }
                        });
                    }
                };

        return newAdapter;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final MenuItem search = menu.add("Search");
        search.setVisible(true);
        search.setIcon(R.drawable.ic_search);
        search.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        search.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == search.getItemId()) {
                    Log.d(TAG, "my icon clicked");

                    if (searchMenu.isVisible()) {
                        getActivity().getSupportFragmentManager().popBackStack();
                        return true;
                    } else {
                        Bundle arguments = new Bundle();

                        arguments.putString("collection", collection);
                        searchMenu.setArguments(arguments);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.add(R.id.fragmentContainer, searchMenu);
                        ft.addToBackStack(null);
                        ft.commit();
                        return true;
                    }
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
}