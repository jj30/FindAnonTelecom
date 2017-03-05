package bldg5.jj.findpayphones;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;

public class StreetView extends AppCompatActivity
{
    private static LatLng LocationLatLng;
    private StreetViewPanorama mStreetViewPanorama;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);

        final Button btnSetStreetView = (Button) findViewById(R.id.btnSetStreetView);

        Intent intent = getIntent();
        Bundle getValues = intent.getExtras();

        final String strMode = getValues.getString("mode");
        final Float flBearing = getValues.getFloat("bearing");
        final Float flTilt = getValues.getFloat("tilt");
        final Float flZoom = getValues.getFloat("zoom");
        final Double dblLatitude = getValues.getDouble("latitude");
        final Double dblLongitude = getValues.getDouble("longitude");

        LocationLatLng = new LatLng(dblLatitude, dblLongitude);

        SupportStreetViewPanoramaFragment streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment) getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama);

        streetViewPanoramaFragment.getStreetViewPanoramaAsync(
                new OnStreetViewPanoramaReadyCallback() {
                    @Override
                    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                        mStreetViewPanorama = panorama;

                        // Only set the panorama to SYDNEY on startup (when no panoramas have been
                        // loaded which is when the savedInstanceState is null).
                        if (savedInstanceState == null) {
                            panorama.setPosition(LocationLatLng);
                        }

                        Log.i("FANTEL", "Panorama changed::panorama.getLocation():: " + panorama.getLocation());
                        Log.i("FANTEL", "Panorama changed::panorama.getPanoramaCamera():: " + panorama.getPanoramaCamera());

                        if (!checkReady()) {
                            return;
                        } else {
                            // two modes "show" and "tag"
                            if (strMode.equals("show")) {
                                mStreetViewPanorama.animateTo(
                                        new StreetViewPanoramaCamera.Builder()
                                                .zoom(flZoom)
                                                .tilt(flTilt)
                                                .bearing(flBearing)
                                                .build(), 1000L);

                                btnSetStreetView.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                });

        btnSetStreetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                StreetViewPanoramaCamera currCamera = mStreetViewPanorama.getPanoramaCamera();
                Float bearing = currCamera.bearing;
                Float tilt = currCamera.tilt;
                Float zoom = currCamera.zoom;

                /* Log.i("FANTEL", "bearing: " + String.valueOf(bearing));
                Log.i("FANTEL", "tilt: " + String.valueOf(tilt));
                Log.i("FANTEL", "zoom: " + String.valueOf(zoom));*/

                Intent intent = new Intent();
                intent.putExtra("mode", strMode);
                intent.putExtra("latitude", dblLatitude);
                intent.putExtra("longitude", dblLongitude);
                intent.putExtra("bearing", bearing);
                intent.putExtra("tilt", tilt);
                intent.putExtra("zoom", zoom);

                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private boolean checkReady() {
        if (mStreetViewPanorama == null) {
            Toast.makeText(this, R.string.panorama_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
