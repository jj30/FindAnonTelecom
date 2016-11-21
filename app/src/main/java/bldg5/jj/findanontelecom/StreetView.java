package bldg5.jj.findanontelecom;

import android.content.Intent;
import android.graphics.Point;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.google.android.gms.maps.model.StreetViewPanoramaOrientation;

public class StreetView extends AppCompatActivity
        implements StreetViewPanorama.OnStreetViewPanoramaClickListener,
        StreetViewPanorama.OnStreetViewPanoramaChangeListener,
        StreetViewPanorama.OnStreetViewPanoramaCameraChangeListener,
        StreetViewPanorama.OnStreetViewPanoramaLongClickListener
{

    private static LatLng LocationLatLng;
    private StreetViewPanorama mStreetViewPanorama;

    private TextView mPanoChangeTimesTextView;

    private TextView mPanoCameraChangeTextView;

    private TextView mPanoClickTextView;

    private TextView mPanoLongClickTextView;

    private int mPanoChangeTimes = 0;

    private int mPanoCameraChangeTimes = 0;

    private int mPanoClickTimes = 0;

    private int mPanoLongClickTimes = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);

        mPanoChangeTimesTextView = (TextView) findViewById(R.id.change_pano);
        mPanoCameraChangeTextView = (TextView) findViewById(R.id.change_camera);
        mPanoClickTextView = (TextView) findViewById(R.id.click_pano);
        mPanoLongClickTextView = (TextView) findViewById(R.id.long_click_pano);

        Intent intent = getIntent();
        String strLatitude = intent.getStringExtra("latitude");
        String strLongitude = intent.getStringExtra("longitude");
        Double dblLatitude = 0.0d;
        Double dblLongitude = 0.0d;

        try {
            dblLatitude = Double.parseDouble(strLatitude);
            dblLongitude = Double.parseDouble(strLongitude);
        } catch(ClassCastException exception) {
            Log.e("FANTEL", "string longitude or latitude to double exception:: " + exception.toString());
        } finally {
            LocationLatLng = new LatLng(dblLatitude, dblLongitude);
        }

        SupportStreetViewPanoramaFragment streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment) getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama);

        streetViewPanoramaFragment.getStreetViewPanoramaAsync(
                new OnStreetViewPanoramaReadyCallback() {
                    @Override
                    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                        mStreetViewPanorama = panorama;
                        mStreetViewPanorama.setOnStreetViewPanoramaChangeListener(StreetView.this);
                        mStreetViewPanorama.setOnStreetViewPanoramaCameraChangeListener(StreetView.this);
                        mStreetViewPanorama.setOnStreetViewPanoramaClickListener(StreetView.this);
                        mStreetViewPanorama.setOnStreetViewPanoramaLongClickListener(StreetView.this);

                        // Only set the panorama to SYDNEY on startup (when no panoramas have been
                        // loaded which is when the savedInstanceState is null).
                        if (savedInstanceState == null) {
                            panorama.setPosition(LocationLatLng);
                        }

                        Log.i("FANTEL", "Panorama changed::panorama.getLocation():: " + panorama.getLocation());
                        Log.i("FANTEL", "Panorama changed::panorama.getPanoramaCamera():: " + panorama.getPanoramaCamera());
                    }
                });
    }

    @Override
    public void onStreetViewPanoramaChange(StreetViewPanoramaLocation location) {
        if (location != null) {
            mPanoChangeTimesTextView.setText("Times panorama changed=" + ++mPanoChangeTimes);
        }
    }

    @Override
    public void onStreetViewPanoramaCameraChange(StreetViewPanoramaCamera camera) {
        mPanoCameraChangeTextView.setText("Times camera changed=" + ++mPanoCameraChangeTimes);
    }

    @Override
    public void onStreetViewPanoramaClick(StreetViewPanoramaOrientation orientation) {
        Point point = mStreetViewPanorama.orientationToPoint(orientation);
        if (point != null) {
            mPanoClickTimes++;
            mPanoClickTextView.setText(
                    "Times clicked=" + mPanoClickTimes + " : " + point.toString());
            mStreetViewPanorama.animateTo(
                    new StreetViewPanoramaCamera.Builder()
                            .orientation(orientation)
                            .zoom(mStreetViewPanorama.getPanoramaCamera().zoom)
                            .build(), 1000);
        }

        Log.i("FANTEL", "streetViewPanoramaCamera.bearing:: " + orientation.bearing);
        Log.i("FANTEL", "streetViewPanoramaCamera.getOrientation():: " + orientation.toString());
    }

    @Override
    public void onStreetViewPanoramaLongClick(StreetViewPanoramaOrientation orientation) {
        Point point = mStreetViewPanorama.orientationToPoint(orientation);
        if (point != null) {
            mPanoLongClickTimes++;
            mPanoLongClickTextView.setText(
                    "Times long clicked=" + mPanoLongClickTimes + " : " + point.toString());
        }
    }
}
