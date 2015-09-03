package com.ccjeng.tptrashcan;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ccjeng.tptrashcan.app.TPTrashCan;
import com.ccjeng.tptrashcan.ui.TrashCanItem;
import com.ccjeng.tptrashcan.utils.Analytics;
import com.ccjeng.tptrashcan.utils.Utils;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class InfoActivity extends ActionBarActivity /*FragmentActivity*/ {

    private static final String TAG = TPTrashCan.class.getSimpleName();

    @Bind(R.id.address)
    TextView addressView;

    @Bind(R.id.memo)
    TextView memoView;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.trashcanList)
    ListView trashcanList;

    private String strFrom = "";
    private String strFromLat = "";
    private String strFromLng = "";

    private String strTo = "";
    private String strToLat = "";
    private String strToLng = "";

    private String address;
    private String memo;
    private Location myLoc;
    private ParseQueryAdapter<TrashCanItem> trashcanQueryAdapter;

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

        //toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_arrow_back)
                .color(Color.WHITE)
                .actionBarSize());

        // Menu item click 的監聽事件一樣要設定在 setSupportActionBar 才有作用
        toolbar.setOnMenuItemClickListener(onMenuItemClick);


        Bundle bundle = this.getIntent().getExtras();

        /*
        ArrayList list = bundle.getParcelableArrayList("list");

        ArrayItem item = (ArrayItem) list.get(0);

        address = item.getAddress();
        carno = item.getCarNo();
        carnumber = item.getCarNumber();
        time = item.getCarTime();
        memo = item.getMemo();
        garbage = item.checkTodayAvailableGarbage();
        food = item.checkTodayAvailableFood();
        recycling = item.checkTodayAvailableRecycling();


        strTo = String.valueOf(item.getLocation().getLatitude()) + "," +
                String.valueOf(item.getLocation().getLongitude());

*/

        strFromLat = bundle.getString("fromLat");
        strFromLng = bundle.getString("fromLng");
        strFrom = strFromLat + "," + strFromLng;

        strToLat = bundle.getString("toLat");
        strToLng = bundle.getString("toLng");
        strTo = strToLat + "," + strToLng;

        address = bundle.getString("address");
        memo = bundle.getString("memo");

        //addressView.setText("位置：" + address);
        memoView.setText(memo);


        //reset title
        /*
        Geocoder geocoder = new Geocoder(this, new Locale("zh", "TW"));
        String current_location = "現在位置:";
        try {
            List<Address> addressList = geocoder.getFromLocation(Double.valueOf(strFromLat)
                    , Double.valueOf(strFromLng), 1);

            if (addressList == null) {
                current_location = getString(R.string.app_name);
            } else {
                current_location = current_location + addressList.get(0).getAdminArea() + "" + addressList.get(0).getLocality();
            }

        } catch (IOException e) {

            Log.d(TAG, e.toString());

            current_location = getString(R.string.app_name);
        }
*/
        getSupportActionBar().setTitle(address);


        parseQuery();

        // Set up the map fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);

        CameraUpdate center =
                CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(strToLat)
                        , Double.valueOf(strToLng)), 15);
        map.animateCamera(center);


        //Current
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(Double.valueOf(strFromLat)
                , Double.valueOf(strFromLng)));
        markerOpt.title("現在位置");
        markerOpt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
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

    private void parseQuery() {

        myLoc = new Location("");;
        myLoc.setLatitude(Double.valueOf(strToLat));
        myLoc.setLongitude(Double.valueOf(strToLng));


            if (TPTrashCan.APPDEBUG)
                Log.d(TAG, "location = " + myLoc.toString());

            // Set up a customized query
            ParseQueryAdapter.QueryFactory<TrashCanItem> factory =
                    new ParseQueryAdapter.QueryFactory<TrashCanItem>() {
                        public ParseQuery<TrashCanItem> create() {



                            ParseQuery<TrashCanItem> query = TrashCanItem.getQuery();

                            int distance = 100;
                            query.whereWithinKilometers("location"
                                    , Utils.geoPointFromLocation(myLoc)
                                    , distance
                            );

                            query.setLimit(5);

                            return query;
                        }
                    };

            // Set up the query adapter
            trashcanQueryAdapter = new ParseQueryAdapter<TrashCanItem>(this, factory) {
                @Override
                public View getItemView(TrashCanItem trash, View view, ViewGroup parent) {
                    if (view == null) {
                        view = View.inflate(getContext(), R.layout.trashcan_item, null);
                    }

                    TextView regionView = (TextView) view.findViewById(R.id.region_view);
                    TextView addressView = (TextView) view.findViewById(R.id.address_view);
                    TextView distanceView = (TextView) view.findViewById(R.id.distance_view);


                    regionView.setText(trash.getRegion());
                    distanceView.setText(trash.getDistance(Utils.geoPointFromLocation(myLoc)).toString());
                    addressView.setText(trash.getFullAddress());

                    return view;
                }
            };

            trashcanQueryAdapter.setPaginationEnabled(false);
            trashcanQueryAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<TrashCanItem>() {

                @Override
                public void onLoading() {
                    //pbLoading.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoaded(List<TrashCanItem> objects, Exception e) {

                    //pbLoading.setVisibility(View.GONE);

                    //No data
                    if (trashcanList.getCount() == 0) {
                        //String msg = String.valueOf(distance) + "公里"
                        //         + getString(R.string.data_not_found);

                    }
                }
            });

            trashcanList.setAdapter(trashcanQueryAdapter);

            // Set up the handler for an item's selection
            trashcanList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final TrashCanItem item = trashcanQueryAdapter.getItem(position);
                    //Open Detail Page
                    //goIntent(item);
                }
            });


    }


}
