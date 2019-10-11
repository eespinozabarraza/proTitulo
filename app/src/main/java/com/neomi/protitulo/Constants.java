package com.neomi.protitulo;

import android.Manifest;
import android.os.Build;

import androidx.annotation.RequiresApi;


public class Constants {
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String BACKGROUND = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
    @RequiresApi(api = Build.VERSION_CODES.P)
    public static final String FOREGROUND = Manifest.permission.FOREGROUND_SERVICE;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static final String ACTIVITY = Manifest.permission.ACTIVITY_RECOGNITION;

    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";
    public static final String BROADCAST_GNSS_INFO = "activity_intent";
    public static final String BROADCAST_GPSS_INFO = "activity_intent";

    public static final String BROADCAST_LOCATION_ACTIVITY = "activity_intent";

    static final long DETECTION_INTERVAL_IN_MILLISECONDS = 10 * 1000;

    public static final float SMALLEST_DISPLACEMENT = 10;

    public static final int CONFIDENCE = 70;

    // shared parameters based on the settings -- key is stored in preferences.xml
    final static String PREF_WALKING_SPEED = "Walking_speed";
    public final static String PREF_DEBUG_MODE = "Debug_mode";
    public final static String PREF_TRIP_TYPE = "Trip_type";
    final static String PREF_PLOT_SPEED = "Plot_speed";
    final static String PREF_NUMBER_TRIP_ROWS = "Number_of_trip_rows";
    public final static String PREF_NUMBER_TRIP_READINGS = "Number_of_trip_readings";
    final static String PREF_WAKEUP_GPS = "wakeup_gps";
    // constants for GPS Satellites
    public final static String GPS_SATELLITES = "gps_satellites";
    public final static String GPS_USED_SATELLITES = "gps_used_satellites";
    public final static String GPS_SNR = "gps_snr";
    public final static String GPS = "GPS";
    final static String GPS_AZIMUTH = "gps_azimuth";
    final static String GPS_ELEVATION = "gps_elevation";
    final static String GPS_PRN = "gps_prn";
    final static String GPS_TYPE = "gps_type";
    final static String GPS_SATINFO = "gps_satinfo";
    final static String GLONASS = "GLONASS";
    // location preferences (shared parameters)
    public final static String PREF_LOCATION_PROVIDER = "Location_provider";
    public final static String PREF_LOCATION_ACCURACY = "Location_accuracy";

    public final static String PREF_POLL_INTERVAL = "Poll_interval";

    public final static String PREF_TRIP_DURATION = "Duration";

    // android versions
    final static boolean preMarshmallow  =  Build.VERSION.SDK_INT < Build.VERSION_CODES.M; // < 23
    final static boolean postMarshmallow =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.M; // >= 23
    final static boolean postNougat =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.N; // >= 24
    final static boolean postOreo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O; // >= 26
    final static boolean postPie = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P; //>=28

}
