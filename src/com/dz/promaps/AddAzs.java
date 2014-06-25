package com.dz.promaps;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;
import com.parse.*;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AddAzs extends Activity implements LocationListener, GoogleMap.OnMarkerClickListener {

    GoogleMap map;
    Marker marker;

    //Для определению координат АЗС, которую создает пользователь
    ParseGeoPoint destination_azs_geoPoint;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_map_xml);

        final Context context;
        context = AddAzs.this;
        Toast.makeText(context, "Выберите местоположение АЗС на карте", Toast.LENGTH_LONG)
                .show();

        init();

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if(status!= ConnectionResult.SUCCESS){ // Google Play Services are not available
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        }
        else {
           FragmentManager fragment = getFragmentManager();
            MapFragment fm
                = (MapFragment)fragment.findFragmentById(R.id.map);

            //SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            map = fm.getMap();

            map.setMyLocationEnabled(true);

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location
            Location location = locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(provider,20000,0,this);

            if(location!=null){

                onLocationChanged(location);
                Log.d("Test","Got location");
            }
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (marker != null) {
                        marker.remove();
                    }
                    marker = map.addMarker(new MarkerOptions().position(latLng).title("АЗС здесь!"));

                    Toast.makeText(getApplicationContext(),
                            "Теперь нажмите на маркер", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void init(){



    }


    @Override
    public void onLocationChanged(Location location) {

        // Getting latitude of the current location
        final double latitude = location.getLatitude();

        // Getting longitude of the current location
        final double longitude = location.getLongitude();

        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        // Showing the current location in Google Map
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // Zoom in the Google Map
        map.animateCamera(CameraUpdateFactory.zoomTo(12));

        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                showDialogAzs();
            }
        });

    }


    public void showDialogAzs() {

        final AlertDialog.Builder ratingdialog = new AlertDialog.Builder(this);

        ratingdialog.setTitle("Добавление новой АЗС");

        View linearlayout = getLayoutInflater().inflate(R.layout.popup_add_azs, null);
        ratingdialog.setView(linearlayout);
//Получение данных от  пользователя об АЗС
        final EditText azs_name = (EditText)linearlayout.findViewById(R.id.azs_name);
        azs_name.setText("Лукойл");//ДОБАВИТЬ ЗАПЯТЫЕ!!!

        final EditText cost95 = (EditText)linearlayout.findViewById(R.id.new95);
        cost95.setText("34.1");

        final EditText cost92 = (EditText)linearlayout.findViewById(R.id.new92);
        cost92.setText("310.5");

//Сохранение полученных данных о новой АЗС
        ratingdialog.setPositiveButton("Готово",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        ParseObject newAZS = new ParseObject("Locations");
                                    newAZS.put("location", destination_azs_geoPoint);
                                    newAZS.put("azs_name",azs_name.getText().toString());
                                    newAZS.put("reyting", 4);
                                    newAZS.put("AI95", Double.parseDouble(cost95.getText().toString()));
                                    newAZS.put("AI92", Double.parseDouble(cost92.getText().toString()));
                                    newAZS.saveInBackground();

                                    Toast.makeText(getApplicationContext(),
                                            "Спасибо за Вашу активность!", Toast.LENGTH_LONG).show();

                        dialog.dismiss();


                    }
                })

                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        ratingdialog.create();
        ratingdialog.show();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        //получение текущей координаты метки для определения АЗС
        destination_azs_geoPoint = new ParseGeoPoint(marker.getPosition().latitude,marker.getPosition().longitude);
        return false;
    }


}
