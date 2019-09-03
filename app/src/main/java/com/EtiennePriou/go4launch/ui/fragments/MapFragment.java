package com.EtiennePriou.go4launch.ui.fragments;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.EtiennePriou.go4launch.R;
import com.EtiennePriou.go4launch.di.DI;
import com.EtiennePriou.go4launch.events.ReceiveListPlace;
import com.EtiennePriou.go4launch.models.PlaceModel;
import com.EtiennePriou.go4launch.services.places.PlacesApi;

import com.EtiennePriou.go4launch.ui.DetailPlaceActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Objects;

public class MapFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleMap.OnInfoWindowClickListener {

    private static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    private GoogleMap mMap;
    private View mView;
    private Context mContext;
    private ProgressDialog myProgress;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesApi mPlacesApi;
    private static final String PLACEREFERENCE = "placeReference";


    public MapFragment() { }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mPlacesApi = DI.getServiceApiPlaces();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getContext());

        // Create Progress Bar.
        myProgress = new ProgressDialog(mContext);
        myProgress.setTitle("Fetching nearby restaurants ...");
        myProgress.setMessage("Please wait...");
        myProgress.setCancelable(true);
        // Display Progress Bar.
        myProgress.show();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_map, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        MapView mapView = mView.findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        requestPermissionLocation();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void requestPermissionLocation() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // The Permissions to ask user.
            String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION};
            // Show a dialog asking the user to allow the above permissions.
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), permissions, REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);
            return;
        }
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                setNearbyPlaces(location);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ID_ACCESS_COURSE_FINE_LOCATION) {
            if (grantResults.length > 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(getContext(), "Permission granted!", Toast.LENGTH_LONG).show();
            }
            // Cancelled or denied.
            else {
                Toast.makeText(getContext(), "Permission denied!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setNearbyPlaces(Location location) {

        if (mPlacesApi.getNearbyPlaceModelList() == null){
            mPlacesApi.setListPlaces(location.getLatitude(),location.getLongitude(), mMap);
        }else {
            showNearbyPlaces();
        }
    }

    private void showNearbyPlaces() {

        for (PlaceModel placeModel : mPlacesApi.getNearbyPlaceModelList()) {

            MarkerOptions markerOptions = new MarkerOptions();

            double lat = Double.parseDouble( placeModel.getLat() );
            double lng = Double.parseDouble( placeModel.getLongit() );
            LatLng latLng = new LatLng( lat, lng);

            markerOptions.position(latLng);
            markerOptions.title(placeModel.getName());
            markerOptions.snippet(placeModel.getAdresse());
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker());

            Marker marker = mMap.addMarker(markerOptions);
            marker.setTag(placeModel.getReference());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
            mMap.setOnInfoWindowClickListener(this);
        }
        myProgress.dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onReceiveList(ReceiveListPlace event){
        showNearbyPlaces();
    }

    @Override
    public void onLocationChanged(Location location) {
        setNearbyPlaces(location);
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent detailIntent = new Intent(this.getContext(), DetailPlaceActivity.class);
        detailIntent.putExtra(PLACEREFERENCE,marker.getTag().toString());
        this.getContext().startActivity(detailIntent);
    }
}
