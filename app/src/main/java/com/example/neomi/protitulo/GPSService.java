package com.example.neomi.protitulo;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;

/*
  Start a GPS Service to keep track of the number of satellites used for a fix
 */
public class GPSService extends Service implements LocationListener {

    private static final String TAG = "GPSService";
    private LocationManager mLocationManager;
    private LocationProvider mProvider;


    // Android N (7.0) and above status and listeners
    private GnssStatus mGnssStatus;
    private GnssStatus.Callback mGnssStatusListener;

    public ArrayList<Satellite> satellites = new ArrayList<Satellite>();
    private boolean gpsPermissionGranted = false;
    private SharedPreferences sharedPreferences;
    private Context context;
    private long minTime; // Min Time between location updates, in milliseconds
    private float minDistance; // Min Distance between location updates, in meters
    private final IBinder mBinder = new GPSService.LocalBinder();


    public class LocalBinder extends Binder {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int localLog = Integer.parseInt(sharedPreferences.getString(Constants.PREF_DEBUG_MODE, "0"));
        Log.d(TAG, "Started GPS Service");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mProvider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER);
        if (mProvider == null) {
            Log.e(TAG, "Unable to get GPS_PROVIDER");
            return;
        }
        gpsPermissionGranted = Constants.preMarshmallow;

        // set the min time and distance for the location manager

        minTime = Constants.DETECTION_INTERVAL_IN_MILLISECONDS;
        LocationRequest mLocationRequest = new LocationRequest();
        minDistance = mLocationRequest.getSmallestDisplacement();

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("satelite", satellites);
        startActivity(i);





    }

    // called when the service is started
    @Override
    @RequiresApi(Build.VERSION_CODES.N)
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Constants.postMarshmallow && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){gpsPermissionGranted = true;}

        if (gpsPermissionGranted) {mLocationManager.requestLocationUpdates(mProvider.getName(), minTime, minDistance, this);}

        if (Constants.postNougat) {addGnssStatusListener();}
        else{}
        return START_STICKY;
    }

    @Override
    @RequiresApi(Build.VERSION_CODES.N)
    public void onDestroy() {
        removeStatusListener();
        if (Constants.postMarshmallow && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            gpsPermissionGranted = true;
        if (gpsPermissionGranted)
            mLocationManager.removeUpdates(this);
        super.onDestroy();
    }
//GnssStatus

    @RequiresApi(Build.VERSION_CODES.N)
    public void addGnssStatusListener() {
        mGnssStatusListener = new GnssStatus.Callback() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onStopped() {
            }

            @Override
            public void onFirstFix(int ttffMillis) {
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                mGnssStatus = status;
                int satelliteCount = mGnssStatus.getSatelliteCount();
                for (int i = 0; i < satelliteCount; i++)
                    satellites.add(getSatellite(mGnssStatus, i));


            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.registerGnssStatusCallback(mGnssStatusListener);
    }

    @RequiresApi(Build.VERSION_CODES.N)
    // populate a new satellite from gnss status
    private Satellite getSatellite(GnssStatus mGnssStatus, int i) {
        String SatNum = String.valueOf(i);
        float azi = mGnssStatus.getAzimuthDegrees(i);
        String azimuth = String.valueOf(azi);
        float elev = mGnssStatus.getElevationDegrees(i);
        String elevation = String.valueOf(elev);
        float Snr = mGnssStatus.getCn0DbHz(i);
        String snr = String.valueOf(Snr);
        int Prn = mGnssStatus.getSvid(i);
        String prn =String.valueOf(Prn);
        boolean Used = mGnssStatus.usedInFix(i);
        String used =String.valueOf(Used);
        int Type = mGnssStatus.getConstellationType(i);
        String type = " ";
        switch (Type) {
            case GnssStatus.CONSTELLATION_GPS:
                type = "GPS"; break;
            case GnssStatus.CONSTELLATION_BEIDOU:
                type = "BEIDOU"; break;
            case GnssStatus.CONSTELLATION_GLONASS:
                type = "GLONASS"; break;
            case GnssStatus.CONSTELLATION_QZSS:
                type = "QZSS"; break;
            case GnssStatus.CONSTELLATION_GALILEO:
                type = "Galileo"; break;
            case GnssStatus.CONSTELLATION_SBAS:
                type = "SBAS"; break;
            default:
                type = "DEFENSE";
        }

        return (new Satellite(SatNum, azimuth, elevation, snr, prn, used, type));
    }

    @Deprecated



    @RequiresApi(Build.VERSION_CODES.N)
    private void removeStatusListener() {
        if (mLocationManager != null) {
            if (Constants.postNougat &&(mGnssStatusListener != null))
                mLocationManager.unregisterGnssStatusCallback(mGnssStatusListener);
            else;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}