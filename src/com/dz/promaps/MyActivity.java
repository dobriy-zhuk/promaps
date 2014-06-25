package com.dz.promaps;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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


public class MyActivity extends Activity implements LocationListener, GoogleMap.OnMarkerClickListener {


    private Double MAX_DISTANCE = 100.0;
    private Double RESIDUE;


    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    // Maximum results returned from a Parse query
    private static final int MAX_POST_SEARCH_RESULTS = 20;

    private LatLng MY_LOCATION;
    String MARKA;
    GoogleMap map;
    protected LocationManager locationManager;

    Marker marker;
    List<ParseObject> OBJECTS;
    String nowObjectId;
    Map<String, Integer> MAS_MARKER = new HashMap<String, Integer>();//соотносит id маркера с его номером на карте

    RelativeLayout Rel;
    TextView cost_text, azs_text, distance_text, time_text;
    Button mistake, voice, addAzs;
    RatingBar ratingBar;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_xml);

        init();

//принимаем данные пользователя
        MAX_DISTANCE = Double.valueOf(getIntent().getStringExtra("distance"));
        MARKA = getIntent().getStringExtra("marka");


        System.out.print("Got from PlayActivity" + MAX_DISTANCE + " " + MARKA);

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
                getObjects(location);
                onLocationChanged(location);
                Log.d("Test","Got location");
            }
        }
    }

    private void init(){

        Thread thread = new Thread(new Runnable(){//init my PARSE_KEY
            @Override
            public void run() {
                try {
                    Parse.initialize(MyActivity.this, "My ID", "My ID");
                    ParseAnalytics.trackAppOpened(getIntent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        Rel = (RelativeLayout) findViewById(R.id.about_azs);
        azs_text = (TextView) findViewById(R.id.azs_text);
        cost_text = (TextView) findViewById(R.id.cost_text);
        distance_text = (TextView) findViewById(R.id.distance_text);
        time_text = (TextView) findViewById(R.id.time_text);

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setRating((float) 4.5);

        mistake = (Button) findViewById(R.id.mistake);
        mistake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });//проверить

        voice = (Button) findViewById(R.id.voice);
        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(),
                        "Ваш голос учтен!", Toast.LENGTH_LONG).show();
            }
        });

        addAzs = (Button)findViewById(R.id.ButtonAdd);
        addAzs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyActivity.this, AddAzs.class);
                startActivity(intent);
            }
        });
    }

    public void showRatingDialog() {

        final AlertDialog.Builder ratingdialog = new AlertDialog.Builder(this);

        ratingdialog.setIcon(android.R.drawable.btn_star_big_on);
        ratingdialog.setTitle("Исправить цену");

        View linearlayout = getLayoutInflater().inflate(R.layout.popup, null);
        ratingdialog.setView(linearlayout);

        final EditText change_cost = (EditText)linearlayout.findViewById(R.id.change_cost);
        change_cost.setText("33.4");//ДОБАВИТЬ ЗАПЯТЫЕ!!!

        ratingdialog.setPositiveButton("Готово",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Locations");

                        query.getInBackground(nowObjectId, new GetCallback<ParseObject>() {
                            public void done(ParseObject ch_object_cost, ParseException e) {
                                if (e == null) {
                                    // Now let's update it with some new data. In this case, only cheatMode and score
                                    // will get sent to the Parse Cloud. playerName hasn't changed.
                                    ch_object_cost.put(MARKA, Double.parseDouble(change_cost.getText().toString()));
                                    ch_object_cost.saveInBackground();
                                }
                            }
                        });
                        Toast.makeText(getApplicationContext(),
                                "Спасибо, цена вскоре будет исправлена", Toast.LENGTH_LONG).show();
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
    public void onLocationChanged(Location location) {

        // Getting latitude of the current location
       double latitude = location.getLatitude();

        // Getting longitude of the current location
        double longitude = location.getLongitude();

        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);
        MY_LOCATION = latLng;

        // Showing the current location in Google Map
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // Zoom in the Google Map
        map.animateCamera(CameraUpdateFactory.zoomTo(12));

        map.setOnMarkerClickListener(this);

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                get_way(marker);
            }
        });

    }

    private  void getObjects(Location location){

        final ParseGeoPoint myPoint = new ParseGeoPoint(location.getLatitude(),location.getLongitude());//my location
        // query to database Locations
        ParseQuery<ParseObject> mapQuery = ParseQuery.getQuery("Locations");
        // get objects in MAX_DISTANCE
        mapQuery.whereWithinKilometers("location", myPoint, MAX_DISTANCE);

        mapQuery.orderByAscending(MARKA);
        // print SEARCH_RESULT
        mapQuery.setLimit(MAX_POST_SEARCH_RESULTS);
        // get every objects
        mapQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null)
                {//print values of location
                    OBJECTS = objects;
                    Log.d("Test", "Got results: " + objects.size());
                    for(int i = 0; i < objects.size(); i++){
                        System.out.print("OBJECTS " + objects.get(i).getNumber("AI92") + " " + objects.get(i).getNumber("AI95"));
                        System.out.println(" " + objects.get(i).getParseGeoPoint("location").getLongitude() + ","
                                + objects.get(i).getParseGeoPoint("location").getLatitude());
                        }
                    to_map(objects);
                }
                else
                    Log.d("Test", "Location not found: " + e.getMessage());
            }
        });
    }

    public void to_map(List<ParseObject> objects){

        for(int i = 0; i < objects.size(); i++){

          if((objects.get(i).getDouble(MARKA) - objects.get(0).getDouble(MARKA)) <= 5.0){
            MarkerOptions markerOptions = new MarkerOptions();

            LatLng latLng = new LatLng(objects.get(i).getParseGeoPoint("location").getLatitude(),
                    objects.get(i).getParseGeoPoint("location").getLongitude());
            markerOptions.position(latLng);
            markerOptions.title("Проложить маршрут");

            marker = map.addMarker(markerOptions);

            MAS_MARKER.put(marker.getId(),i);
                System.out.println("differences == " + (objects.get(i).getDouble(MARKA) - objects.get(0).getDouble(MARKA)));
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    public boolean get_way(Marker marker) {

        map.clear();
        to_map(OBJECTS);

        LatLng origin = MY_LOCATION;
        LatLng dest = marker.getPosition();

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);

        //сохранение текущих настроек
        preferences = getSharedPreferences("mySettings", Context.MODE_PRIVATE);
        double count_score_diff = (OBJECTS.get(OBJECTS.size() - 1).getDouble(MARKA) - OBJECTS.get(0).getDouble(MARKA));

        int count_score = 0;
        if(preferences.contains("count_score"))
        {
            count_score = preferences.getInt("count_score", 0);
        }
        editor = preferences.edit();
        editor.putInt("count_score", count_score + (int) count_score_diff*10 );
        editor.commit();

        return true;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //Показать информацию для маркера АЗС
        Rel.setVisibility(View.VISIBLE);

        String origin = MY_LOCATION.latitude + "," + MY_LOCATION.longitude;
        String destination = marker.getPosition().latitude + "," + marker.getPosition().longitude;

        nowObjectId = OBJECTS.get(MAS_MARKER.get(marker.getId())).getObjectId();


        // устанавиливаем значения названия и цены для маркера
       ratingBar.setRating((float)OBJECTS.get(MAS_MARKER.get(marker.getId())).getDouble("reyting"));

        Log.i("MARKER_AZS", marker.getId());
        azs_text.setText("АЗС: " + OBJECTS.get(MAS_MARKER.get(marker.getId())).getString("azs_name"));

        Log.i("MARKER_COST",marker.getId());
        cost_text.setText("Цена: " + OBJECTS.get(MAS_MARKER.get(marker.getId())).getNumber(MARKA));

        return false;
    }


    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(7);
                lineOptions.color(Color.RED);
            }

            distance_text.setText("Расстояние: " + distance);
            time_text.setText("Время: " + duration);

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
        }
    }

}
