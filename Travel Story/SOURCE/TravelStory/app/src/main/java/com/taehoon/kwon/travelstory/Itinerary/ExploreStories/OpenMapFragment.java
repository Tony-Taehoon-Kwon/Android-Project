package com.taehoon.kwon.travelstory.Itinerary.ExploreStories;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
import com.taehoon.kwon.travelstory.Itinerary.Map.ItineraryMapAdapter;
import com.taehoon.kwon.travelstory.OnBackPressed;
import com.taehoon.kwon.travelstory.Itinerary.model.ItineraryLocation;
import com.taehoon.kwon.travelstory.Itinerary.model.ParcelableGeoPoint;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;
import java.util.List;

public class OpenMapFragment extends ExploreStoryContainerFragment implements OnBackPressed, OnMapReadyCallback {

    public static final String FRAG_TAG = "explore_stories_open_map";
    private static final String TAG = "OpenMapFragment";
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

    private RecyclerView recyclerView;
    private ItineraryMapAdapter itineraryMapAdapter;
    private ArrayList<ItineraryLocation> itineraryLocationList = new ArrayList<>();

    private GoogleMap gMap;

    private ImageView mapTypeWidget;
    private ImageView itineraryWidget;
    private PopupMenu mapTypePopupMenu = null;
    private PopupMenu itineraryPopupMenu = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        MainActivity.current_fragment_tag = FRAG_TAG;

        root = inflater.inflate(R.layout.fragment_open_map, container, false);

        if (getArguments() != null) {
            itineraryLocationList = getArguments().getParcelableArrayList("open_itinerary_list");
        }

        if(checkMapServices()) {
            if(!isLocationPermissionGranted)
                getLocationPermission();
        }
        isLocationPermissionGranted = isLocOn();

        fragmentManager = getActivity().getSupportFragmentManager();

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView = (MapView)root.findViewById(R.id.googleMap);
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);

        mapTypeWidget = root.findViewById(R.id.ic_map);
        itineraryWidget = root.findViewById(R.id.ic_itinerary);
        recyclerView = root.findViewById(R.id.itineraryRecyclerView);
        itineraryMapAdapter = new ItineraryMapAdapter(getContext(), itineraryLocationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(itineraryMapAdapter);

        mapTypeWidget.setOnClickListener(mapTypeWidgetClickListener);
        itineraryWidget.setOnClickListener(itineraryWidgetClickListener);

        return root;
    }

    @Override
    public void onBackPressed() {

        Fragment fragment = fragmentManager.findFragmentByTag(ExploreStoryDetailFragment.FRAG_TAG);

        if (fragment == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, new ExploreStoryDetailFragment(), ExploreStoryDetailFragment.FRAG_TAG)
                    .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                    .commit();
        }
        else {
            fragmentManager.beginTransaction()
                    .show(fragment)
                    .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                    .commit();
        }
        MainActivity.current_fragment_tag = ExploreStoryDetailFragment.FRAG_TAG;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        // Get the first itinerary location and set the position of the map.
        getFirstItineraryLocation();

        // Add all markers and path directions
        addMarkersToMap();
        addDirectionPolylinesToMap();
    }

    private void getFirstItineraryLocation() {

        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(itineraryLocationList.get(0).getGeo_point().getLatitude(),
                            itineraryLocationList.get(0).getGeo_point().getLongitude()),
                            DEFAULT_ZOOM));
    }

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

    private void ToastMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void addMarkersToMap() {
        for (int i = 0; i < itineraryLocationList.size(); ++i) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title(itineraryLocationList.get(i).getName());
            markerOptions.position(new LatLng(
                    itineraryLocationList.get(i).getGeo_point().getLatitude(),
                    itineraryLocationList.get(i).getGeo_point().getLongitude()));
            Marker marker = gMap.addMarker(markerOptions);
        }
    }

    private void addDirectionPolylinesToMap(){

        // marker0 - direction1 - marker1 - direction2 - marker2 ...
        // there is no polyline data in marker with index 0
        for (int i = 1; i < itineraryLocationList.size(); ++i) {

            List<LatLng> downloadedPath = new ArrayList<>();

            List<ParcelableGeoPoint> path = itineraryLocationList.get(i).getDirection_points_from_previous_location();
            if (path.size() != 0) {
                for (int j = 0; j < path.size(); ++j) {
                    downloadedPath.add(new LatLng(
                            path.get(j).getLatitude(),
                            path.get(j).getLongitude()));
                }
                Polyline newLine = gMap.addPolyline(new PolylineOptions().addAll(downloadedPath));
                newLine.setColor(ContextCompat.getColor(getActivity(), R.color.Blue));
            }
        }
    }
}
