package com.example.petpatrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
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
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;


public class AddAnimalFragment extends Fragment implements OnMapReadyCallback,
        ImageDialogFragment.ImageDialogListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMapLongClickListener {

    ResetButtons _mResetListener;

    private FirebaseFirestore fbFirestore;
    private FirebaseStorage fbStorage;
    private ImageView imageThumbnail;
    private MapView animalLocation;
    private GoogleMap map;
    private Uri imageUri = null;
    private LatLng animalPos = null;
    private boolean tagEnabled = false;
    private boolean inputIsValid = true;
    private String collection;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PERMISSION_GET_LOCATION = 2;
    private static final int REQUEST_IMAGE_GET = 3;
    private static final int PERMISSION_CHOOSE_PICTURE = 4;
    private static final int DEFAULT_CAMERA_ZOOM = 10;
    private static final int MAX_IMAGE_SIZE = 800;
    private static final int IMAGE_QUALITY = 75;
    private static final String TAG = "AddAnimalFragment";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

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
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        fbFirestore = FirebaseFirestore.getInstance();
        fbStorage = FirebaseStorage.getInstance();

        String role = getArguments().getString("role");

        if (role == null) {
            Log.e(TAG, "No value found for role in fragment arguments.");
            getFragmentManager().popBackStack();
        } else {
            if (role.equals("lost") || role.equals("found")) {
                Log.d(TAG, "Role: " + role);
                collection = role;
            } else {
                Log.e(TAG, "No matching role Value in fragment arguments.");
                getFragmentManager().popBackStack();
            }
        }

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
        map.setOnMyLocationClickListener(this);
        map.setOnMapLongClickListener(this);
        map.getUiSettings().setZoomControlsEnabled(false);

        setInitialLocation();
    }

    private void setInitialLocation(){
        Log.d(TAG, "setInitialLocation: getting the devices current location");

        if(!setDeviceLocationSuccessful()){
            Log.d(TAG, "setInitialLocation: Setting default location.");
        }
    }

    private boolean setDeviceLocationSuccessful() {
        FusedLocationProviderClient locationProvider = LocationServices
                .getFusedLocationProviderClient(getContext());

        int permission = ActivityCompat.checkSelfPermission(getContext(), ACCESS_COARSE_LOCATION);
        int granted = getActivity().getPackageManager().PERMISSION_GRANTED;

        if (permission == granted) {
            try{
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
            }catch (SecurityException e){
                Log.e(TAG, "setInitialLocation: SecurityException: " + e.getMessage() );
            }
        } else {
            Log.d(TAG, "setInitialLocation: unable to set current location. Permissions.");
            requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, PERMISSION_GET_LOCATION);
        }
        return false;
    }

    private void setDefaultLocation() {
        LatLng positionHamburg = new LatLng(52.5, 13.4);
        map.moveCamera(CameraUpdateFactory.newLatLng(positionHamburg));
    }

    private void moveCamera(LatLng latLng){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_CAMERA_ZOOM));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initAddButton(view);
        initExitButton(view);
        initAddImageButton(view);
        initSpinners(view);
        initSearchPlace();

        imageThumbnail = view.findViewById(R.id.animal_thumbnail);
    }

    private void initAddButton(View view) {
        final FrameLayout layout = (FrameLayout) view.getParent();
        FloatingActionButton addButton = layout.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> advert = createAdvert(layout);

                if (advert != null) {
                    uploadAdvert(advert);
                    getFragmentManager().popBackStack();
                }
            }
        });
    }

    private Map<String, Object> createAdvert(FrameLayout layout) {
        inputIsValid = true;
        FirebaseAuth firebase = FirebaseAuth.getInstance();

        EditText advertTitle = layout.findViewById(R.id.animal_advert_title);
        Spinner advertAnimal = layout.findViewById(R.id.spinner_animal);
        Spinner advertColor = layout.findViewById(R.id.spinner_color);
        Spinner advertSize = layout.findViewById(R.id.spinner_size);
        Spinner advertTagType = layout.findViewById(R.id.spinner_tag_type);
        EditText advertTag = layout.findViewById(R.id.tag);

        String tagType = null;
        String tag = null;

        Map<String, Object> advert = new HashMap<>();

        String title = advertTitle.getText().toString();
        String titleHint = getString(R.string.add_animal_hint_title);

        validateInput(title, titleHint);

        String animal = advertAnimal.getSelectedItem().toString();
        String animalHint = getString(R.string.add_animal_hint_animal);

        validateInput(animal, animalHint);

        String size = advertSize.getSelectedItem().toString();
        String sizeHint = getString(R.string.add_animal_hint_size);

        validateInput(size, sizeHint);

        String color = advertColor.getSelectedItem().toString();
        String colorHint = getString(R.string.add_animal_hint_color);

        validateInput(color, colorHint);

        if (tagEnabled) {
            tagType = advertTagType.getSelectedItem().toString();
            String tagTypeHint = getString(R.string.add_animal_hint_tag_type);

            validateInput(tagType, tagTypeHint);

            tag = advertTag.getText().toString();
            String tagHint = getString(R.string.add_animal_hint_tag);

            validateInput(tag, tagHint);
        }

        FirebaseUser currentUser = firebase.getCurrentUser();

        validateInput(currentUser, getString(R.string.add_animal_hint_user));
        validateInput(animalPos, getString(R.string.add_animal_hint_location) );

        if (inputIsValid) {
            advert.put("title", title);
            advert.put("animal", animal);
            advert.put("size", size);
            advert.put("color", color);
            if (tagEnabled) {
                advert.put("tag_type", tagType);
                advert.put("tag", tag);
            } else {
                advert.put("tag_type", "");
                advert.put("tag", "");
            }
            advert.put("user_id", currentUser.getUid());
            advert.put("position", animalPos);
            advert.put("created", FieldValue.serverTimestamp());

            String imageUUID = UUID.randomUUID().toString();

            if (imageUri != null) {
                uploadImage(imageUri, imageUUID);
                advert.put("image", "images/" + imageUUID + ".jpg");
            } else {
                advert.put("image", getStockImage(animal));
            }
        } else {
            return null;
        }

        return advert;
    }

    private void validateInput(Object input, String inputHint) {
        if (inputIsValid) {
            if (input != null) {
                if (input instanceof String) {
                    String text = (String) input;
                    if (text.isEmpty() || text.equals(inputHint)) {
                        Toast.makeText(getContext(), inputHint, Toast.LENGTH_SHORT).show();
                        inputIsValid = false;
                    }
                }
            } else {
                Toast.makeText(getContext(), inputHint, Toast.LENGTH_SHORT).show();
                inputIsValid = false;
            }
        }
    }

    private String getStockImage(String animal) {
        String image = getString(R.string.stock_image_other);

        if (animal.equals(getString(R.string.animal_dog))) {
            image = getString(R.string.stock_image_dog);
        } else if (animal.equals(getString(R.string.animal_cat))) {
            image = getString(R.string.stock_image_cat);
        } else if (animal.equals(getString(R.string.animal_small_animal))) {
            image = getString(R.string.stock_image_small_animal);
        } else if (animal.equals(getString(R.string.animal_bird))) {
            image = getString(R.string.stock_image_bird);
        } else if (animal.equals(getString(R.string.animal_lizard))) {
            image = getString(R.string.stock_image_lizard);
        } else if (animal.equals(getString(R.string.animal_farm_animal))) {
            image = getString(R.string.stock_image_farm_animal);
        }
        return image;
    }

    private void uploadAdvert(Map<String, Object> advert) {
        fbFirestore.collection(collection)
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
    }

    private void initExitButton(View view) {
        final FrameLayout foundLayout = (FrameLayout) view.getParent();
        FloatingActionButton exitButton = foundLayout.findViewById(R.id.floatingExitButton);
        exitButton.show();
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "exitButton");
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
        initSpinner(view.findViewById(R.id.spinner_animal), R.array.selection_animal);
        initSpinner(view.findViewById(R.id.spinner_color), R.array.selection_color);
        initSpinner(view.findViewById(R.id.spinner_size), R.array.selection_size);

        Spinner tagSpinner = initSpinner(view.findViewById(R.id.spinner_tag_type), R.array.selection_tag_type);
        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout foundLayout = (LinearLayout) parent.getParent();
                if ((parent.getCount() - 1) != id) {
                    Log.d(TAG, "Tag Type selected.");
                    tagEnabled = true;
                    foundLayout.findViewById(R.id.tag).setEnabled(true);
                } else {
                    tagEnabled = false;
                    foundLayout.findViewById(R.id.tag).setEnabled(false);
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

    private void initSearchPlace() {
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
                setMapPosition(place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                Log.d(TAG, "An error occurred: " + status);
            }
        });
    }

    private void takePicture() {
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
                File outputFile = File.createTempFile("petpatrol_temp", "jpg", outputDir);
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
            Bitmap image = scaleBitmap(bitmap,MAX_IMAGE_SIZE, true);
            image.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, baos);
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
        takePicture();
    }

    @Override
    public void onChoosePictureClick(DialogFragment dialog) {
        choosePicture();
    }

    private void choosePicture() {
        int permission = ActivityCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE);
        int granted = getActivity().getPackageManager().PERMISSION_GRANTED;
        if (permission == granted) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        } else {
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, PERMISSION_CHOOSE_PICTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        boolean hasResults = grantResults.length > 0;
        switch (requestCode) {
            case PERMISSION_CHOOSE_PICTURE:
                if (hasResults && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choosePicture();
                } else {
                    Log.d(TAG, "Permissions for choosing picture not granted!");
                }
                return;
            case PERMISSION_GET_LOCATION:
                if ( hasResults && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setInitialLocation();
                } else {
                    Log.d(TAG, "Permissions for getting current location not granted!");
                    setDefaultLocation();
                }
                return;
        }
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

        animalLocation.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        animalLocation.onLowMemory();
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Log.d(TAG, "Current Locaton clicked");
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
        setMapPosition(position);
    }

    @Override
    public void onMapLongClick(LatLng position) {
        Log.d(TAG, "On Map Clicked.");
        setMapPosition(position);
    }

    private void setMapPosition(LatLng position) {
        animalPos = position;
        map.clear();
        map.addMarker(new MarkerOptions().position(position));
        moveCamera(position);
        Toast.makeText(getContext(), "Position ausgewählt.",
                Toast.LENGTH_SHORT).show();
    }
}
