package com.example.nemanja.upoznajkraljevo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference mDatabase;
    ImageView image;

    Intent intentMyService;
    ComponentName service;
    BroadcastReceiver receiver;
    String GPS_FILTER = "com.example.nemanja.mylocationtracker.LOCATION";

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mAuth= FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        try {
            GetUserData();
        }
        catch (Exception e){
            System.out.print(e.toString());
        }


        if(!runtimePermisions()){
            startService(new Intent(this,MyService.class));
        }

        //Location Service start
        intentMyService = new Intent(this, MyService.class);
        service = startService(intentMyService);

        IntentFilter mainFilter = new IntentFilter(GPS_FILTER);
        receiver = new MyMainLocalReceiver();
        registerReceiver(receiver, mainFilter);
        //Location Service end

        View hView =  navigationView.getHeaderView(0);
        image=(ImageView) hView.findViewById(R.id.pp);
        linkLayouts();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Intent openRang=new Intent(MainActivity.this.getApplicationContext(),RangList.class);
            startActivity(openRang);
        } else if (id == R.id.nav_gallery) {

            Intent profile=new Intent(MainActivity.this.getApplicationContext(),ProfileActivity.class);
            startActivity(profile);

        } /*else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        }*/ else if (id == R.id.nav_share) {

            startService(new Intent(MainActivity.this.getApplicationContext(), MyService.class));
            Toast toast = Toast.makeText(getApplicationContext(), "Uključili ste notifikacije!", Toast.LENGTH_SHORT);
            toast.show();


        } else if (id == R.id.nav_send) {

            stopService(new Intent(MainActivity.this.getApplicationContext(), MyService.class));

            Toast toast = Toast.makeText(getApplicationContext(), "Isključili ste notifikacije!", Toast.LENGTH_SHORT);
            toast.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean runtimePermisions(){
        if(Build.VERSION.SDK_INT>=23 && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA},100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==100){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED)
            {
                Context context = getApplicationContext();
                CharSequence text = "Super";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            else
            {
                Context context = getApplicationContext();
                CharSequence text = "O ne! Da bi aplikacija funkcionisla potrebno je da omogućite GPS";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                runtimePermisions();
            }


        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void GetUserData() throws IOException
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String userName = user.getDisplayName();
            View headerView = navigationView.getHeaderView(0);
            TextView navUsername = (TextView) headerView.findViewById(R.id.userInfo);
            navUsername.setText(userName);

            String userEmail = user.getEmail();
            View headerView1 = navigationView.getHeaderView(0);
            TextView navTextView = (TextView) headerView.findViewById(R.id.textView);
            navTextView.setText(userEmail);

            String userID=user.getUid();
            StorageReference storageReference = storageRef.child(userID+".jpg");


            final File localFile = File.createTempFile(userID, "jpg");

            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    Bitmap myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    image.setImageBitmap(myBitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
    }

    private void linkLayouts(){
        LinearLayout NiskaTvrdjava= (LinearLayout) findViewById(R.id.galerija_marzik);
        NiskaTvrdjava.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","galerija_marzik");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.galerija_marzik1);
                images.add(R.drawable.galerija_marzik2);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout KonstantinVeliki= (LinearLayout) findViewById(R.id.hram_svetog_save);
        KonstantinVeliki.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","hram_svetog_save");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.hram_ss1);
                images.add(R.drawable.hram_ss2);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout Medijana= (LinearLayout) findViewById(R.id.isposnica_svetog_save);
        Medijana.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","isposnica_svetog_save");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.isposnica_ss);
                images.add(R.drawable.isposnica_ss1);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout CeleKula= (LinearLayout) findViewById(R.id.kraljevacko_pozoriste);
        CeleKula.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","kraljevacko_pozoriste");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.pozoriste_1);
                images.add(R.drawable.pozoriste_2);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout LatinskaCrkva= (LinearLayout) findViewById(R.id.maglic);
        LatinskaCrkva.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","maglic");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.maglic_1);
                images.add(R.drawable.maglic_2);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout CrkvaSN= (LinearLayout) findViewById(R.id.manastir_studenica);
        CrkvaSN.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","manastir_studenica");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.manastir_studenica_1);
                images.add(R.drawable.manastir_studenica_2);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout Sinagoga= (LinearLayout) findViewById(R.id.narodna_biblioteka);
        Sinagoga.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","narodna_biblioteka");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.biblioteka);
                images.add(R.drawable.biblioteka_1);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout ZgradaBanovine= (LinearLayout) findViewById(R.id.narodni_muzej);
        ZgradaBanovine.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","narodni_muzej_u_kraljevu");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.muzej);
                images.add(R.drawable.muzej_1);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout OficirskiDom= (LinearLayout) findViewById(R.id.saborna_crkva_svete_trojice);
        OficirskiDom.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","saborna_crkva_svete_trojice");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.saborna);
                images.add(R.drawable.saborna_1);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout CrkvaSvPant= (LinearLayout) findViewById(R.id.spomen_park_kraljevo);
        CrkvaSvPant.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","spomen_park_kraljevo");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.spark_1);
                images.add(R.drawable.spark_2);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout CrkvaSvAM= (LinearLayout) findViewById(R.id.spomenik_ibarskim_junacima);
        CrkvaSvAM.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","spomenik_ibarskim_junacima");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.ibarski);
                images.add(R.drawable.ibarski_1);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout ssv= (LinearLayout) findViewById(R.id.spomenik_srpskom_vojniku1);
        ssv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","spomenik_srpskom_vojniku");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.ssv);
                images.add(R.drawable.ssv_1);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout SabornaCrkva= (LinearLayout) findViewById(R.id.sportski_objekti_kraljevo);
        SabornaCrkva.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","sportski_objekti_kraljevo");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.so);
                images.add(R.drawable.so_1);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout Bubanj= (LinearLayout) findViewById(R.id.zica);
        Bubanj.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","zica");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.zica_1);
                images.add(R.drawable.zica_2);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

        LinearLayout Logor= (LinearLayout) findViewById(R.id.zupa);
        Logor.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent openInfo=new Intent(MainActivity.this.getApplicationContext(),SpotInfo.class);
                openInfo.putExtra("spot","zupa_svetog_mihaela_arkandjela");
                ArrayList<Integer> images=new ArrayList<Integer>();
                images.add(R.drawable.zupa);
                images.add(R.drawable.zupa_1);
                openInfo.putExtra("images",images);
                startActivity(openInfo);
            }
        });

    }

    private class MyMainLocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = intent.getDoubleExtra("latitude", -1);
            double longitude = intent.getDoubleExtra("longitude", -1);
            /*EditText lon = (EditText) findViewById(R.id.lon);
            EditText lat = (EditText) findViewById(R.id.lat);
            lon.setText(String.valueOf(longitude));
            lat.setText(String.valueOf(latitude));*/
            //Toast.makeText(getApplicationContext(), String.valueOf(latitude) +  " " + String.valueOf(longitude), Toast.LENGTH_LONG).show();
        }
    }


}
