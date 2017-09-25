package com.example.nemanja.upoznajkraljevo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class RangList extends AppCompatActivity {

    DatabaseReference dref;
    ListView listview;
    RangListAdapter adapter;
    List<Korisnik> list;
    Gson gson=new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rang_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listview=(ListView)findViewById(R.id.listview);
        list= new ArrayList<>();
        adapter=new RangListAdapter(getApplicationContext(),list);
        listview.setAdapter(adapter);

        dref=FirebaseDatabase.getInstance().getReference("user");
        dref.orderByChild("score").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Korisnik p=dataSnapshot.getValue(Korisnik.class);
                list.add(0,p);
                listview.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Korisnik korisnik = dataSnapshot.getValue(Korisnik.class);
                String value= korisnik.getFirstname()+" "+korisnik.getLastname() +" "+korisnik.getScore();
                list.remove(value);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
