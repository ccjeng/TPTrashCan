package com.ccjeng.tptrashcan.ui;

/**
 * Data model for a trash item.
 */

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DecimalFormat;

@ParseClassName("TPE112515")
public class TrashCanItem extends ParseObject {

    public String getId() {
        return getString("objectId");
    }

    public String getAddress() {
        return  getString("address");
    }

    public String getMemo() {
        return  getString("memo");
    }

    public String getRegion() {
        return  getString("region");
    }

    public String getRoad() {
        return  getString("road");
    }

    public String getFullAddress() {
        return getString("road") + getString("address");
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public double getDegree(ParseGeoPoint current) {
        double radians = getLocation().distanceInRadiansTo(current);

        return Math.toDegrees(radians);
    }

    public String getDistance(ParseGeoPoint current) {

        Double distance = getLocation().distanceInKilometersTo(current);

        String strDistance = "";
        DecimalFormat df;
        String unit = "";

        if (distance < 1) {
            distance = distance * 1000;
            unit = " 公尺";
        } else {
            unit = " 公里";
        }
        df = new DecimalFormat("#");
        strDistance = df.format(distance);

        return strDistance + unit;
    }

    public static ParseQuery<TrashCanItem> getQuery() {
        return ParseQuery.getQuery(TrashCanItem.class);
    }
}
