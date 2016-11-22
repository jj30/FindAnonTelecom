package bldg5.jj.findanontelecom;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;

// how to do draggable maps like uber
// http://stackoverflow.com/questions/27504606/how-to-implement-draggable-map-like-uber-android-update-with-change-location
// https://www.javacodegeeks.com/2010/09/android-location-based-services.html
public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback {
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
    protected GoogleMap mMap;
    protected LocationManager locationManager;
    protected Double dblLat = 0.0;
    protected Double dblLong = 0.0;
    String mprovider;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    // private String android_id;
    private AdView mAdView;
    private Button btnLocation;
    private Button btnNearest;
    private boolean bPinsDrawn = false;
    private FrameLayout pinSelected;
    private Location pinsDrawn;
    private List<TCODb> allDBOptions;
    private ArrayList<LatLng> allDBOptionsLatLng = new ArrayList<>();
    public RestClient getCloudOptions = new RestClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        btnLocation = (Button) findViewById(R.id.btnLocation);
        btnNearest = (Button) findViewById(R.id.btnNearest);
        pinSelected = (FrameLayout) findViewById(R.id.pinSelected);

        // Show the ad
        AdRequest adRequest = new AdRequest.Builder().build();
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-1882113672777118~7688775386");
        // I/Ads: Use AdRequest.Builder.addTestDevice("CA9D245DFDA0DE28135B9132BCF6089F") to get test ads on this device.
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(adRequest);

        // add the listener to the buttons -- when clicked, get location of pin, or find nearest pin
        addListeners();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }

        pinsDrawn = getLastLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Initializing the map means 1) get last location 2) camera to it
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            InitMap(googleMap);
        }
    }

    private Location getLastLocation() {
        // Get last location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATES,
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                new MyLocationListener()
        );

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        mprovider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        Location location = locationManager.getLastKnownLocation(mprovider);
        return location;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION)
        {
            // Check Permissions Granted or not
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                InitMap(mMap);
            } else {
                Toast.makeText(this, "Access location permission was denied.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void InitMap(GoogleMap googleMap) {
        Location location = getLastLocation();

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        // If we have one and the map is ready, zoom into it.
        if (location != null && mMap != null) {
            dblLat = location.getLatitude();
            dblLong = location.getLongitude();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dblLat, dblLong), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(dblLat, dblLong))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // get the options from the cloud
            getCloudOptions.sqLiteHelper = new FanTelSQLiteHelper(MapsActivity.super.getApplicationContext());
            getCloudOptions.PullDown(dblLat, dblLong);

            // draw the markers from the cloud, now in the local DB (asynchronous)
            DrawMarkers50Miles(location);
        }
    }

    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            // if new location is within a mile of the init'ed location, don't redraw
            float distance = pinsDrawn.distanceTo(location);

            // 1600 meters is roughly one mile
            if (distance > 1600 || !bPinsDrawn) {
                // let's synch the cloud and the local db's now
                // options in db? send to cloud. opts in cloud? pull down.
                // at present time, the app only shows options FOR THE CURRENT LOCATION & FROM THE LOCAL DB

                DrawMarkers50Miles(location);
                pinsDrawn = location;
                bPinsDrawn = true;
            }
        }

        public void onStatusChanged(String s, int i, Bundle b) {
        }

        public void onProviderDisabled(String s) {
        }

        public void onProviderEnabled(String s) {
        }
    }

    private void DrawMarkers50Miles(Location location) {
        dblLat = location.getLatitude();
        dblLong = location.getLongitude();

        final FanTelSQLiteHelper sqLiteHelper = new FanTelSQLiteHelper(MapsActivity.super.getApplicationContext());
        allDBOptions = sqLiteHelper.getAllTCOs(true);

        if (allDBOptions != null) {
            if (allDBOptions.size() > 0) {
                for (final TCODb tcoDb : allDBOptions) {
                    markLocation(tcoDb);
                    mMap.setOnMarkerClickListener(mMarkerListener);
                }

                bPinsDrawn = true;
                pinsDrawn = location;
            }
        }
    }

    private void markLocation(TCODb tcoDb) {
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng latLng = new LatLng(tcoDb.getLatitude(), tcoDb.getLongitude());
        markerOptions.title("Option " + String.valueOf(tcoDb.getOptionsID()));
        markerOptions.position(latLng);

        // Check if it already has been added -- we are only checking by lat long.
        // This means that the list of markers won't have the "Option Title"
        if (allDBOptionsLatLng.contains(latLng)) {
            Log.i("FANTEL", "Attempt to dupe marker: latitude: " + String.valueOf(tcoDb.getLatitude()) +
                    " longitude: " + String.valueOf(tcoDb.getLongitude()) +
                    " OptionID: " + String.valueOf(tcoDb.getOptionsID()));
        } else {
            mMap.addMarker(markerOptions);
            mMap.setOnMarkerClickListener(mMarkerListener);
            allDBOptionsLatLng.add(latLng);
        }
    }


    private GoogleMap.OnMarkerClickListener mMarkerListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker arg0) {
            final FanTelSQLiteHelper sqLiteHelper = new FanTelSQLiteHelper(MapsActivity.super.getApplicationContext());
            final String strTitle = arg0.getTitle();

            if (strTitle.contains("Option")) {
                // if marker source is clicked
                // switch buttons (toggle visibility)
                final boolean bLocationVisible = btnLocation.getVisibility() == View.VISIBLE;
                final boolean bNearestVisible = btnNearest.getVisibility() == View.VISIBLE;
                final boolean bPinSelectedShowing = pinSelected.getVisibility() == View.VISIBLE;

                pinSelected.setVisibility(bPinSelectedShowing ? View.GONE : View.VISIBLE);
                btnLocation.setVisibility(bLocationVisible ? View.GONE : View.VISIBLE);
                btnNearest.setVisibility(bNearestVisible ? View.GONE : View.VISIBLE);

                // newly visible button has to handle event with ID from marker
                final int nID = Integer.valueOf(strTitle.replace("Option ", ""));
                Button buttonUnTag = (Button) findViewById(R.id.btnNoLocation);
                Button buttonCancel = (Button) findViewById(R.id.btnCancel);
                Button btnGetStreetView = (Button) findViewById(R.id.btnGetStreetView);

                buttonUnTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TCODb deleteThis = new TCODb();
                        deleteThis.setOptionsID(nID);
                        sqLiteHelper.deleteTCO(deleteThis);
                        arg0.remove();

                        // toggle visibility again
                        pinSelected.setVisibility(View.GONE);
                        btnLocation.setVisibility(View.VISIBLE);
                        btnNearest.setVisibility(View.VISIBLE);
                    }
                });

                btnGetStreetView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TCODb tcoDb = sqLiteHelper.readTCOByOptionID(nID);

                        Bundle bPassVals = new Bundle();

                        bPassVals.putString("mode", "show");
                        bPassVals.putDouble("latitude", tcoDb.getLatitude());
                        bPassVals.putDouble("longitude", tcoDb.getLongitude());
                        bPassVals.putFloat("bearing", tcoDb.getBearing());
                        bPassVals.putFloat("tilt", tcoDb.getTilt());
                        bPassVals.putFloat("zoom", tcoDb.getZoom());

                        Intent showStView = new Intent(MapsActivity.this, StreetView.class);
                        showStView.putExtras(bPassVals);

                        MapsActivity.this.startActivity(showStView);
                    }
                });

                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // toggle visibility again
                        pinSelected.setVisibility(View.GONE);
                        btnLocation.setVisibility(View.VISIBLE);
                        btnNearest.setVisibility(View.VISIBLE);
                    }
                });
            }
            return true;
        }
    };

    public void addListeners()
    {
        Button btnTag = (Button) findViewById(R.id.btnLocation);
        Button btnNearest = (Button) findViewById(R.id.btnNearest);
        final Context mContext = this;

        btnTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imgV = (ImageView) findViewById(R.id.imageMarker);
                int[] location = new int[2];
                imgV.getLocationOnScreen(location);

                // create a new latLng from the map pin
                Projection projection = mMap.getProjection();
                LatLng pinLoc = projection.fromScreenLocation(new Point(location[0], location[1]));

                // create a new location from the LatLng
                Location lpin = new Location(mprovider);
                lpin.setLatitude(pinLoc.latitude);
                lpin.setLongitude(pinLoc.longitude);

                // first thing's first. You cannot tag a location farther than 100 meters
                float distance = pinsDrawn.distanceTo(lpin);
                boolean bOutOfSight = distance > 100;
                if (bOutOfSight) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setTitle("Invalid Action");
                    dialog.setCancelable(false);
                    dialog.setMessage("You cannot tag or untag an\noption out of sight. (100 meters.)");
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface paramDialogInterface, int id) {
                            return;
                        }
                    });

                    AlertDialog alertDialog = dialog.create();
                    alertDialog.show();

                    Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    pbutton.setBackgroundColor(Color.GRAY);
                    pbutton.setTextColor(Color.WHITE);
                } else {
                    try {
                        // now go tag the street view
                        Intent setStreetView = new Intent(MapsActivity.this, StreetView.class);

                        Bundle bPassVals = new Bundle();
                        bPassVals.putString("mode", "tag");
                        bPassVals.putDouble("latitude", pinLoc.latitude);
                        bPassVals.putDouble("longitude", pinLoc.longitude);

                        setStreetView.putExtras(bPassVals);

                        MapsActivity.this.startActivityForResult(setStreetView, 1);
                    } catch (Exception ex) {
                        Log.i("Fantel", ex.getMessage());
                    }
                }
            }
        });

        btnNearest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allDBOptions == null || allDBOptions.size() == 0) {
                    // there are no options, therefore no 'nearest' one
                    Context context = getApplicationContext();
                    CharSequence text = "There are no nearby options.";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    TCODb nearest = allDBOptions.get(0);
                    Double dblNearestLat = nearest.getLatitude();
                    Double dblNearestLong = nearest.getLongitude();

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dblNearestLat, dblNearestLong), 13));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(dblNearestLat, dblNearestLong))
                            .zoom(17)
                            .build();

                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                String strMode = "";
                Double dblLat = 0.0d;
                Double dblLong = 0.0d;
                Float bearing = 0.0f;
                Float tilt = 0.0f;
                Float zoom = 0.0f;

                strMode = data.getStringExtra("mode");
                dblLat = data.getDoubleExtra("latitude", dblLat);
                dblLong = data.getDoubleExtra("longitude", dblLong);
                bearing = data.getFloatExtra("bearing", bearing);
                tilt = data.getFloatExtra("tilt", tilt);
                zoom = data.getFloatExtra("zoom", zoom);

                FanTelSQLiteHelper sqLiteHelper = new FanTelSQLiteHelper(MapsActivity.super.getApplicationContext());

                // get today's date
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();

                TCODb tcoDb = new TCODb();
                tcoDb.setLatitude(dblLat);
                tcoDb.setLongitude(dblLong);
                tcoDb.setBearing(bearing);
                tcoDb.setTilt(tilt);
                tcoDb.setZoom(zoom);
                tcoDb.setUserID(sqLiteHelper.UserID);
                tcoDb.setDateTagged(dateFormat.format(date));
                sqLiteHelper.createTCODb(tcoDb);

                // now that it's in the DB, put it on the map
                markLocation(tcoDb);
            }
        }
    }
}
