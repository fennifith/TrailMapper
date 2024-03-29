package james.trailmapper.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import james.trailmapper.R;
import james.trailmapper.TrailMapper;
import james.trailmapper.activities.MapActivity;
import james.trailmapper.data.MapData;
import james.trailmapper.data.PositionData;

public class MapFragment extends SimpleFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String MAP_BUNDLE_KEY = "mapBundleKey";

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.mapView)
    MapView mapView;

    private TrailMapper trailMapper;
    private GoogleMap map;
    private Snackbar snackbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        trailMapper = (TrailMapper) getContext().getApplicationContext();
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, v);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        trailMapper.applySettings(map);
        map.setOnMarkerClickListener(this);
        if (getTrailMapper().getPosition() != null)
            onLocationChanged(getTrailMapper().getPosition());
        else {
            snackbar = Snackbar.make(coordinatorLayout, R.string.msg_getting_location, Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
        onMapsChanged();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onLocationChanged(PositionData position) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position.getLatLng(), 10));
        if (snackbar != null) snackbar.dismiss();
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onMapChanged(MapData map) {
        onMapsChanged();
    }

    @Override
    public void onMapsChanged() {
        if (map != null) {
            for (MapData mapData : getTrailMapper().getMaps()) {
                map.addMarker(new MarkerOptions().position(mapData.getLatLng())).setTag(mapData);
            }
        }
    }

    @Override
    public void onPreferenceChanged() {
        trailMapper.applySettings(map);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getTag() != null && marker.getTag() instanceof MapData) {
            Intent intent = new Intent(getContext(), MapActivity.class);
            intent.putExtra(MapActivity.EXTRA_MAP, (MapData) marker.getTag());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle());
            else startActivity(intent);
        }
        return false;
    }

    @Override
    public void onSelect() {
    }
}
