package com.example.neomi.protitulo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GpsStatus.Listener {
// Variables BD
    private DatabaseReference mDatabaseRef;
    final String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static final String TAG = "Grabando Ubicacion";
    DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");

// Variables Ubicacion
    private Location location;
    private TextView locationTv;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 10000, FASTES_INTERVAL = 10000; //1000 ms = 1 seg
//***

    BroadcastReceiver broadcastReceiver;
    private String ACTIVIDAD, CONFIANZA;


    // Variables Satellite
    private LocationManager mLocationManager;
    private LocationProvider mProvider;
    /*
     * Variables GnssStatus
     */
    // Android M (6.0.1) and below status and listener
    private GpsStatus mGpsStatus;
    private GpsStatus.Listener mGpsStatusListener;

    // Android N (7.0) and above status and listeners

    GnssStatus mGnssStatus;
    GnssStatus.Callback mGnssStatusListener;
    public ArrayList<Satellite> satellites ;
    private boolean gpsPermissionGranted = false;
    private SharedPreferences sharedPreferences;
    private Context context;
    private long minTime; // Min Time between location updates, in milliseconds
    private float minDistance; // Min Distance between location updates, in meters
    int satelliteCount;

    GnssMeasurementsEvent.Callback mGnssMeasurementsListener;
    OnNmeaMessageListener mOnNmeaMessageListener;
    GnssNavigationMessage.Callback mGnssNavMessageListener;

    // Variables Acelerometro y magnetometro
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        float[] gData = new float[3]; // accelerometer
        float[] mData = new float[3]; // magnetometer
        float[] rMat = new float[9];
        float[] iMat = new float[9];
        float[] orientation = new float[3];

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            float[] data;
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    gData = event.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mData = event.values.clone();
                    break;
                default:
                    return;
            }

            if (SensorManager.getRotationMatrix(rMat, iMat, gData, mData)) {
                mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
                dimX = event.values[0];
                dimY = event.values[1];
                dimZ = event.values[2];
            }
        }
    };
    private int mAzimuth = 0; //grados
    private float dimX;
    private float dimY;
    private float dimZ;
    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    boolean haveAccelerometer = false;
    boolean haveMagnetometer = false;

    //lista de presmisos.

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private static final int ALL_PERMISSIONS_RESULT = 1011;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTv = findViewById(R.id.location);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        // we add permissions we need to request location of the users
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);
//inicia locacion del usuario.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.
                        toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                  addOnConnectionFailedListener(this).build();
//inicia detector de actividad del usuario
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }

        };

        //GNSSSTATUS
        context = getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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


        //Manager del Sensor

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        haveAccelerometer = mSensorManager.registerListener(mSensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);

        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        haveMagnetometer = mSensorManager.registerListener(mSensorEventListener, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);

        if (haveAccelerometer && haveMagnetometer) {
            // ready to go
        } else {
            // unregister and stop
        }
        Log.d(TAG, "onCreate: Registered accelerometer listener");


    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermission) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermission) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTracking();
        //Ubicacion
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
        StartGNSSGPS();

        //
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Ubicacion
        googleApiClient.reconnect();
        //
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
        //Ubicacion
        if (!checkPlayServices()) {
            locationTv.setText("Necesitas instalar Google Play Services para usar la aplicacion");
        }
        //
    }

    //para detener el proceso de recoleccion de datos (BORRAR DESPUES)
    @Override
    protected void onPause() {
        super.onPause();
        startTracking();
        StartGNSSGPS();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        Toast.makeText(this, "Estoy en pausa", Toast.LENGTH_LONG).show();
        //Ubicacion
        googleApiClient.reconnect();
        //
    }
// Ubicacion
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }
            return false;
        }
        return true;

    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        mLocationManager.addGpsStatusListener(this);
        if (location != null) {
            Toast.makeText(this, "Bienvenido al sistema", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Ubicacion no encontrada", Toast.LENGTH_LONG).show();
        }
        startLocationUpdates();


    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTES_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Debes activar los permisos para mostrar tu ubicacion", Toast.LENGTH_SHORT).show();

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }
//

