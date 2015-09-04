package com.ccjeng.tptrashcan.app;

import android.app.Application;
import android.location.Location;

import com.ccjeng.tptrashcan.R;
import com.ccjeng.tptrashcan.ui.TrashCanItem;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.ParseObject;

import java.util.HashMap;

/**
 * Created by andycheng on 2015/9/2.
 */
public class TPTrashCan extends Application {
    // Debugging switch 
    public static final boolean APPDEBUG = true;

    // Debugging tag for the application
    public static final String APPTAG = Application.class.getSimpleName();

    //Admob
    public static final String ADMOB_TEST_DEVICE_ID = "7710C21FF2537758BF3F80963477D68E";
    //Parse
    private static final String PARSE_APPLICATION_ID = "5fzYdG6YMpMPKBNSqvzhEL1OVoXgcVvlCAghW09Q";
    private static final String PARSE_CLIENT_KEY = "FOdbIafjvlxcuVFEECLPK4vd6K876P5pJsQ2Wu5G";


    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(TrashCanItem.class);
        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);

    }


    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-19743390-18";
    public enum TrackerName {
        APP_TRACKER // Tracker used only in this app.
    }
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            if (APPDEBUG) {
                analytics.getInstance(this).setDryRun(true);
            }
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : analytics.newTracker(R.xml.global_tracker);
            t.enableAdvertisingIdCollection(true);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }

    //Global variable for current location
    private static Location mLocation;
    public static Location getCurrentLocation(){
        return mLocation;
    }
    public static void setCurrentLocation(Location l){
        mLocation = l;
    }
}
