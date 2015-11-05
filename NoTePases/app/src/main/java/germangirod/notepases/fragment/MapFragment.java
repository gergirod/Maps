package germangirod.notepases.fragment;

import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.ButterKnife;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import germangirod.notepases.R;
import germangirod.notepases.data.alarm_service.GeofenseTransitionsIntentService;
import germangirod.notepases.data.model.AlarmPlace;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by germangirod on 10/29/15.
 */
public class MapFragment extends MapBaseFragment implements ResultCallback<Status> {

    private GoogleMap map;
    private ArrayList<Marker> markers = new ArrayList<>();
    private List<Geofence> myFences = new ArrayList<>();
    private PendingIntent mGeofencePendingIntent;
    private Circle circle;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, container, false);

        ButterKnife.inject(this, view);
        setMap();

        return view;
    }

    private void setMap() {

        FragmentManager fragmentManager = getChildFragmentManager();
        SupportMapFragment mapFragment = new SupportMapFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.map, mapFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
        mapFragment.getMapAsync(this);
    }

    public void addGeofencesButtonHandler() {
        if (!googleApiClient.isConnected()) {
            Toast.makeText(getActivity(), "not connect", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(googleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            //logSecurityException(securityException);
        }
    }

    public void removeGeofencesButtonHandler() {
        if (!googleApiClient.isConnected()) {
            Toast.makeText(getActivity(), "not connect", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(googleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            //logSecurityException(securityException);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(myFences);

        // Return a GeofencingRequest.
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(getActivity(), GeofenseTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);
        map.setOnMapClickListener(this);
        map.setMyLocationEnabled(true);
    }

    @Override public void onMapLoaded() {
        if (map.getMyLocation() != null) {
            CameraUpdate cameraUpdate =
                    CameraUpdateFactory.newLatLngZoom(new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude()), 15);
            map.animateCamera(cameraUpdate);
        }
    }

    @Override public void onMapClick(LatLng latLng) {
        if (markers.size() == 0) {
            MarkerOptions markerOptions = new MarkerOptions().title("place").position(latLng).icon(BitmapDescriptorFactory.defaultMarker());
            markers.add(map.addMarker(markerOptions));

            AlarmPlace alarmPlace = new AlarmPlace(latLng.latitude, latLng.longitude, 100, "German Girod");

            addGeofence(alarmPlace);
        } else {
            removeGeofence();
        }
    }

    private void addGeofence(AlarmPlace alarmPlace) {
        addFence(alarmPlace);
        drawCircleRadius(alarmPlace);
        addGeofencesButtonHandler();
    }

    private void removeGeofence() {
        Marker marker = markers.get(0);
        marker.remove();
        markers.clear();
        removeGeofencesButtonHandler();
        circle.remove();
    }

    private void addFence(AlarmPlace place) {
        if (place.getRadius() <= 0) {
            // Nothing to monitor
            return;
        }
        Geofence geofence = new Geofence.Builder().setCircularRegion(place.getLatitude(), place.getLongitude(), 100).setRequestId(
                place.getName()) // every fence must have an ID
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT) // can also have DWELL
                .setExpirationDuration(Geofence.NEVER_EXPIRE) // how long do we care about this geofence?
                        //.setLoiteringDelay(60000) // 1 min.
                .build();
        myFences.add(geofence);
    }

    private void drawCircleRadius(AlarmPlace place) {

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(new LatLng(place.getLatitude(), place.getLongitude()));
        circleOptions.fillColor(Color.argb(0x55, 0x00, 0x00, 0xff));
        circleOptions.strokeColor(Color.argb(0xaa, 0x00, 0x00, 0xff));
        circleOptions.radius(place.radius);

        circle = map.addCircle(circleOptions);
    }

    @Override public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
        }
    }
}


