package com.neomi.protitulo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.location.LocationRequest;

import java.text.DateFormat;
import java.util.Date;

public class Utils {
    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }
    public static int setSatSignalStrength(float snr) {
        int signalStrength = 0;
        if ( (25 < snr)  ) signalStrength = 4;
        if ( (15 < snr) && (snr <= 25) ) signalStrength = 3;
        if ( (10 < snr) && (snr <= 15) ) signalStrength = 2;
        if ( (0 <= snr ) && (snr <= 10) ) signalStrength = 1;
        return signalStrength;
    }
    public static LocationRequest getLocationRequest(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        //LocationRequest mLocationRequest = new LocationRequest();
        LocationRequest mLocationRequest = LocationRequest.create();
        String locationProvider = sharedPreferences.getString(Constants.PREF_LOCATION_PROVIDER, "");
        switch (locationProvider) {
            case "gps": mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); break;
            default: mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); break;
        }

        // set the smallest displacement in meters before location update is called
        String location_separation = sharedPreferences.getString(Constants.PREF_LOCATION_ACCURACY, "");
        int min_separation;
        switch (location_separation) {
            case "high": min_separation = 20; break;
            case "medium": min_separation = 10; break;
            default: min_separation = 5; break;
        }
        mLocationRequest.setSmallestDisplacement(min_separation);

        // set the time interval in msecs. for location updates based on the cost
        mLocationRequest.setInterval(get_poll_interval(sharedPreferences));
        mLocationRequest.setFastestInterval(1000);

        // set the max trip duration based on settings
        String tripType = sharedPreferences.getString(Constants.PREF_TRIP_TYPE, "");
        long duration = 3600 * 1000;        // milliseonds in an hour
        switch (tripType) {
            case "medium": duration *= 4; break;
            case "long": duration *= 6; break;
            case "really_long": duration *= 48; break;
            default: break;
        }
        mLocationRequest.setExpirationDuration(duration);

        // place the duration in the preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(Constants.PREF_TRIP_DURATION, duration);
        editor.apply();

        return mLocationRequest;

    }
    public static int get_poll_interval(SharedPreferences sharedPreferences) {
        String pollInterval = sharedPreferences.getString(Constants.PREF_POLL_INTERVAL, "");
        int interval;
        switch (pollInterval) {
            case "really_low":interval = 1000; break;
            case "low": interval = 1000 * 5; break;
            case "medium": interval = 1000 * 10; break;
            default: interval = 1000 * 5; break;
        }
        return interval;
    }
}
