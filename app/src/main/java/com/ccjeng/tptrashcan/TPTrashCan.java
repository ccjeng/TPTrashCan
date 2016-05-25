package com.ccjeng.tptrashcan;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * Created by andycheng on 2015/9/2.
 */
public class TPTrashCan extends Application {
    // Debugging switch 
    public static final boolean APPDEBUG = BuildConfig.DEBUG;

    // Debugging tag for the application
    public static final String APPTAG = Application.class.getSimpleName();

    //Admob
    public static final String ADMOB_TEST_DEVICE_ID = "3C8B080F4F918474804E2685BA46E2B3";
    public static final String ADMOB_UNIT_ID = "ca-app-pub-6914084100751028/2129470418";


    @Override
    public void onCreate() {
        super.onCreate();

    //    Firebase.setAndroidContext(this);
    //    Firebase.getDefaultConfig().setPersistenceEnabled(true);

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

}
