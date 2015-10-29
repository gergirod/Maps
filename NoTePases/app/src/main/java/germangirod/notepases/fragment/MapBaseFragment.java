package germangirod.notepases.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

/**
 * Created by germangirod on 10/29/15.
 */
public abstract class MapBaseFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    public GoogleMap googleMap;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
