package bldg5.jj.findanontelecom;

import android.content.Intent;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

public class StreetView extends AppCompatActivity {

    private static LatLng LocationLatLng;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);

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

}
