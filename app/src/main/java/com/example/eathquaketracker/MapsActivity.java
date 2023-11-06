package com.example.eathquaketracker;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.eathquaketracker.Model.EarthQuake;
import com.example.eathquaketracker.Util.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.eathquaketracker.databinding.ActivityMapsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GoogleMap mMap;
//    private ActivityMapsBinding binding;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        queue = Volley.newRequestQueue(this);

        getEarthQuakes();
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .title("Hello"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));

        }

        // Add a marker in Sydney and move the camera
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    public void getEarthQuakes() {

        final EarthQuake earthQuake = new EarthQuake();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Constants.URL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features = response.getJSONArray("features");
                            for (int i=0; i < Constants.LIMIT; i++) {
                                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                                JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");
                                JSONArray coordinates = geometry.getJSONArray("coordinates");

                                double lon = coordinates.getDouble(0);
                                double lat = coordinates.getDouble(1);

                                earthQuake.setPlace(properties.getString("place"));
                                earthQuake.setType(properties.getString("type"));
                                earthQuake.setTime(properties.getLong("time"));
                                earthQuake.setMagnitude(properties.getDouble("mag"));
                                earthQuake.setDetailLink(properties.getString("detail"));

                                java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
                                String formattedDate = dateFormat.format(new Date(Long.valueOf(properties.getLong("time")))
                                        .getTime());
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                markerOptions.title(earthQuake.getPlace());
                                markerOptions.position(new LatLng(lat, lon));
//                                markerOptions.snippet("Magnitude: " + earthQuake.getMagnitude() + "\n" +
//                                        "Date: " + formattedDate);

                                Marker marker = mMap.addMarker(markerOptions);
//                                marker.setTag(earthQuake.getDetailLink());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 1));


                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }
}