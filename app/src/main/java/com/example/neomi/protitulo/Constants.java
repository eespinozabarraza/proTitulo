package com.example.neomi.protitulo;

import android.os.Build;

public class Constants {
    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";

    static final long DETECTION_INTERVAL_IN_MILLISECONDS = 10 * 1000;

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

    // android versions
    final static boolean preMarshmallow  =  Build.VERSION.SDK_INT < Build.VERSION_CODES.M; // < 23
    final static boolean postMarshmallow =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.M; // >= 23
    final static boolean postNougat =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.N; // >= 24

}
