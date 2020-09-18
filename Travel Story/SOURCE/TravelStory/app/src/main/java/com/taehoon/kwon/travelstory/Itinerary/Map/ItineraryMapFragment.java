package com.taehoon.kwon.travelstory.Itinerary.Map;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.app.FragmentManager;

import com.felipecsl.quickreturn.library.widget.QuickReturnAdapter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.maps.MapFragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.taehoon.kwon.travelstory.Itinerary.Record.ItineraryRecordFragment;
import com.taehoon.kwon.travelstory.OnBackPressed;
import com.taehoon.kwon.travelstory.Itinerary.StoryBook.StoryBookContainerFragment;
import com.taehoon.kwon.travelstory.Itinerary.StoryBook.StoryBookNewRecordFragment;
import com.taehoon.kwon.travelstory.Itinerary.model.ItineraryLocation;
import com.taehoon.kwon.travelstory.Itinerary.model.MarkerList;
import com.taehoon.kwon.travelstory.Itinerary.model.ParcelableGeoPoint;
import com.taehoon.kwon.travelstory.Itinerary.model.PolylineData;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.HORIZONTAL;

public class ItineraryMapFragment extends StoryBookContainerFragment implements OnBackPressed, OnMapReadyCallback {

    public static final String FRAG_TAG = "story_book_itinerary_map";
    private static final String TAG = "ItineraryMapFragment";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9003;
    private static final float DEFAULT_ZOOM = 15f;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private View root;
    private MapView mapView;
    private FragmentManager fragmentManager;
    private boolean isLocationPermissionGranted = false;
    private boolean isGpsEnabled = false;
    private boolean isLocSearchChangedByClickOnItem = false;

    private ListView autocompleteLocationListView;
    private AutocompleteLocationListAdapter autocompleteLocationAdapter;
    private ArrayList<AutocompletePrediction> autocompleteLocationList = new ArrayList<>();
    private ProgressBar searchLocationLoading;
    private RecyclerView recyclerView;
    private ItineraryMapAdapter itineraryMapAdapter;
    private ArrayList<ItineraryLocation> itineraryLocationList = new ArrayList<>();
    private List<LatLng> markerLatLngList = new ArrayList<>();
    private ArrayList<PolylineData> directionPolylines = new ArrayList<>(); // directionPolylines.size() == markerLatLngList.size() -1
    private List<Integer> markerIndexMovedList = new ArrayList<>();
    /*  m0  m1  m2  m3  m4  m5  m6  m7  m8   marker id // m0 does not exist
    *  [0,  0,  0,  0,  0,  0,  0]          initial state
    *  [0,  0,  0,  ?, -1, -1, -1]          after m3 removed (? for removed flag. The number does not have meaning anymore)
    *  [0,  ?, -1,  ?, -2, -2, -2]          after m1 removed (? for removed flag. The number does not have meaning anymore)
    *  [0,  ?, -1,  ?, -2, -2, -2, -2]      after new marker m7 added (copy the value of the previous index)
    *  [0,  ?, -1,  ?, -2, -2, -2, -1]      after m7 removed (if last element is removed, keep the value as -1)
    *  [0,  ?, -1,  ?, -2, -2, -2, -1, -1]  after new marker m8 added (copy the value of the previous index)
    * */

    private GoogleMap gMap;
    private static GeoApiContext geoApiContext = null;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation = null;
    private LatLng initialLocation = new LatLng(47.688955, -122.150186);
    private PlacesClient placesClient;

    private EditText searchText;
    private ImageView gpsWidget;
    private ImageView mapTypeWidget;
    private ImageView itineraryWidget;
    private ImageView placePickerWidget;
    private ImageView writeItineraryWidget;
    private PopupMenu mapTypePopupMenu = null;
    private PopupMenu itineraryPopupMenu = null;

    private Boolean isEditing = false;
    private Boolean isPlaceRemoved = false;
    private Boolean isPlaceRemoved_for_backPressed = false;
    private int removedIndex = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("FRAG_TAG", "Before Frag tag : " + MainActivity.current_fragment_tag);
        MainActivity.current_fragment_tag = FRAG_TAG;
        Log.d("FRAG_TAG", "Current Frag tag : " + MainActivity.current_fragment_tag);

