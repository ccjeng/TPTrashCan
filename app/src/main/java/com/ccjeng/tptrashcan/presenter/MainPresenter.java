package com.ccjeng.tptrashcan.presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import com.ccjeng.tptrashcan.R;
import com.ccjeng.tptrashcan.model.TrashCan;
import com.ccjeng.tptrashcan.presenter.base.BasePresenter;
import com.ccjeng.tptrashcan.utils.MapUtils;
import com.ccjeng.tptrashcan.utils.Utils;
import com.ccjeng.tptrashcan.view.adapter.CustomInfoWindowAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static c.Device.TAG;

/**
 * Created by andycheng on 2016/10/3.
 */

public class MainPresenter extends BasePresenter<MainView>
        implements OnMapReadyCallback, LocationConnectedListener {

    private MainView view;
    private Context context;
    private LocationService locationService;
    private Location currentLocation;
    private Marker markerTrashCan;


    public MainPresenter(MainView view, Context context) {
        this.view = view;
        this.context = context;
    }

    private void getTrashCan(final GoogleMap map) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://tptrashcan.firebaseio.com/results");

        ref.keepSynced(true);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Bitmap bitmap = MapUtils.getBitmap(context, R.drawable.ic_trash);

                for (DataSnapshot keySnapshot : snapshot.getChildren()) {
                    TrashCan can = keySnapshot.getValue(TrashCan.class);

                    //Marker
                    MarkerOptions markerOption = new MarkerOptions();
                    markerOption.position(new LatLng(Double.valueOf(can.getLatitude()), Double.valueOf(can.getLongitude())));
                    markerOption.title(can.getAddress());
                    markerOption.snippet(can.getRegion());
                    markerOption.icon(BitmapDescriptorFactory.fromBitmap(bitmap));

                    CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter((Activity) context);
                    map.setInfoWindowAdapter(adapter);

                    markerTrashCan = map.addMarker(markerOption);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }

        });

    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);

        getTrashCan(map);
    }

    @Override
    public void onLocationServiceConnected(Location location) {
        currentLocation = location;

        //auto set selected selection on spinner
        if (currentLocation != null) {
            MapFragment mapFragment = ((MapFragment) ((Activity) context).getFragmentManager().findFragmentById(R.id.map_fragment));
            mapFragment.getMapAsync(this);
        } else {
            //location error
            view.showError(R.string.location_error);
        }
    }

    @Override
    public void onCreate() {
        view.initView();
        locationService = new LocationService(context);
        locationService.setLocationConnectedListener(this);

        if (Utils.isNetworkConnected(context)) {
            locationService.connect();
        } else {
            view.showError(R.string.network_error);
        }
    }

    @Override
    public void onStart() {
        locationService.connect();
    }

    @Override
    public void onStop() {
        locationService.disconnect();
    }

    @Override
    public void onResume() {
        locationService.connect();
    }

    @Override
    public void onPause() {
        locationService.pause();
    }

    @Override
    public void onDestroy() {

    }


}
