package germangirod.notepases.fragment;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import germangirod.notepases.R;

/**
 * Created by germangirod on 10/29/15.
 */
public class MapFragment extends MapBaseFragment {

    private GoogleMap map;

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

    @Override public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);
        map.setOnMapLongClickListener(this);
        map.setMyLocationEnabled(true);

    }

    @Override public void onMapLoaded() {
        if(map.getMyLocation()!=null){
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude()), 15);
            map.animateCamera(cameraUpdate);
        }
    }
}
