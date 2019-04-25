package com.example.neomi.protitulo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssNavigationMessage;
import android.location.Location;
import android.location.GnssStatus;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
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
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus.Callback;



import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;



import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private DatabaseReference mDatabaseRef;

    private Location location;
    private TextView locationTv;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL =10000, FASTES_INTERVAL = 10000; //1000 ms = 1 seg

    final String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static final String TAG = "Grabando Ubicacion";

    BroadcastReceiver broadcastReceiver;

    private String ACTIVIDAD,CONFIANZA;

// Variables Satellite
    private LocationManager mLocationManager;
    /**
     * Android N (7.0) and above status and listeners
     */
    private GnssStatus mGnssStatus;

    private GnssStatus.Callback mGnssStatusListener;

    private GnssMeasurementsEvent.Callback mGnssMeasurementsListener;

    private OnNmeaMessageListener mOnNmeaMessageListener;

    private GnssNavigationMessage.Callback mGnssNavMessageListener;
    //lista de presmisos.

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected =new ArrayList<>(  );
    private ArrayList<String> permissions = new ArrayList<>(  );

    private static final int ALL_PERMISSIONS_RESULT = 1011;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        locationTv = findViewById( R.id.location );

        //fecha?//



        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        // we add permissions we need to request location of the users
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION );
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION );

        permissionsToRequest = permissionsToRequest( permissions );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions( permissionsToRequest.
                        toArray( new String[permissionsToRequest.size()] ), ALL_PERMISSIONS_RESULT );
            }
        }
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi( LocationServices.API ).
                addConnectionCallbacks( this ).
                addOnConnectionFailedListener( this ).build();

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
     private ArrayList<String> permissionsToRequest(ArrayList<String>wantedPermission){
        ArrayList<String> result = new ArrayList<>(  );

        for (String perm : wantedPermission){
            if (!hasPermission(perm)){
                result.add(perm);
            }
        }
        return result;
     }
     private boolean hasPermission(String permission){
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            return checkSelfPermission( permission ) == PackageManager.PERMISSION_GRANTED;
        }
        return  true;
     }

    @Override
    protected void onStart() {
        super.onStart();
        startTracking();
        if(googleApiClient != null){
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
        if(!checkPlayServices()){
            locationTv.setText( "Necesitas instalar Google Play Services para usar la aplicacion" );
        }
    }
    //para detener el proceso de recoleccion de datos (BORRAR DESPUES)
   @Override
   protected void onPause() {
       super.onPause();
       startTracking();
       LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
       Toast.makeText(this , "Estoy en pausa", Toast.LENGTH_LONG).show();
       googleApiClient.reconnect();
   }

    private boolean checkPlayServices(){
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable( this );

        if (resultCode != ConnectionResult.SUCCESS){
            if (apiAvailability.isUserResolvableError( resultCode )){
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            }else {
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
            CONFIANZA = new String( confidence +"%");
        }

    }
    private void startTracking() {
        Intent intent1 = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent1);
        Toast.makeText(this, "actividad" +":"+ ACTIVIDAD+" "+ "confianza" +CONFIANZA, Toast.LENGTH_SHORT).show();


    }

    private void stopTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }

    //fin reconocer actividad de usuario.

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ){
            return;
        }


        location = LocationServices.FusedLocationApi.getLastLocation( googleApiClient );
        if (location != null){
            Toast.makeText(this , "Bienvenido al sistema", Toast.LENGTH_LONG).show();
        }else{ Toast.makeText(this , "Ubicacion no encontrada", Toast.LENGTH_LONG).show();}
        startLocationUpdates();




    }
    private void startLocationUpdates(){
        locationRequest = new LocationRequest();
        locationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );
        locationRequest.setInterval( UPDATE_INTERVAL );
        locationRequest.setFastestInterval( FASTES_INTERVAL );

        if(ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ){
            Toast.makeText( this,"Debes activar los permisos para mostrar tu ubicacion", Toast.LENGTH_SHORT ).show();

        }
        LocationServices.FusedLocationApi.requestLocationUpdates( googleApiClient, locationRequest, this );

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult){

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null){
            final double latitud = location.getLatitude();
            final double longitud = location.getLongitude();
            final double altitud = location.getAltitude();
            final float velocidad = location.getSpeed();
            final String Actividad = ACTIVIDAD;
            final String Confianza = CONFIANZA;
            Bundle extras = location.getExtras();
            String proveedor = location.getProvider();

            locationTv.setText( "Latitud: " + latitud+ "\n  Longitud: " + location.getLongitude() + "\n Altitud: " + location.getAltitude()
                    + "\n Velocidad: " + location.getSpeed()+ "\n Actividad: " + Actividad + "\n confianza: " + Confianza + "\n  onLocChange ");


            writeNewLocation( UserId,latitud,longitud,altitud,velocidad,Actividad, Confianza);


        }

    }

    public void writeNewLocation(String userId, double latitud, double longitud, double altitud, float velocidad,
                                 String actividad, String confianza){
        String key = mDatabaseRef.push().getKey();
        Ubicacion ubicacion = new Ubicacion(UserId, latitud,longitud,altitud,velocidad, actividad, confianza);
        Map<String, Object> ubicacionValues = ubicacion.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Ubicaciones/" + key, ubicacionValues);
        childUpdates.put("/Ubi-por-usuario/" + UserId + "/" + key, ubicacionValues);

        mDatabaseRef.updateChildren(childUpdates);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int [] grantResults){
        switch(requestCode){
            case ALL_PERMISSIONS_RESULT:
                for (String perm: permissionsToRequest){
                    if (!hasPermission( perm )){
                        permissionsRejected.add(perm);
                    }
                }
                if (permissionsRejected.size()>0){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if (shouldShowRequestPermissionRationale( permissionsRejected.get(0))){
                            new AlertDialog.Builder( MainActivity.this ).
                                    setMessage("Estos permisos son necesarios para obtener tu posicion, debes aceptarlos").
                                    setPositiveButton( "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                requestPermissions( permissionsRejected.toArray(new String[permissionsRejected.size()]),
                                                        ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    } ).
                                    setNegativeButton( "Cancel", null ).create().show();
                        }
                    }
                }else{
                    if(googleApiClient!=null){
                        googleApiClient.connect();
                    }

                }
                break;
        }
    }

    public void volverLogIn(View view){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        FirebaseAuth.getInstance().signOut();
        finish();
    }






}
