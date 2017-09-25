package com.example.nemanja.upoznajkraljevo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class ProfileActivity extends AppCompatActivity {

    // private static final String TAG = MainActivity.class.getSimpleName();
    private DatabaseReference dref;
    private FirebaseDatabase mBase;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String places;

    private ListView visitedPlaces;
    ArrayAdapter<String> adapter;
    ArrayList<String> list=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
       // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      //  setSupportActionBar(toolbar);


        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        if(user!=null) {

            mBase = FirebaseDatabase.getInstance();
            dref = mBase.getReference();


            dref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {

                    Korisnik t = dataSnapshot.child("user").child(user.getUid()).getValue(Korisnik.class);

                    places = t.getPlaces();

                    TextView Name = (TextView) findViewById(R.id.textViewName);
                    Name.setText(t.getFirstname());
                    TextView LastName = (TextView) findViewById(R.id.textViewLastName);
                    LastName.setText(t.getLastname());
                    TextView Email = (TextView) findViewById(R.id.textViewEmail);
                    Email.setText(t.getEmail());
                    TextView PhoneNumber = (TextView) findViewById(R.id.textViewPhone);
                    PhoneNumber.setText(t.getPhonenumber());
                    TextView Points = (TextView) findViewById(R.id.textViewPoints);
                    Points.setText(String.valueOf(t.getScore()));


                    String userID = user.getUid();

                    storage = FirebaseStorage.getInstance();
                    storageRef = storage.getReference();

                    StorageReference sRef = storageRef.child(userID + ".jpg");



                File localFile = null;
                    try {
                        localFile = File.createTempFile(userID, "jpg");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    final File localFile2 = localFile;

                    sRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Local temp file has been created
                            Bitmap myBitmap = BitmapFactory.decodeFile(localFile2.getAbsolutePath());
                            ImageView image = (ImageView) findViewById(R.id.imageView);

                            int width = 800;
                            int height = 600;
                            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
                            parms.gravity = Gravity.CENTER_HORIZONTAL;
                            image.setLayoutParams(parms);

                            image.setImageBitmap(myBitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });

                    //          GOTOVO

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("Failed to read value.", error.toException());
                }
            });


        }
    }
}
