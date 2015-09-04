package com.ccjeng.tptrashcan;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ccjeng.tptrashcan.app.TPTrashCan;
import com.ccjeng.tptrashcan.utils.Analytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import butterknife.Bind;
import butterknife.ButterKnife;


public class InfoActivity extends ActionBarActivity {

    private static final String TAG = TPTrashCan.class.getSimpleName();

    @Bind(R.id.address)
    TextView addressView;

    @Bind(R.id.memo)
    TextView memoView;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private String strFrom = "";
    private String strFromLat = "";
    private String strFromLng = "";

    private String strTo = "";
    private String strToLat = "";
    private String strToLng = "";

    private String address;
    private String memo;
    private String objectId;
    private Location myLoc;
    //private ParseQueryAdapter<TrashCanItem> trashcanQueryAdapter;

    // Map fragment
    private GoogleMap map;
    private Analytics ga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_arrow_back)
                .color(Color.WHITE)
                .actionBarSize());

        // Menu item click 的監聽事件一樣要設定在 setSupportActionBar 才有作用
        toolbar.setOnMenuItemClickListener(onMenuItemClick);


        Bundle bundle = this.getIntent().getExtras();

        strFromLat = bundle.getString("fromLat");
        strFromLng = bundle.getString("fromLng");
        strFrom = strFromLat + "," + strFromLng;

        strToLat = bundle.getString("toLat");
        strToLng = bundle.getString("toLng");
        strTo = strToLat + "," + strToLng;

        address = bundle.getString("address");
        memo = bundle.getString("memo");
        //objectId = bundle.getString("objectId");

        addressView.setText("位置：" + address);
        memoView.setText(memo);

        //getSupportActionBar().setTitle(address);

        // Set up the map fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);

        CameraUpdate center =
                CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(strToLat)
                        , Double.valueOf(strToLng)), 17);
        map.animateCamera(center);


        //Current
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(Double.valueOf(strFromLat)
                , Double.valueOf(strFromLng)));
        markerOpt.title("現在位置");
        markerOpt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        map.addMarker(markerOpt).showInfoWindow();

        //Marker
        MarkerOptions markerOpt2 = new MarkerOptions();
        markerOpt2.position(new LatLng(Double.valueOf(strToLat)
                , Double.valueOf(strToLng)));
        markerOpt2.title(address);
        markerOpt2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        map.addMarker(markerOpt2).showInfoWindow();

        //Draw Line
        PolylineOptions polylineOpt = new PolylineOptions();
        polylineOpt.add(new LatLng(Double.valueOf(strFromLat)
                , Double.valueOf(strFromLng)));
        polylineOpt.add(new LatLng(Double.valueOf(strToLat)
                , Double.valueOf(strToLng)));

        polylineOpt.color(Color.BLUE);

        Polyline polyline = map.addPolyline(polylineOpt);
        polyline.setWidth(10);
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            switch (menuItem.getItemId()) {
                case android.R.id.home:
                    finish();
                    break;
                case R.id.menu_navi:
                    goBrowser();
                    break;

            }

            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.info, menu);

        MenuItem menuItem1 = menu.findItem(R.id.menu_navi);
        menuItem1.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_navigation).actionBarSize().color(Color.WHITE));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);

    }

    private void goBrowser() {

        ga.trackEvent(this, "Click", "Button", "Google Map", 0);
        //Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        String from = "saddr=" + strFrom;
        String to = "daddr=" + strTo;
        String para = "&hl=zh&dirflg=w";
        String url = "http://maps.google.com.tw/maps?" + from + "&" + to + para;
        Intent ie = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(ie);

    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    /*
    * Called when the Activity is restarted, even before it becomes visible.
    */
    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /*
    * Called when the Activity is resumed. Updates the view.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
