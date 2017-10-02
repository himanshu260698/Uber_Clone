package com.example.hamuj.uber_hj;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ViewRequest extends AppCompatActivity implements LocationListener {

    ListView listView;
    ArrayList<String> listViewContent;
    ArrayList<String> usernames;
    ArrayList<Double> latitudes;
    ArrayList<Double> longitudes;
    ArrayAdapter arrayAdapter;

    Location location;
    LocationManager locationManager;
    String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);
        listView = (ListView) findViewById(R.id.listView);
        listViewContent = new ArrayList<String>();
       // listViewContent.add("TEST");
        usernames = new ArrayList<String>();
        latitudes = new ArrayList<Double>();
        longitudes = new ArrayList<Double>();

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listViewContent);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

               // Log.i("appInfo",usernames.get(position) + latitudes.get(position).toString() +longitudes.get(position).toString());
                Intent i = new Intent(getApplicationContext(), ViewRidersLocation.class);
                i.putExtra("username", usernames.get(position));
               i.putExtra("latitude", latitudes.get(position));
               i.putExtra("longitude", longitudes.get(position));
                i.putExtra("userLatitude", location.getLatitude());
               i.putExtra("userLongitude", location.getLongitude());
                startActivity(i);

            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);

        location = locationManager.getLastKnownLocation(provider);

        if (location != null) {


            updateLocation();

        }
    }



    public  void updateLocation(){
        final ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");

        query.whereNear("requesterLocation", userLocation);
        query.setLimit(100);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (e == null) {

                    Log.i("MyApp", objects.toString());

                    if (objects.size() > 0) {

                        listViewContent.clear();
                        usernames.clear();
                        latitudes.clear();
                        longitudes.clear();


                        for (ParseObject object : objects) {

                            if (object.get("driverUsername") == null) {

                               // for printing userNames   listViewContent.add((String) object.get("requesterUsername"));
                                Double distanceInMiles = userLocation.distanceInKilometersTo((ParseGeoPoint) object.get("requesterLocation"));

                                Double distanceOneDP = (double) Math.round(distanceInMiles * 10) / 10;

                                listViewContent.add(distanceOneDP.toString() + " Km");
                                usernames.add(object.getString("requesterUsername"));
                                latitudes.add(object.getParseGeoPoint("requesterLocation").getLatitude());
                                longitudes.add(object.getParseGeoPoint("requesterLocation").getLongitude());

                            }

                        }

                        arrayAdapter.notifyDataSetChanged();


                    }


                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }








    @Override
    public void onLocationChanged(Location location) {


        updateLocation();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
