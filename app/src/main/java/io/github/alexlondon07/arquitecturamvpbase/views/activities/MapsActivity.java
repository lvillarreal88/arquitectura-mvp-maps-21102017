package io.github.alexlondon07.arquitecturamvpbase.views.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.alexlondon07.arquitecturamvpbase.R;
import io.github.alexlondon07.arquitecturamvpbase.helper.Constants;
import io.github.alexlondon07.arquitecturamvpbase.model.PhoneList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = ":: MAPS ACTIVTY ::";
    private GoogleMap mMap;
    private ArrayList<PhoneList> lstPhones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(checkPlayServices()) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        initComponents();
    }

    private void initComponents() {
        lstPhones = (ArrayList<PhoneList>) getIntent().getSerializableExtra(Constants.ITEM_PHONELIST);
        for (PhoneList phone: lstPhones) {

        }
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
        createMarkers();
        changeStateControls();


    }

    private void createMarkers() {
        ArrayList<LatLng> lstLatLng = new ArrayList<>();
        for (PhoneList phone: lstPhones) {
            LatLng punto = new LatLng(phone.getLocation().getCoordinates()[0], phone.getLocation().getCoordinates()[1]);
            mMap.addMarker(new MarkerOptions().position(punto).title(phone.getDescription()).icon(bitmapDescriptorFromVector(this, R.drawable.ic_room_black_30dp)));
            lstLatLng.add(punto);
        }

        for(int i=0;i<lstLatLng.size();i++){
            if((i+1)!=lstLatLng.size()){
                calculateRoute(lstLatLng.get(i),lstLatLng.get(i+1));
            }
        }


        //LatLng myHomer = new LatLng(6.2819626, -75.5966609);
        //mMap.addMarker(new MarkerOptions().position(myHomer).title("Marker in myHome").icon(bitmapDescriptorFromVector(this, R.drawable.ic_room_black_30dp)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lstLatLng.get(1), 10));

    }

    RoutingListener routingListener = new RoutingListener() {
        @Override
        public void onRoutingFailure(RouteException e) {
            Log.e(TAG, e.getMessage());
        }

        @Override
        public void onRoutingStart() {
            Log.i(TAG, ":: INICIANDO RUTA ::");
        }

        @Override
        public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {
            ArrayList polyLines = new ArrayList<>();
            String [] allcolors= getResources().getStringArray(R.array.colors);
            int valor =0;
            for (int i = 0; i < routes.size(); i++){
                valor = new Random().nextInt(allcolors.length);
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.parseColor(allcolors[valor]));
                polylineOptions.width(5);
                polylineOptions.addAll(routes.get(i).getPoints());

                Polyline polyline = mMap.addPolyline(polylineOptions);
                polyLines.add(polyline);

                //int distance =  routes.get(i).getDistanceValue();
                //int duration = routes.get(i).getDurationValue();
                //Toast.makeText(MapsActivity.this, "distance :" + distance + " - Tiempo: "+ duration, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onRoutingCancelled() {
            Log.w(TAG, "RUTA CANCELADA");
        }
    };

    private void calculateRoute(LatLng myHome, LatLng myOffice) {

        ArrayList<LatLng> points = new ArrayList<>();

        points.add(myHome);
        points.add(myOffice);

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .waypoints(points)
                .key(getString(R.string.google_maps_key))
                .alternativeRoutes(true)
                .withListener(routingListener)
                .build();

        routing.execute();
    }

    private void changeStateControls() {
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorId);
        vectorDrawable.setBounds(0,0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);

        if(result != ConnectionResult.SUCCESS){
            if(googleAPI.isUserResolvableError(result)){
                googleAPI.getErrorDialog(this,result,9000).show();
            }
            return false;
        }
        return true;
    }

}
