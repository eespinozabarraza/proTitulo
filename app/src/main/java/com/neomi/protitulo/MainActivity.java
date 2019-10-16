package com.neomi.protitulo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, LocationListener {

    // Variables Location
    private Button mRequestLocationUpdatesButton;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private MyReceiver myReceiver;
    private LocationUpdatesService mService =null;
    private boolean mBound = false;
    private TextView locationTv;

    //Location Var

    private double latitud;
    private double longitud;
    private double altitud;
    private float velocidad;
    private String fecha;
    private boolean detect = false;
    int CantSat;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    // Variables BD
    private DatabaseReference mDatabaseRef;
    final String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


    //Variables Actividad

    BroadcastReceiver activityReceiver, GNSSReceiver, GPSReceiver;

    private String ACTIVIDAD;
    private int CONFIANZA;


    // Variables Acelerometro y magnetometro

    private SensorEventListener mSensorEventListener = new SensorEventListener() {


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
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    tData = event.values.clone();
                    break;
                default:
                    return;
            }
            if (mTemperature != null) {
                temp = event.values[0];
            }else{
                temp = -273;
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
    private float temp;
    float[] tData = new float[5];
    float[] gData = new float[3]; // accelerometer
    float[] mData = new float[3]; // magnetometer
    float[] rMat = new float[9];
    float[] iMat = new float[9];
    float[] orientation = new float[3];
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private Sensor mTemperature;
    boolean haveAccelerometer = false;
    boolean haveMagnetometer = false;


    /*
     * VARIABLES GPS Y GNS
     */
    // Variables Satellite
    private LocationManager mLocationManager;
    private LocationProvider mProvider;
    // Android M (6.0.1) and below status and listener
    private GpsStatus mGpsStatus;
    private GpsStatus.Listener mGpsStatusListener;

    // Android N (7.0) and above status and listeners

    GnssStatus mGnssStatus;
    GnssStatus.Callback mGnssStatusListener;
    public ArrayList<Satellite> satellites;
    private boolean gpsPermissionGranted = false;
    private SharedPreferences sharedPreferences;
    private Context context;
    private long minTime; // Min Time between location updates, in milliseconds
    private float minDistance; // Min Distance between location updates, in meters
    int satelliteCount;
    int numSatellites;

    // VARIABLES NMEA
    public String nmea;
    public String Hdop;
    public String Vdop;
    public String Pdop;
    public String geoIdH;
    public String ageOfData = "DGPS not in use";
    public String antenaAltitud;



    GnssMeasurementsEvent.Callback mGnssMeasurementsListener;
    OnNmeaMessageListener mOnNmeaMessageListener;
    GnssNavigationMessage.Callback mGnssNavMessageListener;

    static MainActivity instance;



    public static MainActivity getInstance() {
        return instance;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        myReceiver = new MyReceiver();

        //GNSSSTATUS
        context = getApplicationContext();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        minTime = Constants.DETECTION_INTERVAL_IN_MILLISECONDS;
        LocationRequest mLocationRequest = new LocationRequest();
        minDistance = mLocationRequest.getSmallestDisplacement();

        gpsPermissionGranted = Constants.preMarshmallow;

        setContentView(R.layout.activity_main);
        locationTv = findViewById(R.id.location);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();

            } else{
            mProvider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER);
            if (mProvider == null) {
                Log.e(TAG, "Unable to get GPS_PROVIDER");
                return;
            }}
        }




        activityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }


            }
        };

    }





    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        mRequestLocationUpdatesButton = (Button) findViewById(R.id.btnIniciar);
        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();
                    startTracking();
                    startSensorManager();
                    StartGNSSGPS();
                    detect = true;
                }
            }
        });
        setButtonsState(detect);
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);






        // Restore the state of the buttons when the activity (re)launches.
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
        LocalBroadcastManager.getInstance(this).registerReceiver(activityReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        detect = false;
        stopTracking();
        mService.removeLocationUpdates();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(activityReceiver);
    }


    /**
     * PERMISOS DE TODA LA APLICACION.
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private boolean checkPermissions() {

        if(Constants.postOreo){
            return  ((PackageManager.PERMISSION_GRANTED == (ActivityCompat.checkSelfPermission(this,
                    Constants.FINE_LOCATION)))&&(PackageManager.PERMISSION_GRANTED == (ActivityCompat.checkSelfPermission(this,
                    Constants.FOREGROUND))));}
        else{
            return  PackageManager.PERMISSION_GRANTED == (ActivityCompat.checkSelfPermission(this,
                    Constants.FINE_LOCATION));
        }

    }
    private void requestPermissions() {
        final boolean shouldProvideRationale;
        if(Constants.postPie){
            shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Constants.FINE_LOCATION)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Constants.COARSE_LOCATION)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Constants.BACKGROUND)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Constants.FOREGROUND);
        }else{
            shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Constants.FINE_LOCATION)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Constants.COARSE_LOCATION)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Constants.BACKGROUND);
        }


        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            if(Constants.postPie){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{
                                Constants.FINE_LOCATION,
                                Constants.COARSE_LOCATION,
                                Constants.BACKGROUND,
                                Constants.FOREGROUND
                        },
                        REQUEST_PERMISSIONS_REQUEST_CODE);
            }else{
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{
                                Constants.FINE_LOCATION,
                                Constants.COARSE_LOCATION,
                                Constants.BACKGROUND
                        },
                        REQUEST_PERMISSIONS_REQUEST_CODE);
            }


        } else {
            Log.i(TAG, "Requesting permission");

            if(Constants.postPie){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{
                                Constants.FINE_LOCATION,
                                Constants.COARSE_LOCATION,
                                Constants.BACKGROUND,
                                Constants.FOREGROUND
                        },
                        REQUEST_PERMISSIONS_REQUEST_CODE);

            }else{
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{
                                Constants.FINE_LOCATION,
                                Constants.COARSE_LOCATION,
                                Constants.BACKGROUND
                        },
                        REQUEST_PERMISSIONS_REQUEST_CODE);

            }

        }

    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
                startTracking();
                startSensorManager();
                StartGNSSGPS();

            } else {
                setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
                // Permission denied.


            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    /**
     *FIN PÈRMISOS DE LA APLICACION.
     */
    /**
     * RECEIVER DEL LOCATIONUPDATESSERVICES
     * DESDE ACA PUEDES OBTENER LA DATA+
     */
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null ){
                latitud = location.getLatitude();
                longitud = location.getLongitude();
                altitud = location.getAltitude();
                velocidad = location.getSpeed();
                fecha = DateFormat.getDateTimeInstance().format(new Date());
                locationTv.setText(String.format( "Muchas gracias por compartir tu ubicación"));
                /*
                locationTv.setText(String.format(
                        "Latitud: " + latitud
                                + "\n  Longitud: " + longitud
                                + "\n Altitud: " + altitud
                                + "\n Velocidad: " + velocidad
                                + "\n Fecha: " + fecha
                                + "\n Actividad: " + ACTIVIDAD
                                + "\n Confianza: " + CONFIANZA
                                + "\n dipAzimuth: " + mAzimuth
                                + "\n xAXIS: " + dimX
                                + "\n yAXIS: " + dimY
                                + "\n zAXIS: " + dimZ
                                + "\n CantSat: " + satelliteCount
                                + "\n PDOP: " + Pdop
                                + "\n HDOP: " + Hdop
                                + "\n VDOP: " + Vdop
                                + "\n GEO: " + geoIdH
                                + "\n AGE: " + ageOfData
                                + "\n ANTENA: " +  antenaAltitud
                                + "\n SATELITES: " + satellites.toString()
                ));}*/
                if(Constants.postNougat){
                    CantSat = satelliteCount;
                    writeNewLocation(UserId, fecha, latitud, longitud, altitud, velocidad, ACTIVIDAD, CONFIANZA,
                            mAzimuth, dimX, dimY, dimZ, CantSat,Pdop,Hdop,Vdop,geoIdH,ageOfData, antenaAltitud,
                            satellites, Float.toString(temp));
                }else{
                    CantSat = numSatellites;
                    Pdop  = null;Hdop  = null;Vdop  = null;geoIdH  = null;ageOfData  = null; antenaAltitud = null;
                    writeNewLocation(UserId, fecha, latitud, longitud, altitud, velocidad, ACTIVIDAD, CONFIANZA,
                        mAzimuth, dimX, dimY, dimZ, CantSat,Pdop,Hdop,Vdop,geoIdH,ageOfData, antenaAltitud,
                        satellites, Float.toString(temp));}

            }else{
                locationTv.setText(String.format( "Debes iniciar la recopilación de datos.  Presiona el botón!!"));
            }





        }
    }
    //ESCRIBIR LA DATA EN FIREBASE
    public void writeNewLocation(String userId, String date, double latitud, double longitud, double altitud, float velocidad,
                                 String actividad, int confianza, float azimuth, float X, float Y, float Z, int cantSat,
                                 String positionDop, String horizontalDop, String verticalDop, String geoidHeight, String ageOfGpsData,
                                 String antennaAltitude, ArrayList<Satellite> listaSatelites, String temperatura) {
        String key = mDatabaseRef.push().getKey();
        Ubicacion ubicacion = new Ubicacion(
                UserId, date, latitud, longitud, altitud, velocidad, actividad, confianza,
                azimuth, X, Y, Z, cantSat,positionDop, horizontalDop, verticalDop, geoidHeight,
                ageOfGpsData, antennaAltitude, listaSatelites, temperatura);
        Map<String, Object> ubicacionValues = ubicacion.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Ubicaciones/" + key, ubicacionValues);
        childUpdates.put("/Ubi-por-usuario/" + UserId + "/" + key, ubicacionValues);

        mDatabaseRef.updateChildren(childUpdates);

    }

    //CAMBIAR LOS ESTADOS DEL BOTON RECOLECTOR.
    private void setButtonsState(boolean detect) {
        if (detect) {
            mRequestLocationUpdatesButton.setEnabled(false);
        } else {
            mRequestLocationUpdatesButton.setEnabled(true);
        }
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }


