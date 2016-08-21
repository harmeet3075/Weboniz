package com.example.windows_7.webonize.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.windows_7.webonize.Fragments.PlaceListFragment;
import com.example.windows_7.webonize.Model.Place;
import com.example.windows_7.webonize.Model.PlaceList;
import com.example.windows_7.webonize.Networking.Connectivity;
import com.example.windows_7.webonize.Networking.CurrentLocation;
import com.example.windows_7.webonize.R;
import com.example.windows_7.webonize.Utils.KeyboardUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PlaceListFragment.OnFragmentInteractionListener{

    EditText searchText;
    Button searchButton;
    CurrentLocation mCurrentLocation;
    Location mLocation;
    double lat,lng;
    RequestQueue requestQueue;
    PlaceListFragment placeListFragment;
    Gson gson;
    PlaceList placeList;
    public static final String API_KEY = "AIzaSyD7KTtcY7KFlGgkZnwRJgkNtuNSa32Jc-A";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchText=(EditText)findViewById(R.id.searchbar);
        searchButton=(Button)findViewById(R.id.searchButton);
        placeListFragment=(PlaceListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardUtils.tryToHideKeyboard(MainActivity.this);
                if(!Connectivity.isNetworkAvailable(getApplicationContext())){
                    Toast.makeText(getApplicationContext(),"Not connected",Toast.LENGTH_SHORT).show();
                }else
                {
                    mCurrentLocation=new CurrentLocation(getApplicationContext());

                    if(mCurrentLocation.canGetLocation()){
                        lat=mCurrentLocation.getLatitude();
                        lng=mCurrentLocation.getLongitude();
                        requestQueue= Volley.newRequestQueue(getApplicationContext());

                        String url="https://maps.googleapis.com/maps/api/place/search/json?location=" + lat + "," + lng + "&radius=5000&sensor=true&key=" +API_KEY+"&type="+searchText.getText().toString();

                        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url,null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("Response: ",response.toString());
                                gson=new Gson();
                                placeList=gson.fromJson(response.toString(),PlaceList.class);
                                Log.d("placeList",placeList.getResults().toString());
                                notifyPlaceListDataReceived();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("error",error.toString());
                            }
                        });
                        requestQueue.add(jsonObjectRequest);
                    }else
                        showSettingsAlert();
                }
            }
        });
    }


    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    public List<Place> getPlaceList() {
        if(placeList!=null)
        return placeList.getResults();
        else
            return null;
    }


    private void notifyPlaceListDataReceived(){
        if (placeListFragment != null) {
            placeListFragment.showPlacesList(lat,lng);
        }
    }
}
