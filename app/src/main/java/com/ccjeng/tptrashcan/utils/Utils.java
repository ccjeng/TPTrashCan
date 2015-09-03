package com.ccjeng.tptrashcan.utils;

import android.location.Location;

import com.parse.ParseGeoPoint;

/**
 * Created by andycheng on 2015/9/3.
 */
public class Utils {

    public static ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

}