//DESLOGEAR USUARIO

    public void volverLogIn(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    //RECONOCE ACTIVIDAD DEL USUARIO
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
            ACTIVIDAD = label;
            CONFIANZA = confidence;
        }
    }




    private void startTracking() {
        Intent activity = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(activity);
        /* CUANDO SE IMPLEMENTE EL SERVICIO GPSSSERVICE
        Intent gps = new Intent(MainActivity.this , GPSService.class);
        startService(gps); */

    }

    private void stopTracking() {
        Intent activity = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(activity);
    }





    //FIN RECONOCER ACTIVIDAD DE USUARIO.

    //INICIAR LECTURA DE SENSORES
    private void startSensorManager(){
        //Manager del Sensor

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
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

    //FIN LECTURA DE SENSORES

    //INICIO GNSS GPSS Y NMEA
    public void StartGNSSGPS() {
        if (Constants.postNougat) {
            addGnssStatusListener();
            addNameaStatusListener();
        }else{
            addGpsStatusListener();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addNameaStatusListener(){
        mOnNmeaMessageListener = new OnNmeaMessageListener() {
            @Override
            public void onNmeaMessage(String message, long timestamp) {
                nmea = message;

                String[] tokens = nmea.split(",");
                boolean isGGA = tokens[0].toUpperCase().contains("GGA");
                boolean isGSA = tokens[0].toUpperCase().contains("GSA");
                if (isGSA) {

                    if (tokens.length > 15 && !IsNullOrEmpty(tokens[15])) {
                        if (tokens[15]== null){
                            Pdop = "Not Valid";
                        }else{
                            Pdop = tokens[15];
                        }
                    }
                }
                if (isGGA) {
                    if (tokens.length > 8 &&!IsNullOrEmpty(tokens[8])) {
                        if (tokens[8]== null){
                            Hdop = "Not Valid";
                        }else{
                            Hdop = tokens[8];
                        }

                    }
                }
                else if (isGSA) {
                    if (tokens.length > 16 &&!IsNullOrEmpty(tokens[16])) {
                        if (tokens[16]== null){
                            Hdop = "Not Valid";
                        }else{
                            Hdop = tokens[16];
                        }
                    }
                }
                if (isGSA) {
                    if (tokens.length > 17 &&!IsNullOrEmpty(tokens[17])) {
                        if (tokens[17].contains("*")) {
                            String VdopAux = tokens[17].split("\\*")[0];
                            Vdop = VdopAux;
                        }else{
                            Vdop = tokens[17];
                        }

                    }
                }
                if (isGGA) {
                    if (tokens.length > 11 &&!IsNullOrEmpty(tokens[11])) {
                        geoIdH = tokens[11];
                    }
                }

                if (isGGA) {
                    if (tokens.length > 13 && !IsNullOrEmpty(tokens[13])) {
                        ageOfData = tokens[13];
                    }else{
                        ageOfData = "DGPS not in use";
                    }
                }

                if (isGGA) {
                    if (tokens.length > 9 &&!IsNullOrEmpty(tokens[9])) {

                        antenaAltitud = tokens[9];


                    }
                }





            }
            public boolean IsNullOrEmpty(String text){
                return text == null ||  text.trim().length() == 0;

            }

        };


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.addNmeaListener(mOnNmeaMessageListener);
    }




    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addGnssStatusListener() {

        mGnssStatusListener = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                mGnssStatus = status;
                satelliteCount = mGnssStatus.getSatelliteCount();
                satellites = new ArrayList<>();
                for (int i = 0; i < satelliteCount; i++)
                    satellites.add(getSatellite(mGnssStatus, i));
                // putSatellitePreferences();
            }

        };

        if (ActivityCompat.checkSelfPermission(this, Constants.FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onGpsStatusChanged(int event) {
                if (checkSelfPermission(Constants.FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
        if (Constants.postMarshmallow && ContextCompat.checkSelfPermission(context, Constants.FINE_LOCATION)
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
        numSatellites = satellites.size();
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




}
