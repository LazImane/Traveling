package com.example.traveling;

import java.io.Serializable;

public class SearchInfo implements Serializable {
    public double lat;
    public double lon;
    public String loc_name;

    public int type = 0;

    public SearchInfo(double _lat, double _lon, String _loc_name){
        lat = _lat;
        lon = _lon;
        loc_name = _loc_name;
    }

    public double walkingTimeMinutes(SearchInfo b) {
        double walkSpeed = 3.0;
        double earthRadius = 6371.0; // km

        double dLat = Math.toRadians(b.lat - lat);
        double dLon = Math.toRadians(b.lon - lon);

        double lat1 = Math.toRadians(lat);
        double lat2 = Math.toRadians(b.lat);

        double hav =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(lat1) * Math.cos(lat2)
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(hav), Math.sqrt(1 - hav));

        double distanceKm = earthRadius * c;

        double walkingTimeHours = distanceKm / walkSpeed;

        return walkingTimeHours * 60.0;
    }
}
