package com.example.concordia_campus_guide.Fragments.LocationFragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.concordia_campus_guide.Activities.MainActivity;
import com.example.concordia_campus_guide.Adapters.FloorPickerAdapter;
import com.example.concordia_campus_guide.ClassConstants;
import com.example.concordia_campus_guide.Helper.ViewModelFactory;
import com.example.concordia_campus_guide.Interfaces.OnFloorPickerOnClickListener;
import com.example.concordia_campus_guide.Models.Building;
import com.example.concordia_campus_guide.Models.WalkingPoint;
import com.example.concordia_campus_guide.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.Observer;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class LocationFragment extends Fragment implements OnFloorPickerOnClickListener {

    MapView mMapView;

    private Location currentLocation;

    private ImageButton myLocationBtn;

    private GoogleMap mMap;
    private LocationFragmentViewModel mViewModel;
    private GeoJsonLayer mLayer;
    private Button loyolaBtn;
    private Button sgwBtn;
    private GridView mFloorPickerGv;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final String TAG = "LocationFragment";
    private boolean myLocationPermissionsGranted = false;
    private HashMap<String, GroundOverlay> buildingsGroundOverlays;
    private FloorPickerAdapter currentFloorPickerAdapter;

    /**
     * @return it will return a new object of this fragment
     */
    public static LocationFragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.location_fragment_fragment, container, false);
        initComponent(rootView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        getLocationPermission();
        initMap();
        setupClickListeners();
        updateLocationEvery5Seconds();

        return rootView;
    }

    /**
     * @param rootView is the object that contains the parts displayed on the fragment
     */
    private void initComponent(View rootView) {
        mMapView = rootView.findViewById(R.id.mapView);
        sgwBtn = rootView.findViewById(R.id.SGWBtn);
        loyolaBtn = rootView.findViewById(R.id.loyolaBtn);
        myLocationBtn = rootView.findViewById(R.id.myLocation);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        mFloorPickerGv = rootView.findViewById(R.id.FloorPickerGv);
        mFloorPickerGv.setVisibility(View.GONE);
        buildingsGroundOverlays = new HashMap<>();
    }

    private void setupFloorPickerAdapter(Building building) {
        mFloorPickerGv.setVisibility(View.VISIBLE);

        currentFloorPickerAdapter = new FloorPickerAdapter(getContext(), building.getAvailableFloors(), building.getBuildingCode(), this);
        mFloorPickerGv.setAdapter(currentFloorPickerAdapter);
        mViewModel.setFloorMarkers(building.getBuildingCode(), building.getAvailableFloors().get(building.getAvailableFloors().size() - 1), getContext(), mMap);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity(), new ViewModelFactory(this.getActivity().getApplication())).get(LocationFragmentViewModel.class);
        setupPOIListListener();
    }


    /**
     * The purpose of this method is to init the map and fill it up with the required
     * information to display them to the user
     */
    private void initMap() {
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setMapStyle(googleMap);
                mMap = googleMap;
                setupPolygons(mMap);
                initFloorPlans();
                uiSettingsForMap(mMap);
                setFirstLocationToDisplay();
            }
        });
    }


    private void setFirstLocationToDisplay() {
        setFirstLocationToDisplayOnSuccess();
        setFirstLocationToDisplayOnFailure();
    }

    private void setFirstLocationToDisplayOnSuccess() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mMap.setMyLocationEnabled(true);
                            zoomInLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                        } else {
                            zoomInLocation(mViewModel.getInitialZoomLocation());
                        }
                    }
                });
    }

    private void setFirstLocationToDisplayOnFailure() {
        fusedLocationProviderClient.getLastLocation().addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                zoomInLocation(mViewModel.getInitialZoomLocation());
            }
        });
    }

    private void setCurrentLocationBtn() {
        myLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFloorPickerGv.setVisibility(View.GONE);
                popUpRequestPermission();
            }
        });
    }

    /**
     * The purpose of this method is to figure the style of the map to display
     */
    private void initFloorPlans() {
        Map<String, Building> temp = mViewModel.getBuildings();
        for (Map.Entry<String, Building> entry : temp.entrySet()) {
            String key = entry.getKey();
            Building value = entry.getValue();

            if (value.getGroundOverlayOption() != null)
                buildingsGroundOverlays.put(key, mMap.addGroundOverlay(temp.get(key).getGroundOverlayOption()));
        }
    }

    private void setMapStyle(GoogleMap googleMap) {
        try {
            googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), mViewModel.getMapStyle()));
        } catch (Resources.NotFoundException e) {
            Log.e("MAPACTIVITY", "Can't find style. Error: ", e);
        }
    }


    /**
     * The purpose of this method is to setup the click listener for both
     * SGW and LOYALA Campuses buttons.
     */
    private void setupClickListeners() {
        setupLoyolaBtnClickListener();
        setupSGWBtnClickListener();
        setCurrentLocationBtn();
    }

    private void popUpRequestPermission() {
        if (!myLocationPermissionsGranted) {
            getLocationPermission();
            if (myLocationPermissionsGranted) {
                mMap.setMyLocationEnabled(true);
                initMap();
            }
        }

        if (myLocationPermissionsGranted) {
            mMap.setMyLocationEnabled(true);
            initMap();
        }
    }

    /**
     * The purpose of this method is to handle the "on click" of Loyala button
     */
    private void setupLoyolaBtnClickListener() {
        loyolaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomInLocation(mViewModel.getLoyolaZoomLocation());
            }
        });
    }

    /**
     * The purpose of this method is to handle the "on click" of SGW button
     */
    private void setupSGWBtnClickListener() {
        sgwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomInLocation(mViewModel.getSGWZoomLocation());
            }
        });
    }

    /**
     * The purpose of this method is display the polygon on the map and
     * call the right method for onClick polygon or on click the marker.
     *
     * @param map is the map to be used in our application
     */
    private void setupPolygons(GoogleMap map) {
        mLayer = mViewModel.loadPolygons(map, getContext());
        mLayer.addLayerToMap();

        setupPolygonClickListener();
        setupBuildingMarkerClickListener(map);
        setupZoomListener(map);
        classRoomCoordinateTool(map);
    }

    private void setupZoomListener(final GoogleMap map) {
        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (map.getCameraPosition().zoom > 20) {
                    mLayer.removeLayerFromMap();
                    setupClassMarkerClickListener(map);
                } else {
                    mLayer.addLayerToMap();
                    setupBuildingMarkerClickListener(map);
                }
            }
        });
    }

    /**
     * The purpose of this method is handle the onclick polygon
     * and to open the info card according to the clicked building.
     */
    public void setupPolygonClickListener() {
        mLayer.setOnFeatureClickListener(new GeoJsonLayer.GeoJsonOnFeatureClickListener() {
            @Override
            public void onFeatureClick(GeoJsonFeature geoJsonFeature) {
                if (geoJsonFeature != null) {
                    Building building = mViewModel.getBuildingFromGeoJsonFeature(geoJsonFeature);
                    onBuildingClick(building);
                    String buildingCode = geoJsonFeature.getProperty("code");
                    ((MainActivity) getActivity()).showInfoCard(buildingCode);
                }
            }
        });
    }

    private void onBuildingClick(Building building) {
        if (building.getAvailableFloors() != null) {
            setupFloorPickerAdapter(building);
        } else {
            mFloorPickerGv.setVisibility(View.GONE);
        }
    }

    /**
     * The purpose of this method is handle the onclick marker
     * and to open the info card according to the clicked building.
     */
    public boolean setupBuildingMarkerClickListener(GoogleMap map) {
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Building building = mViewModel.getBuildingFromeCode(marker.getTag().toString());
                //TODO: Make function that pops up the info card for the building (via the building-code)
                String buildingCode = (marker.getTag()).toString();
                ((MainActivity) getActivity()).showInfoCard(buildingCode);
                onBuildingClick(building);
                return false;
            }
        });
        return true;
    }

    /**
     * The purpose of this method is to display the tools used with google
     * maps such as Current Location
     *
     * @param map is the map used in the application
     */
    public boolean setupClassMarkerClickListener(GoogleMap map) {
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i(TAG, marker.getTag().toString());
                return false;
            }
        });
        return true;
    }

    private void setupPOIListListener() {
        mViewModel.getListOfPOI().observe(getViewLifecycleOwner(), new Observer<List<WalkingPoint>>() {
            @Override
            public void onChanged(List<WalkingPoint> walkingPoints) {
                for (WalkingPoint point : walkingPoints) {
                    LatLng latLng = new LatLng(point.getCoordinate().getLongitude(), point.getCoordinate().getLatitude());
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            //TODO: Change the icon to be the same as the POI icon
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .visible(false);
                    mMap.addMarker(markerOptions);
                }
            }
        });
    }


    /**
     * set up related to UI for the map
     *
     * @param mMap
     */
    private void uiSettingsForMap(GoogleMap mMap) {
        if (myLocationPermissionsGranted) {
            mMap.setMyLocationEnabled(true);
        }
        mMap.setIndoorEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
    }

    private void zoomInLocation(LatLng center) {
        float zoomLevel = 18.5f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomLevel));
    }

    /**
     * The purpose of this application is to ask the user for their permission
     * of using their current location.
     */
    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        
            if (requestPermission()) {
                initMap();
            }
            else {
                ActivityCompat.requestPermissions(getActivity(),
                        permissions,
                        ClassConstants.LOCATION_PERMISSION_REQUEST_CODE);
            }
            initMap();
    }

    private void classRoomCoordinateTool(GoogleMap map) {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i(TAG, "\"coordinates\" : [" + latLng.longitude + ", " + latLng.latitude + "]");
            }
        });
    }


    /**
     * @return the method returns true if the user accepts to give the application permission
     * for using their current location.
     */
    private boolean requestPermission() {
        return (checkSelfPermission(getContext(), ClassConstants.FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onFloorPickerOnClick(int position, View view) {
        mViewModel.setFloorPlan(buildingsGroundOverlays.get(currentFloorPickerAdapter.getBuildingCode()), currentFloorPickerAdapter.getBuildingCode(), currentFloorPickerAdapter.getFloorsAvailable().get(position), getContext(), mMap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ClassConstants.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                myLocationPermissionsGranted = (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED);
                setFirstLocationToDisplayOnSuccess();
            } else {
                setFirstLocationToDisplayOnSuccess();
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void updateLocationEvery5Seconds() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new updateLocationListener());
                handler.postDelayed(this, 5000);
            }
        }, 5000);
    }

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    private class updateLocationListener implements OnSuccessListener {
        @Override
        public void onSuccess(Object location) {
            if (location != null && location instanceof Location) {
                if (location instanceof Location) {
                    LocationFragment.this.setCurrentLocation((android.location.Location) location);
                }
            }
        }
    }
}