        root = inflater.inflate(R.layout.fragment_itinerary_map, container, false);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);

        if (getArguments() != null) {
            MarkerList.getInstance().CreateNewMarkerList(getArguments().getString("story_title"));
            isEditing = getArguments().getBoolean("edit_mode", false);
            isPlaceRemoved = getArguments().getBoolean("place_removed", false);
            removedIndex = getArguments().getInt("update_needed_marker_for_broken_path", -1);

            if (isEditing)
                itineraryLocationList = getArguments().getParcelableArrayList("edit_itinerary_list");
            if (isPlaceRemoved) {
                itineraryLocationList = getArguments().getParcelableArrayList("removed_itinerary_list");
                isPlaceRemoved_for_backPressed = true;
            }

            Log.d("Remove Test", "Remove Test : ItineraryMap.onCreateView - isEditing = " + isEditing);
        }

        if (!Places.isInitialized())
            Places.initialize(getActivity(), getString(R.string.google_map_api_key));
        else
            Places.createClient(getActivity());
        placesClient = Places.createClient(getActivity());

        if(checkMapServices()) {
            if(!isLocationPermissionGranted)
                getLocationPermission();
        }
        isGpsEnabled = isGpsOn();
        isLocationPermissionGranted = isLocOn();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // fragment manager for fragment transaction, not for map fragment
        fragmentManager = getActivity().getSupportFragmentManager();

        Log.d("Backstack Count: ", fragmentManager.getBackStackEntryCount() + " - ItineraryMap");

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView = (MapView)root.findViewById(R.id.googleMap);
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);

        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.server_key))
                    .build();
        }

        gpsWidget = root.findViewById(R.id.ic_gps);
        mapTypeWidget = root.findViewById(R.id.ic_map);
        itineraryWidget = root.findViewById(R.id.ic_itinerary);
        placePickerWidget = root.findViewById(R.id.ic_place);
        writeItineraryWidget = root.findViewById(R.id.ic_writeItinerary);
        searchText = root.findViewById(R.id.inputSearch);
        searchLocationLoading = root.findViewById(R.id.searchLocationLoading);
        recyclerView = root.findViewById(R.id.itineraryRecyclerView);
        autocompleteLocationListView = root.findViewById(R.id.autocompleteLocationListView);
        itineraryMapAdapter = new ItineraryMapAdapter(getContext(), itineraryLocationList);
        autocompleteLocationAdapter = new AutocompleteLocationListAdapter(getContext(), autocompleteLocationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), HORIZONTAL, false));
        autocompleteLocationListView.setAdapter(new QuickReturnAdapter(autocompleteLocationAdapter));
        autocompleteLocationListView.setOnItemClickListener(onAutoCompletedLocationClickListener);

        gpsWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });
        mapTypeWidget.setOnClickListener(mapTypeWidgetClickListener);
        itineraryWidget.setOnClickListener(itineraryWidgetClickListener);
        placePickerWidget.setOnTouchListener(new WidgetTouchListener());
        placePickerWidget.setOnDragListener(new WidgetDragDropListener());
        writeItineraryWidget.setOnClickListener(writeItineraryWidgetClickListener);

        return root;
    }

    @Override
    public void onBackPressed() {

        if (isPlaceRemoved_for_backPressed) {
            if (!isAllPathSelected()) {
                requestSelectingPath();
                return;
            }
            else
                isPlaceRemoved_for_backPressed = false;
        }

        int numOfMarkers = MarkerList.getInstance().markers.size();

        if (numOfMarkers > 0) {
            MarkerList.RegisteredMarker lastMarker = MarkerList.getInstance().markers.get(numOfMarkers - 1);
            if (lastMarker.registered == false)
                RemoveMarker(lastMarker.marker);
        }

        Bundle args = new Bundle();
        args.putParcelableArrayList("changed_itineraryLocationList", itineraryLocationList);

        Fragment fragment = fragmentManager.findFragmentByTag(StoryBookNewRecordFragment.FRAG_TAG);

        if (fragment == null) {
            fragment = new StoryBookNewRecordFragment();
            fragment.setArguments(args);
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment, StoryBookNewRecordFragment.FRAG_TAG)
                    .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                    .commit();
        }
        else {
            fragment.setArguments(args);
            fragmentManager.beginTransaction()
                    .show(fragment)
                    .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                    .commit();
        }

        Log.d("FRAG_TAG", "Current Frag tag : " + MainActivity.current_fragment_tag);
        MainActivity.current_fragment_tag = StoryBookNewRecordFragment.FRAG_TAG;
        Log.d("FRAG_TAG", "After Frag tag : " + MainActivity.current_fragment_tag);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        Log.d("ItineraryMapFragment", "ItineraryMapFragment onHiddenChanged called : " + hidden);

        if (!hidden) {
            if (getArguments() != null) {
                Log.d("ItineraryMapFragment", "ItineraryMapFragment : getArguments!");
                boolean recordCancelled = getArguments().getBoolean("record_cancelled", false);
                isPlaceRemoved = getArguments().getBoolean("place_removed", false);
                removedIndex = getArguments().getInt("update_needed_marker_for_broken_path", -1);

                Log.d("Remove Test", "Remove Test : ItineraryMap.onHiddenChanged - isPlaceRemoved = " + isPlaceRemoved);

                if (recordCancelled) {
                    placePickerWidget.setVisibility(View.GONE);
                    writeItineraryWidget.setVisibility(View.VISIBLE);
                }
                if (isPlaceRemoved) {
                    itineraryLocationList = getArguments().getParcelableArrayList("removed_itinerary_list");
                    isPlaceRemoved_for_backPressed = true;

                    for (PolylineData polylineData : directionPolylines) {
                        for (Polyline line : polylineData.polylines)
                            line.remove();
                        polylineData.polylines.clear();
                        polylineData.legs.clear();
                        polylineData.paths_in_GeoPoint.clear();
                    }

                    for (MarkerList.RegisteredMarker registeredMarker : MarkerList.getInstance().markers) {
                        registeredMarker.direction_points_from_previous_location.clear();
                        registeredMarker.marker.remove();
                    }

                    setNewMarkerIndexMovedList();
                    markerLatLngList.clear();
                    directionPolylines.clear();
                    MarkerList.getInstance().markers.clear();
                    loadMarkersAndDirectionsFromDatabase();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");

        try {
            if (isLocationPermissionGranted) {
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();

                            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()),
                                    DEFAULT_ZOOM));

                            gMap.setMyLocationEnabled(true);
                        }
                        else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: " + task.getException());
                            lastKnownLocation.setLatitude(initialLocation.latitude);
                            lastKnownLocation.setLongitude(initialLocation.longitude);
                        }
                    }
                });
            }
        } catch(SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        // Do other setup activities here too, as described elsewhere in this tutorial.
        mapInit();

        // Turn on the My Location layer and the related control on the map.
        gMap.setOnMarkerDragListener(markerDragListener);
        gMap.setOnInfoWindowClickListener(infoWindowClickListener);
        gMap.setOnPolylineClickListener(directionClickListener);

        // Get the current location of the device and set the position of the map.
        if (!isEditing)
            getDeviceLocation();
    }

    private Boolean isAllPathSelected() {
        for (PolylineData polylineData : directionPolylines) {
            if (polylineData.selectedPathIndex == -1)
                return false;
        }
        return true;
    }

    private View.OnClickListener writeItineraryWidgetClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!isAllPathSelected()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Please select the path")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
                return;
            }

            placePickerWidget.setVisibility(View.VISIBLE);
            writeItineraryWidget.setVisibility(View.GONE);

            Bundle args = new Bundle();
            args.putParcelableArrayList("changed_itineraryLocationList", itineraryLocationList);

            Fragment fragment = fragmentManager.findFragmentByTag(ItineraryRecordFragment.FRAG_TAG);

            if (fragment == null) {
                fragment = new ItineraryRecordFragment();
                fragment.setArguments(args);
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, fragment, ItineraryRecordFragment.FRAG_TAG)
                        .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                        .commit();
            }
            else {
                fragment.setArguments(args);
                fragmentManager.beginTransaction()
                        .show(fragment)
                        .commit();
            }
            MainActivity.current_fragment_tag = ItineraryRecordFragment.FRAG_TAG;
        }
    };

    private View.OnClickListener mapTypeWidgetClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mapTypePopupMenu == null) {
                mapTypePopupMenu = new PopupMenu(getContext(), v);
                MenuInflater inflater = mapTypePopupMenu.getMenuInflater();
                inflater.inflate(R.menu.map_type_menu, mapTypePopupMenu.getMenu());
            }

            mapTypePopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.normalMapType:
                            item.setChecked(true);
                            gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            return true;
                        case R.id.satelliteMapType:
                            item.setChecked(true);
                            gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                            return true;
                        case R.id.terrainMapType:
                            item.setChecked(true);
                            gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                            return true;

                        default:
                            return false;
                    }
                }
            });
            mapTypePopupMenu.show();
        }
    };

    private View.OnClickListener itineraryWidgetClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (itineraryPopupMenu == null) {
                itineraryPopupMenu = new PopupMenu(getContext(), v);
                MenuInflater inflater = itineraryPopupMenu.getMenuInflater();
                inflater.inflate(R.menu.itinerary_map_menu, itineraryPopupMenu.getMenu());
            }

            itineraryPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.showItinerary:
                            if(item.isChecked()) {
                                item.setChecked(false);
                                recyclerView.setVisibility(View.GONE);
                            }
                            else {
                                item.setChecked(true);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                            return true;

                        default:
                            return false;
                    }
                }
            });
            itineraryPopupMenu.show();
        }
    };

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    /* This method is responsible for determining whether the device is able to use Google Services */
    private boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            ToastMessage("You can't make map requests");
        }
        return false;
    }

    private boolean isGpsOn() {
        final LocationManager manager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /* This method is responsible for determining whether the application has GPS enabled on the device */
    private boolean isMapsEnabled(){
        if (!isGpsOn()) {
            buildAlertMessageNoGps();
            return false;
        }
        else
            isGpsEnabled = true;
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(isLocationPermissionGranted){
                    // DO preprocessing things if GPS is on and there is no other problem
                }
                else{
                    getLocationPermission();
                }
            }
        }
    }

    private boolean isLocOn() {
        return ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (isLocOn()) {
            isLocationPermissionGranted = true;
            // DO preprocessing things if GPS is on and there is no other problem

        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        isLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isLocationPermissionGranted = true;
                }
            }
        }
    }

    /* this function should be called after markerLatLngList.remove() */
    private void removeDirectionByRemovingMarker(int markerIndex) {
        if (markerIndex == 0) { // remove polylines between first and second marker
            if (directionPolylines.size() == 0)
                return;
            for (Polyline line : directionPolylines.get(0).polylines)
                line.remove();
            directionPolylines.get(0).polylines.clear();
            directionPolylines.get(0).legs.clear();
            directionPolylines.get(0).paths_in_GeoPoint.clear();
            directionPolylines.remove(0);
        }
        else if (markerIndex == directionPolylines.size()) { // remove polylines between last and previous of last marker
            for (Polyline line : directionPolylines.get(markerIndex -1).polylines)
                line.remove();
            directionPolylines.get(markerIndex -1).polylines.clear();
            directionPolylines.get(markerIndex -1).legs.clear();
            directionPolylines.get(markerIndex -1).paths_in_GeoPoint.clear();
            directionPolylines.remove(markerIndex -1);
        }
        else { // remove polylines in the middle of the all the lines
            for (Polyline line : directionPolylines.get(markerIndex).polylines)
                line.remove();
            for (Polyline line : directionPolylines.get(markerIndex -1).polylines)
                line.remove();
            directionPolylines.get(markerIndex).polylines.clear();
            directionPolylines.get(markerIndex -1).polylines.clear();
            directionPolylines.get(markerIndex).legs.clear();
            directionPolylines.get(markerIndex -1).legs.clear();
            directionPolylines.get(markerIndex).paths_in_GeoPoint.clear();
            directionPolylines.get(markerIndex -1).paths_in_GeoPoint.clear();
            directionPolylines.remove(markerIndex);
            calculateDirections(markerLatLngList.get(markerIndex -1), markerLatLngList.get(markerIndex), markerIndex -1);
        }
    }

    private void addDirections(LatLng newMarkerLatLng) {
        // this function executes before new marker latLng is added
        if (markerLatLngList.size() == 0)
            return;

        calculateDirections(markerLatLngList.get(markerLatLngList.size() -1), newMarkerLatLng, -1);
    }

    private void calculateDirections(LatLng originLatLng, LatLng destinationLatLng, int mode) { // mode -1 : add, other number : insert at polylineData index
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                destinationLatLng.latitude,
                destinationLatLng.longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        originLatLng.latitude,
                        originLatLng.longitude
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "onResult: routes: " + result.routes[0].toString());
                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                //result.routes[0].legs[0].duration
                //result.routes[0].legs[0].distance

                if (mode == -1)
                    addDirectionPolylinesToMap(result);
                else
                    insertDirectionPolylinesToMap(result, mode);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
            }
        });
    }

    private void insertDirectionPolylinesToMap(final DirectionsResult result, int directionIndex) {
        // post it to the main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                ArrayList<Polyline> paths = new ArrayList<>();
                ArrayList<DirectionsLeg> pathLegs = new ArrayList<>();
                ArrayList<ArrayList<ParcelableGeoPoint>> paths_in_GeoPoint = new ArrayList<>();
                int selectedPathIndex = -1;

                for (int i = 0; i < result.routes.length; ++i) {
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(result.routes[i].overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();
                    ArrayList<ParcelableGeoPoint> newDecodePath_in_GeoPoint = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng));
                        newDecodePath_in_GeoPoint.add(new ParcelableGeoPoint(
                                latLng.lat,
                                latLng.lng));
                    }
                    Polyline newLine = gMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    newLine.setColor(ContextCompat.getColor(getActivity(), R.color.DarkGray));
                    newLine.setClickable(true);
                    paths.add(newLine);
                    pathLegs.add(result.routes[i].legs[0]);
                    paths_in_GeoPoint.add(newDecodePath_in_GeoPoint);
                }
                directionPolylines.get(directionIndex).polylines = paths;
                directionPolylines.get(directionIndex).legs = pathLegs;
                directionPolylines.get(directionIndex).selectedPathIndex = selectedPathIndex;
                directionPolylines.get(directionIndex).paths_in_GeoPoint = paths_in_GeoPoint;
            }
        });
    }

    private void addDirectionPolylinesToMap(final DirectionsResult result){
        // post it to the main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                ArrayList<Polyline> paths = new ArrayList<>();
                ArrayList<DirectionsLeg> pathLegs = new ArrayList<>();
                ArrayList<ArrayList<ParcelableGeoPoint>> paths_in_GeoPoint = new ArrayList<>();
                int selectedPathIndex = -1;

                for (int i = 0; i < result.routes.length; ++i) {
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(result.routes[i].overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();
                    ArrayList<ParcelableGeoPoint> newDecodePath_in_GeoPoint = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng));
                        newDecodePath_in_GeoPoint.add(new ParcelableGeoPoint(
                                latLng.lat,
                                latLng.lng));
                    }
                    Polyline newLine = gMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    newLine.setColor(ContextCompat.getColor(getActivity(), R.color.DarkGray));
                    newLine.setClickable(true);
                    paths.add(newLine);
                    pathLegs.add(result.routes[i].legs[0]);
                    paths_in_GeoPoint.add(newDecodePath_in_GeoPoint);
                }
                directionPolylines.add(new PolylineData(paths, pathLegs, selectedPathIndex, paths_in_GeoPoint));

                if (directionPolylines.size() == itineraryLocationList.size() -1) {

                    if (isEditing || isPlaceRemoved) {
                        selectPathWithGivenData();
                        if (isEditing)
                            isEditing = false;
                        if (isPlaceRemoved) {
                            if (!isAllPathSelected())
                                requestSelectingPath();
                            isPlaceRemoved = false;
                        }
                    }
                }
            }
        });
    }

    private void selectPathWithGivenData() {
        /* given saved information in database, highlight(select) path that user already selected one */
        for (int i = 0; i < directionPolylines.size(); ++i) {
            if (removedIndex != -1 && removedIndex == i+1)
                continue;

            int selectedIndex = itineraryLocationList.get(i+1).getIndex_of_selected_path_from_previous_location();
            // prevent selectedIndex outof boundary. This condition might happen because geopoint slightly changed through upload and download data
            // if geopoint changed, there is a chance to get different direction results, which cause the polylines.size() changed
            if (selectedIndex >= directionPolylines.get(i).polylines.size())
                selectedIndex = 0;
            directionPolylines.get(i).polylines.get(selectedIndex).setColor(ContextCompat.getColor(getActivity(), R.color.Blue));
            directionPolylines.get(i).polylines.get(selectedIndex).setZIndex(1);
            directionPolylines.get(i).selectedPathIndex = selectedIndex;
            MarkerList.getInstance().markers.get(i+1).index_of_selected_path_from_previous_location = selectedIndex;
            MarkerList.getInstance().markers.get(i+1).direction_points_from_previous_location
                    = directionPolylines.get(i).paths_in_GeoPoint.get(selectedIndex);
            itineraryLocationList.get(i+1).setIndex_of_selected_path_from_previous_location(selectedIndex);
            itineraryLocationList.get(i+1).setDirection_points_from_previous_location(directionPolylines.get(i).paths_in_GeoPoint.get(selectedIndex));
        }
    }

    private void requestSelectingPath() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Paths are recalculated due to removing location. Select the path. If you are done with selecting, click back button to go back to story title page")
                .setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
        return;
    }

    private void mapInit() {
        Log.d(TAG, "search init: initializing");

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                if (isLocSearchChangedByClickOnItem == true) {
                    isLocSearchChangedByClickOnItem = false;
                    return;
                }

                autocompleteLocationListView.setVisibility(View.VISIBLE);
                findAutocompletePredictions();
            }
        });

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                    autocompleteLocationListView.setVisibility(View.GONE);
                    geoLocate();
                }
                return false;
            }
        });

        hideSoftKeyboard();

        if (isEditing) {
            loadMarkersAndDirectionsFromDatabase();

            if (itineraryLocationList.size() > 0) {
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(itineraryLocationList.get(0).getGeo_point().getLatitude(),
                                itineraryLocationList.get(0).getGeo_point().getLongitude()),
                        DEFAULT_ZOOM));
            }
            else
                getDeviceLocation();
        }
    }

    private void loadMarkersAndDirectionsFromDatabase() {
        for (int i = 0; i < itineraryLocationList.size(); ++i) {
            LatLng latLng = new LatLng(
                    itineraryLocationList.get(i).getGeo_point().getLatitude(),
                    itineraryLocationList.get(i).getGeo_point().getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            try {
                Geocoder geocoder = new Geocoder(getContext());
                List<Address> mylist = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                markerOptions.title(mylist.get(0).getAddressLine(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
            markerOptions.position(latLng);
            markerOptions.draggable(true);
            Marker marker = gMap.addMarker(markerOptions);
            addDirections(latLng);
            markerLatLngList.add(latLng);
            MarkerList.getInstance().markers.add(new MarkerList.RegisteredMarker(marker, true));
            addMarkerMovedItem();
        }
        itineraryMapAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(itineraryMapAdapter);
    }

    private void findAutocompletePredictions() {
        searchLocationLoading.setVisibility(View.VISIBLE);

        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest.Builder requestBuilder =
                FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(token)
                        .setQuery(searchText.getText().toString());

        Task<FindAutocompletePredictionsResponse> task =
                placesClient.findAutocompletePredictions(requestBuilder.build());

        task.addOnSuccessListener((response) -> {
                    autocompleteLocationList.clear();
                    autocompleteLocationList.addAll(response.getAutocompletePredictions());
                    autocompleteLocationAdapter.notifyDataSetChanged();
                    autocompleteLocationListView.setAdapter(autocompleteLocationAdapter);
                });

        task.addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    }
                    else {
                        exception.printStackTrace();
                    }
                });

        task.addOnCompleteListener(response -> searchLocationLoading.setVisibility(View.GONE));
    }

    private AdapterView.OnItemClickListener onAutoCompletedLocationClickListener
        = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideSoftKeyboard();
            isLocSearchChangedByClickOnItem = true;

            final AutocompletePrediction item = autocompleteLocationList.get(position);
            final String placeId = item.getPlaceId();

            // Specify the fields to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

            // Construct a request object, passing the place ID and fields array.
            FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

            autocompleteLocationListView.setVisibility(View.GONE);
            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                moveCamera(place.getLatLng(), DEFAULT_ZOOM, null);
                searchText.setText(place.getName());
                Log.i(TAG, "Place found: " + place.getName());
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                }
            });
        }
    };

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = searchText.getText().toString();

        Geocoder geocoder = new Geocoder(getContext());
        List<Address> list = new ArrayList<>();

        try {
            list = geocoder.getFromLocationName(searchString, 1);
        }
        catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //ToastMessage(address.toString());

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moving the camera to " + latLng.latitude + ", " + latLng.longitude);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        hideSoftKeyboard();
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
    }

    private void ToastMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private final class WidgetTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    && ((ImageView)v).getDrawable() != null) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder builder = new View.DragShadowBuilder(v);
                v.startDragAndDrop(data, builder, v, 0);
                return true;
            }
            return false;
        }
    }

    private class WidgetDragDropListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d(TAG, "setOnDragListener: Drag Started");
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    Log.d(TAG, "setOnDragListener: Drag Location : (" + event.getX() + ", " + event.getY() + ")");
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:

                    LatLng latLng = gMap.getProjection().fromScreenLocation(new Point((int)event.getX(),(int)event.getY()));
                    MarkerOptions markerOptions = new MarkerOptions();
                    try {
                        Geocoder geocoder = new Geocoder(getContext());
                        List<Address> mylist = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        markerOptions.title(mylist.get(0).getAddressLine(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    markerOptions.position(latLng);
                    markerOptions.draggable(true);
                    Marker marker = gMap.addMarker(markerOptions);
                    addDirections(latLng);
                    markerLatLngList.add(latLng);
                    MarkerList.getInstance().markers.add(new MarkerList.RegisteredMarker(marker, false));
                    addMarkerMovedItem();
                    marker.showInfoWindow();
                    addItineraryInView(latLng);

                    placePickerWidget.setVisibility(View.GONE);
                    writeItineraryWidget.setVisibility(View.VISIBLE);

                    Log.d(TAG, "setOnDragListener: Drag Ended");
                    return true;
                default:
                    break;
            }
            return false;
        }
    }

    private void addMarkerMovedItem() {
        if (markerIndexMovedList.isEmpty())
            markerIndexMovedList.add(0);

        markerIndexMovedList.add(markerIndexMovedList.get(markerIndexMovedList.size() -1));
    }

    private void removeMarkerMovedItem(int markerIndex) {

        if (markerIndex == markerIndexMovedList.size() -1) {
            markerIndexMovedList.set(markerIndex, -1);
            return;
        }

        for (int i = markerIndex + 1; i < markerIndexMovedList.size(); ++i) {
            markerIndexMovedList.set(i, markerIndexMovedList.get(i) -1);
        }
    }

    private void addItineraryInView(LatLng latLng) {
        ItineraryLocation newSpot = new ItineraryLocation();
        newSpot.setGeo_point(new GeoPoint(latLng.latitude, latLng.longitude));
        String convertedLatitude = String.format("%.03f", latLng.latitude);
        String convertedLongitude = String.format("%.03f", latLng.longitude);
        newSpot.setName(convertedLatitude + ", " + convertedLongitude);
        itineraryLocationList.add(newSpot);
        itineraryMapAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(itineraryMapAdapter);
    }

    private void removeItineraryInView(int itineraryIndex) {
        itineraryLocationList.remove(itineraryIndex);
        itineraryMapAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(itineraryMapAdapter);
    }

    GoogleMap.OnMarkerDragListener markerDragListener = new GoogleMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) { }

        @Override
        public void onMarkerDrag(Marker marker) { }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            Geocoder geocoder = new Geocoder(getContext());

            try {
                LatLng pos = marker.getPosition();
                List<Address> list = geocoder.getFromLocation(pos.latitude, pos.longitude, 1);
                Address address = list.get(0);
                marker.setTitle(address.getLocality());
                marker.showInfoWindow();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void RemoveMarker(Marker marker) {
        String markerListIndexString = marker.getId().replace("m", "");
        int markerListIndex = Integer.parseInt(markerListIndexString);
        removeMarkerMovedItem(markerListIndex);
        markerListIndex += markerIndexMovedList.get(markerListIndex);
        removeItineraryInView(markerListIndex);
        markerLatLngList.remove(markerListIndex);
        MarkerList.getInstance().markers.remove(markerListIndex);
        removeDirectionByRemovingMarker(markerListIndex);
        marker.remove();
        placePickerWidget.setVisibility(View.VISIBLE);
        writeItineraryWidget.setVisibility(View.GONE);
    }

    private void setNewMarkerIndexMovedList() {
        for (int i = 0; i < markerIndexMovedList.size() -1; ++i)
            markerIndexMovedList.set(i, 1); // number does not have meaning, but positive number indicates dead markers
        // last item also dead, but the reason for not setting as 1 is new marker will copy the value of previous index
        markerIndexMovedList.set(markerIndexMovedList.size() -1, -markerIndexMovedList.size() +1); // +1 : see the comment on the declaration section
    }

    private void setPolylineHighlighted(Polyline polyline) {
        int directionIndex = -1;
        int polyLineIndex = -1;
        boolean found = false;

        for(int i = 0; i < directionPolylines.size(); ++i) {
            for (int j = 0; j < directionPolylines.get(i).polylines.size(); ++j) {
                if (polyline.getId().equals(directionPolylines.get(i).polylines.get(j).getId())) {
                    found = true;
                    directionIndex = i;
                    polyLineIndex = j;
                    break;
                }
            }
            if (found)
                break;
        }

        for (int i = 0; i < directionPolylines.get(directionIndex).polylines.size(); ++i) {
            if (i == polyLineIndex) {
                directionPolylines.get(directionIndex).polylines.get(i).setColor(ContextCompat.getColor(getActivity(), R.color.Blue));
                directionPolylines.get(directionIndex).polylines.get(i).setZIndex(1);
                directionPolylines.get(directionIndex).selectedPathIndex = i;
                MarkerList.getInstance().markers.get(directionIndex+1).index_of_selected_path_from_previous_location = i;
                MarkerList.getInstance().markers.get(directionIndex+1).direction_points_from_previous_location
                        = directionPolylines.get(directionIndex).paths_in_GeoPoint.get(i);
                itineraryLocationList.get(directionIndex+1).setIndex_of_selected_path_from_previous_location(i);
                itineraryLocationList.get(directionIndex+1).setDirection_points_from_previous_location(directionPolylines.get(directionIndex).paths_in_GeoPoint.get(i));
            }
            else {
                directionPolylines.get(directionIndex).polylines.get(i).setColor(ContextCompat.getColor(getActivity(), R.color.DarkGray));
                directionPolylines.get(directionIndex).polylines.get(i).setZIndex(0);
            }
        }
    }

    GoogleMap.OnPolylineClickListener directionClickListener = new GoogleMap.OnPolylineClickListener() {
        @Override
        public void onPolylineClick(Polyline polyline) {
            setPolylineHighlighted(polyline);
        }
    };

    GoogleMap.OnInfoWindowClickListener infoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(final Marker marker) {

            final String options[] = new String[2];
            options[0] = "Remove marker";
            options[1] = "Cancel";

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Choose action")
                    .setCancelable(true)
                    .setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0: RemoveMarker(marker);  break;
                                case 1: dialog.cancel();       break;
                            }
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    };
}