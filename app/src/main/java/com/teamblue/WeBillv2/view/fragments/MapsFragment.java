package com.teamblue.WeBillv2.view.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.teamblue.WeBillv2.R;
import com.teamblue.WeBillv2.model.pojo.LocationItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MapsFragment extends Fragment {

    private int ZOOM = 15;  // ranges from 2 to 21; the higher the num, the more zoomed in
    private ClusterManager<LocationItem> clusterManager;

    // 4. the map callback
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            // 5. when the map is ready

            // set the map coordinates to Boston
            LatLng boston = new LatLng(42.360081, -71.058884);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);    // set to basic map

            // move cam to map coordinates and zoom in
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(boston, ZOOM));

            // initialize the cluster manager with the context and the map
            clusterManager = new ClusterManager<LocationItem>(getContext(), googleMap);

            // point the map's listeners at the listeners implemented by the cluster manager
            googleMap.setOnCameraIdleListener(clusterManager);
            googleMap.setOnMarkerClickListener(clusterManager);

            // read location data and add to map
            try {
                List<LocationItem> locationItems = readItems(R.raw.locations);
                clusterManager.addItems(locationItems);
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error reading list of locations", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 1. initialize the view
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2. initialize the map fragment
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        // 3. as long as mapFragment isn't null then call getMapAsync
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private List<LocationItem> readItems(@RawRes int resource) throws JSONException {
        List<LocationItem> result = new ArrayList<>();
        InputStream inputStream = getContext().getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            String name = object.getString("name");
            LocationItem newItem = new LocationItem(lat, lng, name, null);
            result.add(newItem);
        }
        return result;
    }

}