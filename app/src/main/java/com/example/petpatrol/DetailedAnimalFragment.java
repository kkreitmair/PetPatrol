package com.example.petpatrol;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DetailedAnimalFragment extends Fragment implements OnMapReadyCallback {

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

    private LinearLayout fragment;
    private LatLng animalLocation;
    private MapView mapView;
    private GoogleMap map;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String TAG = "DetailedAnimalFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        getActivity().findViewById(R.id.lostButton).setVisibility(View.GONE);
        getActivity().findViewById(R.id.foundButton).setVisibility(View.GONE);
        ( (FloatingActionButton) getActivity().findViewById(R.id.addButton)).hide();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        View view = inflater.inflate(R.layout.detailed_advert, parent, false);
        fragment = view.findViewById(R.id.detailed_advert);

        initMapView(fragment, savedInstanceState);

        ImageView image = fragment.findViewById(R.id.detailed_animal_image);

        TextView animalText = fragment.findViewById(R.id.detailed_animal_animal);
        TextView colorText = fragment.findViewById(R.id.detailed_animal_color);
        TextView sizeText = fragment.findViewById(R.id.detailed_animal_size);
        TextView tagTypeText = fragment.findViewById(R.id.detailed_animal_tag_type);
        TextView tagText = fragment.findViewById(R.id.detailed_animal_tag);

        AnimalAdvertModel advert = (AnimalAdvertModel) getArguments().getParcelable("advert");
        Bitmap bitmap = getArguments().getParcelable("image");

        image.setImageBitmap(bitmap);

        animalLocation = advert.getPosition();
        animalText.setText(getString(R.string.label_animal) + " " + advert.getAnimal());
        colorText.setText(getString(R.string.label_color) + " " + advert.getColor());
        sizeText.setText(getString(R.string.label_size) + " " + advert.getSize());
        tagTypeText.setText(getString(R.string.label_tag_type) + " " + advert.getTagType());
        tagText.setText(getString(R.string.label_tag) + " " + advert.getTag());

        return view;
    }

    public void initMapView(View view, Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView = view.findViewById(R.id.detailed_animal_location);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is ready.");

        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(false);
        if (animalLocation == null) {
            animalLocation =  new LatLng(52.5, 13.4);
        }
        map.addCircle(new CircleOptions().center(animalLocation).radius(2500.0)).setStrokeColor(Color.RED);
        map.addMarker(new MarkerOptions().position(animalLocation));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom( animalLocation, 10));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _mResetListener.resetButtons();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