//******


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    //******
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            final double latitud = location.getLatitude();
            final double longitud = location.getLongitude();
            final double altitud = location.getAltitude();
            final float velocidad = location.getSpeed();
            final String Actividad = ACTIVIDAD;
            final String Confianza = CONFIANZA;
            final float Azimuth = mAzimuth;
            final float X = dimX;
            final float Y = dimY;
            final float Z = dimZ;
            String date = df.format(Calendar.getInstance().getTime());
            int CantSatelites = satelliteCount;
            String ListaSatellite = satellites.toString();
            ArrayList<Satellite> Sat = satellites;


            locationTv.setText(String.format
                    ("Latitud: %s\n  Longitud: %s\n Altitud: %s\n Velocidad: %s\n Actividad: %s\n confianza: %s\n Azimuth: %s\n X : %s\n Y : %s\n Z : %s\n SATELLITE:%s\n  Fecha: %s\n CantidadLista: %s",
                    latitud, longitud, altitud, velocidad, Actividad, Confianza, Azimuth, X, Y, Z, CantSatelites, date, ListaSatellite));


            writeNewLocation(UserId, date, latitud, longitud, altitud, velocidad, Actividad, Confianza, Azimuth, X, Y, Z, CantSatelites, satellites);


        }

    }

    public void writeNewLocation(String userId, String date, double latitud, double longitud, double altitud, float velocidad,
                                 String actividad, String confianza, float azimuth, float X, float Y, float Z, int cantSat, ArrayList<Satellite> listaSatelites) {
        String key = mDatabaseRef.push().getKey();
        Ubicacion ubicacion = new Ubicacion(UserId, date, latitud, longitud, altitud, velocidad, actividad, confianza, azimuth,X,Y,Z, cantSat, listaSatelites);
        Map<String, Object> ubicacionValues = ubicacion.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Ubicaciones/" + key, ubicacionValues);
        childUpdates.put("/Ubi-por-usuario/" + UserId + "/" + key, ubicacionValues);

        mDatabaseRef.updateChildren(childUpdates);

    }
//******
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm);
                    }
                }
                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            new AlertDialog.Builder(MainActivity.this).
                                    setMessage("Estos permisos son necesarios para obtener tu posicion, debes aceptarlos").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]),
                                                        ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).
                                    setNegativeButton("Cancel", null).create().show();
                        }
                    }
                } else {
                    if (googleApiClient != null) {
                        googleApiClient.connect();
                    }

                }
                break;
        }
    }
  
    public void volverLogIn(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        FirebaseAuth.getInstance().signOut();
        finish();
    }




    //Reconocer Actividad de Usuario.
    private void handleUserActivity(int type, int confidence) {
        String label = getString(R.string.activity_unknown);


        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = getString(R.string.activity_in_vehicle);

                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = getString(R.string.activity_on_bicycle);

                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = getString(R.string.activity_on_foot);

                break;
            }
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);

                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);

                break;
            }
            case DetectedActivity.TILTING: {
                label = getString(R.string.activity_tilting);

                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);

                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = getString(R.string.activity_unknown);
                break;
            }
        }

        Log.e(TAG, "User activity: " + label + ", Confidence: " + confidence);

        if (confidence > Constants.CONFIDENCE) {
            ACTIVIDAD = new String(label);
            CONFIANZA = new String(confidence + "%");
        }

    }

    private void startTracking() {
        Intent intent1 = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent1);
    }

    private void stopTracking() {
        Intent intent1 = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent1);
    }

    //fin reconocer actividad de usuario.

