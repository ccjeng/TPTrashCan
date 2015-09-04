package com.ccjeng.tptrashcan;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ccjeng.tptrashcan.app.TPTrashCan;
import com.ccjeng.tptrashcan.ui.DrawerItem;
import com.ccjeng.tptrashcan.ui.DrawerItemAdapter;
import com.ccjeng.tptrashcan.ui.TrashCanItem;
import com.ccjeng.tptrashcan.utils.Analytics;
import com.ccjeng.tptrashcan.utils.Utils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends ActionBarActivity
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = TPTrashCan.class.getSimpleName();
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar actionbar;

    @Bind(R.id.lsv_drawer_menu)
    ListView mLsvDrawerMenu;

    @Bind(R.id.llv_left_drawer)
    LinearLayout mLlvDrawerContent;

    @Bind(R.id.drw_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.trashcanList)
    ListView trashcanList;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    @Bind(R.id.pull_to_refresh)
    SwipeRefreshLayout mSwipeLayout;
    public static final int REFRESH_DELAY = 1000;

    // 記錄被選擇的選單指標用
    private int mCurrentMenuItemPosition = -1;

    private AdView adView;
    private int distance;
    private int rowcount;

    /*
  * Define a request code to send to Google Play services This code is returned in
  * Activity.onActivityResult
  */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    private static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;

    // Adapter for the Parse query
    private ParseQueryAdapter<TrashCanItem> trashcanQueryAdapter;

    // Fields for helping process map and location changes
    private Location lastLocation;
    private Location currentLocation;
    private Location myLoc;
    private Analytics ga;

    // A request to connect to Location Services
    private LocationRequest locationRequest;

    // Stores the current instantiation of the location client in this object
    private GoogleApiClient locationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ga = new Analytics();
        ga.trackerPage(this);

        initActionBar();
        initDrawer();
        initDrawerList();

        getPref();
        adView();

        if (isNetworkConnected()) {
            // 建立Google API用戶端物件
            configGoogleApiClient();

            // 建立Location請求物件
            configLocationRequest();

            if (!locationClient.isConnected()) {
                locationClient.connect();
            }
        } else {

            Crouton.makeText(MainActivity.this, R.string.network_error, Style.ALERT,
                    (ViewGroup)findViewById(R.id.croutonview)).show();
        }


        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_menu)
                .color(Color.WHITE)
                .actionBarSize());

        actionbar = getSupportActionBar();
    }

    private void initDrawer() {
        // 設定 Drawer 的影子
        //mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,    // 讓 Drawer Toggle 知道母體介面是誰
                R.mipmap.ic_drawer, // Drawer 的 Icon
                R.string.app_name, // Drawer 被打開時的描述
                R.string.app_name // Drawer 被關閉時的描述
        ) {
            //被打開後要做的事情
            @Override
            public void onDrawerOpened(View drawerView) {
                // 將 Title 設定為自定義的文字
                actionbar.setTitle(R.string.app_name);
            }

            //被關上後要做的事情
            @Override
            public void onDrawerClosed(View drawerView) {
                // 將 Title 設定回 APP 的名稱
                actionbar.setTitle(R.string.app_name);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void initDrawerList() {

        String[] drawer_menu = this.getResources().getStringArray(R.array.drawer_menu);

        DrawerItem[] drawerItem = new DrawerItem[3];

        drawerItem[0] = new DrawerItem(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_settings)
                .color(Color.GRAY)
                .sizeDp(24),
                drawer_menu[0]);
        drawerItem[1] = new DrawerItem(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_info)
                .color(Color.GRAY)
                .sizeDp(24),
                drawer_menu[1]);
        drawerItem[2] = new DrawerItem(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_thumb_up)
                .color(Color.GRAY)
                .sizeDp(24),
                drawer_menu[2]);


        DrawerItemAdapter adapter = new DrawerItemAdapter(this, R.layout.drawer_item, drawerItem);
        mLsvDrawerMenu.setAdapter(adapter);

        // 當清單選項的子物件被點擊時要做的動作
        mLsvDrawerMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectMenuItem(position);
            }
        });

    }

    private void selectMenuItem(int position) {
        mCurrentMenuItemPosition = position;

        switch (mCurrentMenuItemPosition) {
            case 0:
                startActivity(new Intent(this, Prefs.class));
                break;
            case 1:
                //startActivity(new Intent(this, AboutActivity.class));
                new LibsBuilder()
                        //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
                        .withAboutAppName(getString(R.string.app_name))
                        .withActivityTitle(getString(R.string.activity_about))
                        .withAboutDescription(getString(R.string.license))
                                //start the activity
                        .start(this);
                break;
            case 2:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.ccjeng.tptrashcan")));
                break;
        }

        // 將選單的子物件設定為被選擇的狀態
        mLsvDrawerMenu.setItemChecked(position, true);

        // 關掉 Drawer
        mDrawerLayout.closeDrawer(mLlvDrawerContent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*
    * Called when the Activity is no longer visible at all. Stop updates and disconnect.
    */
    @Override
    public void onStop() {
        if (locationClient != null) {
            if (locationClient.isConnected()) {
                locationClient.disconnect();
            }
        }
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    /*
    * Called when the Activity is restarted, even before it becomes visible.
    */
    @Override
    public void onStart() {
        super.onStart();
        // Connect to the location services client
        if (locationClient != null) {
            locationClient.connect();
        }
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        getPref();
        if (adView != null)
            adView.pause();

        // 移除位置請求服務
        if (locationClient != null) {
            if (locationClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        locationClient, this);
            }
        }
    }

    /*
    * Called when the Activity is resumed. Updates the view.
     */
    @Override
    protected void onResume() {
        super.onResume();

        //getPref();
        if (adView != null)
            adView.resume();

        // 連線到Google API用戶端
        if (locationClient != null) {
            if (!locationClient.isConnected()) {
                locationClient.connect();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null)
            adView.destroy();
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }

    /*
        SwipeRefreshLayout
     */
    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                parseQuery();
                mSwipeLayout.setRefreshing(false);
            }
        }, REFRESH_DELAY);

    }

    private void parseQuery() {

        myLoc = (currentLocation == null) ? lastLocation : currentLocation;

        //fake location
        if (TPTrashCan.APPDEBUG) {
            myLoc = new Location("");
            //myLoc.setLatitude(24.8979347);
            //myLoc.setLongitude(121.5393508);
            //myLoc.setLatitude(25.0249034);
            //myLoc.setLongitude(121.560214);
            //Taipei City
            myLoc.setLatitude(25.0950492);
            myLoc.setLongitude(121.5246077);

        }

        if (myLoc != null) {

            //set current location to global veriable
            TPTrashCan.setCurrentLocation(myLoc);

            if (TPTrashCan.APPDEBUG)
                Log.d(TAG, "location = " + myLoc.toString());

            // Set up a customized query
            ParseQueryAdapter.QueryFactory<TrashCanItem> factory =
                    new ParseQueryAdapter.QueryFactory<TrashCanItem>() {
                        public ParseQuery<TrashCanItem> create() {

                            ParseQuery<TrashCanItem> query = TrashCanItem.getQuery();

                            query.whereWithinKilometers("location"
                                    , Utils.geoPointFromLocation(myLoc)
                                    , distance
                            );

                            query.setLimit(rowcount);

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
                    progressWheel.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoaded(List<TrashCanItem> objects, Exception e) {

                    progressWheel.setVisibility(View.GONE);

                    //No data
                    if (trashcanList.getCount() == 0) {
                        String msg = String.valueOf(distance) + "公里"
                                + getString(R.string.data_not_found);

                        //String msg = getString(R.string.data_not_found);

                        Crouton.makeText(MainActivity.this, msg, Style.CONFIRM,
                                (ViewGroup)findViewById(R.id.croutonview)).show();
                    }
                }
            });

            trashcanList.setAdapter(trashcanQueryAdapter);

            // Set up the handler for an item's selection
            trashcanList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final TrashCanItem item = trashcanQueryAdapter.getItem(position);
                    //Open Detail Page
                    goIntent(item);
                }
            });

        } else {
            //location error
            Crouton.makeText(MainActivity.this, R.string.location_error, Style.ALERT,
                    (ViewGroup)findViewById(R.id.croutonview)).show();
        }

    }


    /*
    * check network state
    * */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /*
 * Verify that Google Play services is available before making a request.
 *
 * @return true if Google Play services is available, otherwise false
 */
    private boolean isGoogleServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            if (TPTrashCan.APPDEBUG) {
                Log.d(TAG, "Google play services available");
            }
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Log.d(TAG, "Google play services NOT available");
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                dialog.show();
            }
            return false;
        }
    }

    /*
 * Get the current location
 */
    private Location getLocation() {
        // If Google Play Services is available
        if (isGoogleServicesAvailable()) {
            // Get the current location
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            return null;
        }
    }

    //Google Play Service ConnectionCallbacks
    // 已經連線到Google Services
    @Override
    public void onConnected(Bundle bundle) {
        if (TPTrashCan.APPDEBUG)
            Log.d(TAG, "onConnected - Connected to location services");


        currentLocation = getLocation();

        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);

        if (TPTrashCan.APPDEBUG)
            Log.d(TAG, "onConnected - isConnected =" + locationClient.isConnected());

        //call Parse service to get data
        parseQuery();


    }

    // Google Services連線中斷
    // int參數是連線中斷的代號
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    // Google Services連線失敗
    // ConnectionResult參數是連線失敗的資訊
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();
        Log.i(TAG, "GoogleApiClient connection failed");

        // 裝置沒有安裝Google Play服務
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Crouton.makeText(MainActivity.this, R.string.google_play_service_missing, Style.ALERT,
                (ViewGroup)findViewById(R.id.croutonview)).show();
        }

    }

    // 建立Google API用戶端物件
    private synchronized void configGoogleApiClient() {
        // Create a new location client, using the enclosing class to handle callbacks.
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    private void configLocationRequest() {
        // Create a new global location parameters object
        locationRequest = LocationRequest.create();

        // Set the update interval
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use low power
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        // Set the interval ceiling to one minute
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
    }

    // 位置改變
    // Location參數是目前的位置
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (lastLocation != null
                && Utils.geoPointFromLocation(location)
                .distanceInKilometersTo(Utils.geoPointFromLocation(lastLocation)) < 0.01) {
            // If the location hasn't changed by more than 10 meters, ignore it.
            return;
        }
        lastLocation = location;
    }

    private void goIntent(TrashCanItem item) {

        ga.trackEvent(this, "Location", "Region", item.getRegion(), 0);
        ga.trackEvent(this, "Location", "Address", item.getFullAddress(), 0);

        Intent intent = new Intent();
        intent.setClass(this, InfoActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString("fromLat", String.valueOf(myLoc.getLatitude()));
        bundle.putString("fromLng", String.valueOf(myLoc.getLongitude()));
        bundle.putString("toLat", String.valueOf(item.getLocation().getLatitude()));
        bundle.putString("toLng", String.valueOf(item.getLocation().getLongitude()));

        bundle.putString("address", item.getFullAddress());
        bundle.putString("memo", item.getMemo());
        //bundle.putString("objectId", item.getObjectId());

        intent.putExtras(bundle);
        startActivityForResult(intent, 0);

    }

    private void getPref() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        distance = Integer.valueOf(prefs.getString("distance", "1")) + 1;
        rowcount = Integer.valueOf(prefs.getString("rowcount", "20"));
    }

    private void adView() {
        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest;

        if (TPTrashCan.APPDEBUG) {
            //Test Mode
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice(TPTrashCan.ADMOB_TEST_DEVICE_ID)
                    .build();
        } else {
            adRequest = new AdRequest.Builder().build();

        }
        adView.loadAd(adRequest);
    }
}
