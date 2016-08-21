package com.example.windows_7.webonize.Activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.windows_7.webonize.Adapters.PhotosRecyclerViewAdapter;
import com.example.windows_7.webonize.Model.Place;
import com.example.windows_7.webonize.Model.PlaceDetails;
import com.example.windows_7.webonize.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by windows-7 on 8/20/2016.
 */
public class SecondActivity extends Activity {

    private GoogleMap googleMap;
    private Place place;
    private String placeName;
    private ListView listView;
    private RecyclerView recyclerView;
    private PhotosRecyclerViewAdapter adapter;
    private double selected_Place_lat,selected_Place_lng,currentLat,currentLng;

    private String url="https://maps.googleapis.com/maps/api/place/details/json?";
    RequestQueue requestQueue;
    Gson gson;
    public void onCreate(Bundle saveInstanceState){

        super.onCreate(saveInstanceState);
        setContentView(R.layout.layout_second);
        /*mImageView=(ImageView)findViewById(R.id.imageview);*/
        /*listView=(ListView)findViewById(R.id.list);*/
        recyclerView=(RecyclerView)findViewById(R.id.recycler);
        adapter=new PhotosRecyclerViewAdapter(getApplicationContext(),bitmaps);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setScrollContainer(true);
        recyclerView.setAdapter(adapter);
        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            place= (Place) bundle.getSerializable("result");
            selected_Place_lat=place.getGeometry().getLocation().getLat();
            selected_Place_lng=place.getGeometry().getLocation().getLng();
            currentLat=bundle.getDouble("currentLat");
            currentLng=bundle.getDouble("currentLng");
            placeName=place.getName();
        }
        try {
            // Loading map
            initilizeMap(selected_Place_lat,selected_Place_lng,placeName,currentLat,currentLng);
            sendRestPlaceDetailsRequest(place);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initilizeMap(double lat,double lng,String placeName,double currentLat,double currentLng) {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            MarkerOptions marker1 = new MarkerOptions().position(new LatLng(lat, lng)).title(placeName);
            marker1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            googleMap.addMarker(marker1);
            googleMap.setMyLocationEnabled(true);
            /*MarkerOptions marker2 = new MarkerOptions().position(new LatLng(currentLat,currentLng)).title("current location");
            marker2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            googleMap.addMarker(marker2);*/
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(lat,lng)).zoom(12).build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap(selected_Place_lat,selected_Place_lng,placeName,currentLat,currentLng);
    }


    public void sendRestPlaceDetailsRequest(Place place){
        requestQueue= Volley.newRequestQueue(getApplicationContext());

        url="https://maps.googleapis.com/maps/api/place/details/json?placeid="+place.getPlace_id()+"&key="+MainActivity.API_KEY;
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response: ", response.toString());

                gson=new Gson();
                placeDetails=gson.fromJson(response.toString(),PlaceDetails.class);
                bitmaps=new ArrayList<>();
                if(placeDetails.getResult()!=null){
                    if(placeDetails.getResult().getPhotos()!=null){
                    for(int i=0;i<placeDetails.getResult().getPhotos().size();i++){
                        sendRestPlaceImageRequest(placeDetails.getResult().getPhotos().get(i).getPhoto_reference(),i);
                    }
                    }
                }else {
                    bitmaps.clear();
                    adapter.updateDataSource(bitmaps);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());
            }
        });
        requestQueue.add(jsonObjectRequest);

    }
    private String status;
    private PlaceDetails placeDetails;
    public void sendRestPlaceImageRequest(String photo_reference, final int position){

        String imageurl="https://maps.googleapis.com/maps/api/place/photo?maxwidth=100"+"&photoreference="+photo_reference+"&key="+MainActivity.API_KEY;


        ImageRequest request = new ImageRequest(imageurl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {

                        bitmaps.add(bitmap);
                        adapter.updateDataSource(bitmaps);
                    }
                }, 100, 100, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(request);
    }

    private List<Bitmap> bitmaps;
}
