package com.example.petpatrol;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
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

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;


public class AddAnimalFragment extends Fragment implements OnMapReadyCallback,
        ImageDialogFragment.ImageDialogListener {

    ResetButtons _mResetListener;

    private FirebaseFirestore fbFirestore;
    private FirebaseStorage fbStorage;
    private ImageView imageThumbnail;
    private MapView animalLocation;
    private GoogleMap map;
    private Uri imageUri = null;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 2;
    private static final int REQUEST_IMAGE_GET = 3;
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
        fbFirestore = FirebaseFirestore.getInstance();
        fbStorage = FirebaseStorage.getInstance();

        initMapView(view, savedInstanceState);

        return view;
    }

    public void initMapView(View view, Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        animalLocation = view.findViewById(R.id.animal_location);
        animalLocation.onCreate(mapViewBundle);
        animalLocation.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is ready.");

        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(false);

        setInitialLocation();
    }

    private void setInitialLocation(){
        Log.d(TAG, "setInitialLocation: getting the devices current location");

        if(!setDeviceLocationSuccessfully()){
            Log.d(TAG, "setInitialLocation: Setting default location.");
            setDefaultLocation();
        }
    }

    private boolean setDeviceLocationSuccessfully() {
        FusedLocationProviderClient locationProvider = LocationServices
                .getFusedLocationProviderClient(getContext());

        try{
            if(localizationPermitted){

                final Task location = locationProvider.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && (task.getResult() != null)) {
                            Log.d(TAG, "onComplete: found location!");

                            Location location = (Location) task.getResult();

                            map.getUiSettings().setMyLocationButtonEnabled(true);
                            map.setMyLocationEnabled(true);
                            moveCamera(new LatLng(location.getLatitude(), location.getLongitude()));
                        }
                    }
                });
                return true;
            } else {
                Log.d(TAG, "setInitialLocation: current location is null.");
                Toast.makeText(getContext(), "unable to get current location",
                        Toast.LENGTH_SHORT).show();
            }
        }catch (SecurityException e){
            Log.e(TAG, "setInitialLocation: SecurityException: " + e.getMessage() );
        }
        return false;
    }

    private void setDefaultLocation() {
        LatLng positionHamburg = new LatLng(52.5, 13.4);
        map.addMarker(new MarkerOptions().position(positionHamburg));
        map.moveCamera(CameraUpdateFactory.newLatLng(positionHamburg));
    }

    private void moveCamera(LatLng latLng){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " +
                latLng.latitude +
                ", lng: " +
                latLng.longitude );
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getActivity().findViewById(R.id.lostButton).setVisibility(View.GONE);
        getActivity().findViewById(R.id.foundButton).setVisibility(View.GONE);

        initAddButton(view);
        initExitButton(view);
        initAddImageButton(view);
        initSpinners(view);

        imageThumbnail = view.findViewById(R.id.animal_thumbnail);

        Places.initialize(getContext(), getResources().getString(R.string.google_maps_key));
        Places.createClient(getContext());

        List<Place.Field> fields = Arrays.asList(Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(fields);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d(TAG, "Place: " + place.getName() + ", " + place.getId());
                map.clear();
                map.addMarker(new MarkerOptions().position(place.getLatLng()));
                moveCamera(place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                Log.d(TAG, "An error occurred: " + status);
            }
        });

    }

    private void initAddButton(View view) {
        final FrameLayout foundLayout = (FrameLayout) view.getParent();
        FloatingActionButton addButton = foundLayout.findViewById(R.id.floatingAddButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean finished = false;
                Log.d(TAG, "addButton");

                EditText advertTitle = foundLayout.findViewById(R.id.animal_advert_title);
                Spinner advertAnimal = foundLayout.findViewById(R.id.spinner_animal);
                Spinner advertColor = foundLayout.findViewById(R.id.spinner_color);
                Spinner advertSize = foundLayout.findViewById(R.id.spinner_size);
                Spinner advertTagType = foundLayout.findViewById(R.id.spinner_tag_type);
                EditText advertTag = foundLayout.findViewById(R.id.tag);

                String imageUUID = UUID.randomUUID().toString();

                if (imageUri != null) {
                    uploadImage(imageUri, imageUUID);
                    finished = true;
                }

                Map<String, Object> advert = new HashMap<>();
                advert.put("title", advertTitle.getText().toString());
                advert.put("animal", advertAnimal.getSelectedItem().toString());
                advert.put("color", advertColor.getSelectedItem().toString());
                advert.put("size", advertSize.getSelectedItem().toString());
                advert.put("tag_type", advertTagType.getSelectedItem().toString());
                advert.put("tag", advertTag.getText().toString());
                advert.put("image", "images/" + imageUUID + ".jpg");

                fbFirestore.collection("lost")
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

                if (finished) {
                    getFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "Please add a Picture.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initExitButton(View view) {
        final FrameLayout foundLayout = (FrameLayout) view.getParent();
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
    }

    private void initAddImageButton(View view) {
        final ImageDialogFragment test = new ImageDialogFragment();
        test.setTargetFragment(this, 1);

        Button addImageButton = view.findViewById(R.id.add_animal_image);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "addImageButton");
                test.show(getFragmentManager(), "test");
            }
        });
    }

    private void initSpinners(View view) {
        fillUpSpinner(view.findViewById(R.id.spinner_animal), R.array.selection_animal);
        fillUpSpinner(view.findViewById(R.id.spinner_color), R.array.selection_color);
        fillUpSpinner(view.findViewById(R.id.spinner_size), R.array.selection_size);
        fillUpSpinner(view.findViewById(R.id.spinner_tag_type), R.array.selection_tag_type);
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
        Log.d(TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            File outputDir = getContext().getCacheDir();
            try {
                File outputFile = File.createTempFile("petpatrol_temp_img", "jpg", outputDir );
                OutputStream stream = new FileOutputStream(outputFile);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                stream.close();
                imageUri = Uri.fromFile(outputFile);
                Log.d(TAG, "onActivityResult: temp_image_stored.");
            }catch (IOException e) {
                Log.d(TAG, "onActivityResult: IOException");
            }


            if (imageBitmap != null) {
                imageThumbnail.setImageBitmap(imageBitmap);
            }
        }
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "got result von intent");
        }
        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK) {
            Bitmap thumbnail = null;
            try {
                thumbnail = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),
                        data.getData());
            } catch (FileNotFoundException e){
                Log.d(TAG, "onActivityResult: Picture file not found.");
            } catch (IOException e) {
                Log.d(TAG, "onActivityResult: IOException");
            }

            imageUri = data.getData();

            if (thumbnail == null) {
                Toast.makeText(getContext(), "Could not load Thumbnail for this image.",
                        Toast.LENGTH_SHORT).show();
                imageThumbnail.setImageURI(imageUri);
            } else {
                imageThumbnail.setImageBitmap(thumbnail);
            }
            Log.d(TAG, "onActivityResult: return from Gallery Intent" + data.getData());
        }
    }

    private void uploadImage(Uri picture, String identifier) {
        Log.d(TAG, "uploadImage: Picture Path: " + picture.getPath());

        StorageReference storageRef = fbStorage.getReference();
        StorageReference pictureRef = storageRef.child("images/" + identifier + ".jpg");
        Bitmap bitmap = null;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), picture);
        } catch (FileNotFoundException e){
            Log.d(TAG, "uploadImage: Picture file not found.");
        } catch (IOException e) {
            Log.d(TAG, "uploadImage: IOException");
        }

        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap image = scaleBitmap(bitmap,800, true);
            image.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] data = baos.toByteArray();
            Log.d(TAG, "uploadImage: Starting uploading picture.");
            pictureRef.putBytes(data).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(TAG, "uploadImage: Picture upload failed.");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "uploadImage: Picture upload successfully.");
                }
            });
        } else {
            Log.d(TAG, "uploadImage: Bitmap could not be created.");
        }
    }

    private Bitmap scaleBitmap(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    @Override
    public void onTakePictureClick(DialogFragment dialog) {
        dispatchTakePictureIntent();
    }

    @Override
    public void onChoosePictureClick(DialogFragment dialog) {
        choosePicture();
    }

    private void choosePicture() {
        if (ActivityCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE) == getActivity().getPackageManager().PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 4);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 4:  {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choosePicture();
                }
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