// Funcion que incia GnssStatus

    public void StartGNSSGPS() {
        if (Constants.postNougat) addGnssStatusListener();
        else addGpsStatusListener();
    }

    public void addGnssStatusListener() {mGnssStatusListener = new GnssStatus.Callback() {
             @Override
             public void onSatelliteStatusChanged(GnssStatus status) {
                             mGnssStatus = status;
                             satelliteCount = mGnssStatus.getSatelliteCount();
                             satellites = new ArrayList<>();
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
        float azimuth = mGnssStatus.getAzimuthDegrees(i);
        float elevation = mGnssStatus.getElevationDegrees(i);
        float snr = mGnssStatus.getCn0DbHz(i);
        int prn = mGnssStatus.getSvid(i);
        boolean used = mGnssStatus.usedInFix(i);
        int type = mGnssStatus.getConstellationType(i);
        return (new Satellite(azimuth, elevation, snr, prn, used, type));
    }

    @Deprecated
    private void addGpsStatusListener() {
        Log.d(TAG, "Adding Gps status listener");
        mGpsStatusListener = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mGpsStatus = mLocationManager.getGpsStatus(mGpsStatus);
                switch (event) {
                    case GpsStatus.GPS_EVENT_STARTED: break;
                    case GpsStatus.GPS_EVENT_STOPPED: break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX: break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        //Log.d(TAG, "Checking status", localLog);
                        GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                        satellites = new ArrayList<>();
                        for(GpsSatellite satellite: gpsStatus.getSatellites())
                            satellites.add(getSatellite(satellite));
                        putSatellitePreferences();
                        break;
                }
            }
        };
        if (Constants.postMarshmallow && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            gpsPermissionGranted = true;
        if (gpsPermissionGranted)
            mLocationManager.addGpsStatusListener(mGpsStatusListener);
    }

    // populate a satellite from gps satellite
    private Satellite getSatellite(GpsSatellite gpsSatellite) {
        float azimuth = gpsSatellite.getAzimuth();
        float elevation = gpsSatellite.getElevation();
        float snr = gpsSatellite.getSnr();
        int prn = gpsSatellite.getPrn();
        boolean used = gpsSatellite.usedInFix();
        return (new Satellite(azimuth, elevation, snr, prn, used));
    }

    // save the satellite data in preferences
    private void putSatellitePreferences() {
        int numSatellites = satellites.size();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.GPS_SATELLITES, numSatellites);
        int used_satellites = 0;
        float total_snr = 0;
        for (int i = 0; i < satellites.size(); i++) {
            editor.putFloat(Constants.GPS_AZIMUTH + "_" + i, satellites.get(i).getAzimuth());
            editor.putFloat(Constants.GPS_ELEVATION + "_" + i, satellites.get(i).getElevation());
            editor.putFloat(Constants.GPS_SNR + "_" + i, satellites.get(i).getSnr());
            editor.putInt(Constants.GPS_PRN + "_" + i, satellites.get(i).getPrn());
            editor.putBoolean(Constants.GPS_USED_SATELLITES + "_" + i, satellites.get(i).isUsed() );
            if (satellites.get(i).isUsed()) {
                used_satellites++;
                total_snr += satellites.get(i).getSnr();
            }
        }
        float avg_snr = (used_satellites > 0) ? total_snr / used_satellites: 0.0f;
        editor.putFloat(Constants.GPS_SATINFO, avg_snr);
        editor.putInt(Constants.GPS_USED_SATELLITES, used_satellites);
        //Log.d(TAG, "No. of used satellites: " + used_satellites + " SNR: " + total_snr, localLog);
        editor.apply();
    }



    @RequiresApi(Build.VERSION_CODES.N)
    private void removeStatusListener() {
        if (mLocationManager != null) {
            if (Constants.postNougat &&(mGnssStatusListener != null))
                mLocationManager.unregisterGnssStatusCallback(mGnssStatusListener);
            else if (mGpsStatusListener != null)
                mLocationManager.removeGpsStatusListener(mGpsStatusListener);
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    public void onProviderEnabled(String provider) {
    }


    public void onProviderDisabled(String provider) {
    }


    @Override
    public void onGpsStatusChanged(int i) {

    }
}
