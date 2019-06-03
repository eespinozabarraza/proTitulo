package com.example.neomi.protitulo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GnssNavigationMessage;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.GnssStatus;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.location.GnssMeasurementsEvent;


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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GpsStatus.Listener {

    private DatabaseReference mDatabaseRef;

    private Location location;
    private TextView locationTv;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 10000, FASTES_INTERVAL = 10000; //1000 ms = 1 seg

    final String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static final String TAG = "Grabando Ubicacion";

    BroadcastReceiver broadcastReceiver;

    private String ACTIVIDAD, CONFIANZA;
    // variable GPSStatus
    LocationManager locationManager = null;
    String lSatellites = null;

    // variables GnssStatus

    // Variables Satellite
    LocationManager mLocationManager;
    /**
     * Android N (7.0) and above status and listeners
     */
    GnssStatus mGnssStatus;

    GnssStatus.Callback mGnssStatusCallback;

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
            float dimX = event.values[0];
            float dimY = event.values[1];
            float dimZ = event.values[2];
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
    int satelliteCount;


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

        //GPSSTATUS
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //GnssStatus
        mLocationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mGnssStatusCallback = new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(GnssStatus status) {

                    satelliteCount = status.getSatelliteCount();
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
            mLocationManager.registerGnssStatusCallback(mGnssStatusCallback);
        } else {
            mLocationManager.addGpsStatusListener(this);
        }




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


        //fecha?//


        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        // we add permissions we need to request location of the users
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);

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
        startTracking();

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
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.reconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
        if (!checkPlayServices()) {
            locationTv.setText("Necesitas instalar Google Play Services para usar la aplicacion");
        }
    }

    //para detener el proceso de recoleccion de datos (BORRAR DESPUES)
    @Override
    protected void onPause() {
        super.onPause();
        startTracking();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        Toast.makeText(this, "Estoy en pausa", Toast.LENGTH_LONG).show();
        googleApiClient.reconnect();
    }

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
        Toast.makeText(this, "actividad" + ":" + ACTIVIDAD + " " + "confianza" + CONFIANZA + "azimuth: " + mAzimuth, Toast.LENGTH_SHORT).show();


    }

    private void stopTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }

    //fin reconocer actividad de usuario.

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        locationManager.addGpsStatusListener(this);
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

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

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
            Bundle extras = location.getExtras();
            String proveedor = location.getProvider();
            String CantSatelites = String.valueOf(satelliteCount);

            locationTv.setText("Latitud: " + latitud + "\n  Longitud: " + longitud + "\n Altitud: " + location.getAltitude()
                    + "\n Velocidad: " + location.getSpeed() + "\n Actividad: " + Actividad + "\n confianza: " + Confianza + "\n  " +
                    "Azimuth: " + Azimuth + "\n X : " + X + "\n Y : " + Y + "\n Z : " + Z + "\n  \"SATELLITE:" + CantSatelites + "\n  Fin ");


            writeNewLocation(UserId, latitud, longitud, altitud, velocidad, Actividad, Confianza, Azimuth);


        }

    }

    public void writeNewLocation(String userId, double latitud, double longitud, double altitud, float velocidad,
                                 String actividad, String confianza, Float azimuth) {
        String key = mDatabaseRef.push().getKey();
        Ubicacion ubicacion = new Ubicacion(UserId, latitud, longitud, altitud, velocidad, actividad, confianza, azimuth);
        Map<String, Object> ubicacionValues = ubicacion.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Ubicaciones/" + key, ubicacionValues);
        childUpdates.put("/Ubi-por-usuario/" + UserId + "/" + key, ubicacionValues);

        mDatabaseRef.updateChildren(childUpdates);

    }

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


    @Override
    public void onGpsStatusChanged(int event) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        if(gpsStatus != null) {
            Iterable<GpsSatellite>satellites = gpsStatus.getSatellites();
            Iterator<GpsSatellite> sat = satellites.iterator();

            int i = 0;
            while (sat.hasNext()) {
                GpsSatellite satellite = sat.next();
                lSatellites = "Satellite" + (i++) + ": "
                        + satellite.getPrn() + ","
                        + satellite.usedInFix() + ","
                        + satellite.getSnr() + ","
                        + satellite.getAzimuth() + ","
                        + satellite.getElevation()+ "\n\n";

                Toast.makeText(this, "SATELLITE :" + lSatellites, Toast.LENGTH_LONG).show();
            }
        }else{
            lSatellites = "Estoy aca";
        }return;



    }

// funciones acelerometro


}
