package com.example.nemanja.upoznajkraljevo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;

public class MyService extends Service {

    private static final String TAG = "LOCATION_SERVICE";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 5f;
    private int mNotificationId = 001;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyMgr;

    Integer galerija_marzik=2;
    Integer hram_svetog_save=3;
    Integer isposnica_svetog_save=4;
    Integer kraljevacko_pozoriste=5;
    Integer maglic=6;
    Integer manastir_studenica=7;
    Integer narodna_biblioteka=8;
    Integer narodni_muzej_u_kraljevu=9;
    Integer saborna_crkva_svete_trojice=10;
    Integer spomen_park_kraljevo=11;
    Integer spomenik_ibarskim_junacima=12;
    Integer spomenik_srpskom_vojniku=13;
    Integer sportski_objekti_kraljevo=14;
    Integer zica=15;
    Integer zupa_sv_mihaela_arkandjela=16;



    DatabaseReference dref;
    ArrayList<Spot> list;
    HashMap<String,String> keyValueMap;
    HashMap<String,Integer> keyNotificationMap;

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;
        Location currLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }


        @Override
        public void onLocationChanged(Location location)
        {
            currLocation = location;
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            ExecutorService transThread = Executors.newSingleThreadExecutor();
            transThread.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        callBroadcastReceiver();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            Location tasklocation=new Location(LocationManager.GPS_PROVIDER);

            int i=0;
            for(Spot s:list){

                tasklocation.setLatitude(s.getLatitude());
                tasklocation.setLongitude(s.getLongitude());
                double distance = location.distanceTo(tasklocation);

                mBuilder.setContentText("Nalazite se blizu lokacije - "+s.getHeader()+"!");
                Intent resultIntent = new Intent(MyService.this.getApplicationContext(), SpotInfo.class);
                resultIntent.putExtra("spot",String.valueOf(keyValueMap.get(s.getHeader())));
                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                MyService.this.getApplicationContext(),
                                i,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                if(distance<=50.00){
                    mNotifyMgr.notify(keyNotificationMap.get(s.getHeader()), mBuilder.build());
                    mNotificationId++;
                }
                i++;
            }

        }

        public void callBroadcastReceiver(){
            Intent myFilteredResponse = new
                    Intent("com.example.nemanja.mylocationtracker.LOCATION");
            myFilteredResponse.putExtra("latitude", currLocation.getLatitude());
            myFilteredResponse.putExtra("longitude", currLocation.getLongitude());

            sendBroadcast(myFilteredResponse);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    // LocationListener GPSListener;
    //LocationListener NetworkListener;
    LocationListener GPSListener =  new LocationListener(LocationManager.GPS_PROVIDER);
    LocationListener NetworkListener =  new LocationListener(LocationManager.NETWORK_PROVIDER);
    public MyService() {
        list=new ArrayList<Spot>();
        keyValueMap=new HashMap<String,String>();
        keyNotificationMap=new HashMap<String, Integer>();
        final Gson gson=new Gson();
        dref= FirebaseDatabase.getInstance().getReference("spot");
        dref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e("Count " ,""+snapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    Object spot = postSnapshot.getValue();
                    String json=gson.toJson(spot);
                    Spot spot1=gson.fromJson(json,Spot.class);
                    list.add(spot1);

                    switch (spot1.getHeader()){
                        case "Galerija Maržik": keyValueMap.put(spot1.getHeader(),"galerija_marzik");
                            keyNotificationMap.put(spot1.getHeader(),galerija_marzik); break;
                        case "Hram Svetog Save": keyValueMap.put(spot1.getHeader(),"hram_svetog_save");
                            keyNotificationMap.put(spot1.getHeader(),hram_svetog_save); break;
                        case "Isposnica Svetog Save": keyValueMap.put(spot1.getHeader(),"isposnica_svetog_save");
                            keyNotificationMap.put(spot1.getHeader(),isposnica_svetog_save); break;
                        case "Kraljevačko pozorište": keyValueMap.put(spot1.getHeader(),"kraljevacko_pozoriste");
                            keyNotificationMap.put(spot1.getHeader(),kraljevacko_pozoriste); break;
                        case "Maglič": keyValueMap.put(spot1.getHeader(),"maglic");
                            keyNotificationMap.put(spot1.getHeader(),maglic); break;
                        case "Manastir Studenica": keyValueMap.put(spot1.getHeader(),"manastir_studenica");
                            keyNotificationMap.put(spot1.getHeader(),manastir_studenica); break;
                        case "Narodna biblioteka \"Stefan Prvovenčani\"": keyValueMap.put(spot1.getHeader(),"narodna_biblioteka");
                            keyNotificationMap.put(spot1.getHeader(),narodna_biblioteka); break;
                        case "Narodni muzej u Kraljevu": keyValueMap.put(spot1.getHeader(),"narodni_muzej_u_kraljevu");
                            keyNotificationMap.put(spot1.getHeader(),narodni_muzej_u_kraljevu); break;
                        case "Saborna crkva Svete Trojice u Kraljevu": keyValueMap.put(spot1.getHeader(),"saborna_crkva_svete_trojice");
                            keyNotificationMap.put(spot1.getHeader(),saborna_crkva_svete_trojice);break;
                        case "Spomen park Kraljevo": keyValueMap.put(spot1.getHeader(),"spomen_park_kraljevo");
                            keyNotificationMap.put(spot1.getHeader(),spomen_park_kraljevo);break;
                        case "Spomenik ibarskim junacima": keyValueMap.put(spot1.getHeader(),"spomenik_ibarskim_junacima");
                            keyNotificationMap.put(spot1.getHeader(),spomenik_ibarskim_junacima);break;
                        case "Spomenik srpskom vojniku": keyValueMap.put(spot1.getHeader(),"spomenik_srpskom_vojniku");
                            keyNotificationMap.put(spot1.getHeader(),spomenik_srpskom_vojniku);break;
                        case "Sportski objekti Kraljevo": keyValueMap.put(spot1.getHeader(),"sportski_objekti_kraljevo");
                            keyNotificationMap.put(spot1.getHeader(),sportski_objekti_kraljevo);break;
                        case "Žiča": keyValueMap.put(spot1.getHeader(),"zica");
                            keyNotificationMap.put(spot1.getHeader(),zica);break;
                        case "Župa sv. Mihaela Arkanđela": keyValueMap.put(spot1.getHeader(),"zupa_svetog_mihaela_arkandjela");
                            keyNotificationMap.put(spot1.getHeader(),zupa_sv_mihaela_arkandjela);break;
                    }
                    Log.e("Get Data", spot1.getHeader());
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: " ,firebaseError.getMessage());
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        Log.e(TAG, "initializeLocationManager");

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    NetworkListener);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    GPSListener);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_near_me_white_48px)
                        .setContentTitle("Upoznaj Grad - Obavestenje")
                        .setContentText("Nalazite se blizu lokacije od značaja!")
                        .setSound(uri);
        mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder.setVibrate(new long[] {1000,200,1000,200});

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {

            try {
                mLocationManager.removeUpdates(GPSListener);
                mLocationManager.removeUpdates(NetworkListener);
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
