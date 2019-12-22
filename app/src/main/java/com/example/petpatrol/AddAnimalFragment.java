package com.example.petpatrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AddAnimalFragment extends Fragment implements OnMapReadyCallback {

    ResetButtons _mResetListener;

    private FirebaseFirestore firestoreDB;
    private ImageView imageThumbnail;
    private MapView animalLocation;
    private GoogleMap map;
    private FusedLocationProviderClient locationProvider;
    private EditText searchText;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "AddAnimalFragment";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private boolean localizationPermitted = true;

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

        animalLocation = view.findViewById(R.id.animal_location);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        animalLocation.onCreate(mapViewBundle);
        animalLocation.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is ready.");
        getDeviceLocation();
        map = googleMap;

        LatLng position = new LatLng(52.5, 13.4);

        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);
        map.addMarker(new MarkerOptions().position(position));
        map.moveCamera(CameraUpdateFactory.newLatLng(position));

        init();
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        locationProvider = LocationServices.getFusedLocationProviderClient(getContext());

        try{
            if(localizationPermitted){

                final Task location = locationProvider.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful() && (task.getResult() != null)){
                            Log.d(TAG, "onComplete: found location!");

                            Location currentLocation = (Location) task.getResult();
                            LatLng position = new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude());

                            moveCamera(position,12);

                        }else{
                            Log.d(TAG, "getDeviceLocation: current location is null");
                            Toast.makeText(getContext(), "unable to get current location",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
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
                Log.d(TAG, "addButton");

                EditText advertTitle = foundLayout.findViewById(R.id.animal_advert_title);
                EditText advertAnimal = foundLayout.findViewById(R.id.spinner_animal);
                EditText advertColor = foundLayout.findViewById(R.id.spinner_color);
                EditText advertSize = foundLayout.findViewById(R.id.spinner_size);
                EditText advertTagType = foundLayout.findViewById(R.id.spinner_tag_type);
                EditText advertTag = foundLayout.findViewById(R.id.tag);

                Map<String, Object> advert = new HashMap<>();
                advert.put("title", advertTitle.getText().toString());
                advert.put("animal", advertAnimal.getText().toString());
                advert.put("color", advertColor.getText().toString());
                advert.put("size", advertSize.getText().toString());
                advert.put("tag_type", advertTagType.getText().toString());
                advert.put("tag", advertTag.getText().toString());


                firestoreDB.collection("lost")
                        .add(advert)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "Event document added - id: "
                                        + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding event document", e);
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
                Log.d(TAG, "exitButton");
                getActivity().findViewById(R.id.lostButton).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.foundButton).setVisibility(View.VISIBLE);
                getFragmentManager().popBackStack();
            }
        });

        fillUpSpinner(view.findViewById(R.id.spinner_animal), R.array.selection_animal);
        fillUpSpinner(view.findViewById(R.id.spinner_color), R.array.selection_color);
        fillUpSpinner(view.findViewById(R.id.spinner_size), R.array.selection_size);
        fillUpSpinner(view.findViewById(R.id.spinner_tag_type), R.array.selection_tag_type);

        imageThumbnail = view.findViewById(R.id.animal_thumbnail);

        Button addImageButton = view.findViewById(R.id.add_animal_image);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "addImageButton");
                dispatchTakePictureIntent();
            }
        });

        searchText = (EditText) view.findViewById(R.id.add_animal_location_input);
    }

    private void init(){
        Log.d(TAG, "init: initializing");

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                    
                    geoLocate();
                }

                return false;
            }
        });
    }

    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = searchText.getText().toString();

        Geocoder geocoder = new Geocoder(getContext());
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            map.clear();
            LatLng position = new LatLng( address.getLatitude(), address.getLongitude());
            moveCamera(position, 12);
            map.addMarker(new MarkerOptions().position(position));
        }
    }

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if (imageBitmap != null) {
                imageThumbnail.setImageBitmap(imageBitmap);
            }
        }
    }

    private void fillUpSpinner(View spinner, int selectionId) {
        Spinner menu = (Spinner) spinner;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                selectionId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        menu.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        animalLocation.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        animalLocation.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        animalLocation.onDestroy();
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

        animalLocation.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        animalLocation.onLowMemory();
    }
}
