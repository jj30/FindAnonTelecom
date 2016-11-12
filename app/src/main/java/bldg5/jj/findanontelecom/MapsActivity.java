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
import android.provider.Settings;
import android.renderscript.Double2;
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
import android.widget.LinearLayout;
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
import com.google.android.gms.plus.model.people.Person;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;

// how to do draggable maps like uber
// http://stackoverflow.com/questions/27504606/how-to-implement-draggable-map-like-uber-android-update-with-change-location
// https://www.javacodegeeks.com/2010/09/android-location-based-services.html
public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback
{
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
    protected GoogleMap mMap;
    protected LocationManager locationManager;
    protected Double dblLat = 0.0;
    protected Double dblLong = 0.0;
    String mprovider;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private String android_id;
    private AdView mAdView;
    private Button btnLocation;
    private boolean bPinsDrawn = false;
    private FrameLayout pinSelected;
    private Location pinsDrawn;
    public RestClient getCloudOptions = new RestClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        btnLocation = (Button) findViewById(R.id.btnLocation);
        pinSelected = (FrameLayout) findViewById(R.id.pinSelected);

        // Show the ad
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(android_id)
                .build();

        android_id =  Settings.Secure.getString(this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-1882113672777118~7688775386");
        // I/Ads: Use AdRequest.Builder.addTestDevice("CA9D245DFDA0DE28135B9132BCF6089F") to get test ads on this device.
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(adRequest);

        // add the listener to the button -- when clicked, get location of pin
        addListener();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplanation("Permission Needed", "Rationale", android.Manifest.permission.ACCESS_FINE_LOCATION, MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }

        // Get last location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        mprovider = locationManager.getBestProvider(criteria, false);

        if (mprovider == null) {
            // then the user hasn't yet given permissions
            return;
        }

        Location location = locationManager.getLastKnownLocation(mprovider);
        // save this location as the "init" location
        pinsDrawn = location;

        // If we have one and the map is ready, zoom into it.
        if (location != null && mMap != null) {
            dblLat = location.getLatitude();
            dblLong = location.getLongitude();

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dblLat, dblLong), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(dblLat, dblLong))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    // .bearing(90)                // Sets the orientation of the camera to east
                    // .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATES,
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                new MyLocationListener()
        );
    }

    private void RedrawPins(Location location) {
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

    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            RedrawPins(location);
        }

        public void onStatusChanged(String s, int i, Bundle b) { }
        public void onProviderDisabled(String s) { }
        public void onProviderEnabled(String s) { }
    }

    private void DrawMarkers50Miles(Location location) {
        dblLat = location.getLatitude();
        dblLong = location.getLongitude();

        final FanTelSQLiteHelper sqLiteHelper = new FanTelSQLiteHelper(MapsActivity.super.getApplicationContext());
        List<TCODb> allOptions = sqLiteHelper.getAllTCOs(true);

        if (allOptions != null) {
            for (final TCODb tcoDb : allOptions) {
                markLocation(tcoDb);
                mMap.setOnMarkerClickListener(mMarkerListener);
            }
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
                // boolean bNoLocationVisible = btnNoLocation.getVisibility() == View.VISIBLE;
                final boolean bPinSelectedShowing = pinSelected.getVisibility() == View.VISIBLE;

                pinSelected.setVisibility(bPinSelectedShowing ? View.GONE : View.VISIBLE);
                btnLocation.setVisibility(bLocationVisible ? View.GONE : View.VISIBLE);

                // newly visible button has to handle event with ID from marker
                final int nID = Integer.valueOf(strTitle.replace("Option ", ""));
                Button buttonUnTag = (Button) findViewById(R.id.btnNoLocation);
                Button buttonCancel = (Button) findViewById(R.id.btnCancel);

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
                    }
                });

                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // toggle visibility again
                        pinSelected.setVisibility(View.GONE);
                        btnLocation.setVisibility(View.VISIBLE);
                    }
                });
            }
            return true;
        }
    };

    private void markLocation(TCODb tcoDb) {
        LatLng latLng = new LatLng(tcoDb.getLatitude(), tcoDb.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("Option " + String.valueOf(tcoDb.getOptionsID()));
        markerOptions.position(latLng);
        mMap.addMarker(markerOptions);
        mMap.setOnMarkerClickListener(mMarkerListener);
    }

    // http://stackoverflow.com/questions/35484767/activitycompat-requestpermissions-not-showing-dialog-box
    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
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
        // Get last location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        // , GoogleMap.OnMyLocationButtonClickListener
        // If we don't have permissions, exit ('return')
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ android.Manifest.permission.ACCESS_FINE_LOCATION },
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }

        mprovider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(mprovider);

        // LatLng lastLoc = new LatLng(dblLat, dblLong);

        mMap = googleMap;
        // mMap.setPadding(50, 5, 0, 0); // left top right bottom
        // mMap.addMarker(new MarkerOptions().position(lastLoc).title("Last Location"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLoc));
        mMap.setMyLocationEnabled(true);

        // If we have one and the map is ready, zoom into it.
        if (location != null && mMap != null) {
            dblLat = location.getLatitude();
            dblLong = location.getLongitude();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dblLat, dblLong), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(dblLat, dblLong))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    // .bearing(90)                // Sets the orientation of the camera to east
                    // .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            DrawMarkers50Miles(location);

            // it's refusing synchronous calls.
            // so we have to do this asynchronously.
            getCloudOptions.sqLiteHelper = new FanTelSQLiteHelper(MapsActivity.super.getApplicationContext());
            getCloudOptions.PullDown(dblLat, dblLong);
        }
    }

    public void addListener()
    {
        Button buttonTag = (Button) findViewById(R.id.btnLocation);
        final Context mContext = this;

        buttonTag.setOnClickListener(new View.OnClickListener() {
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
                    /* dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });*/
                    AlertDialog alertDialog = dialog.create();
                    alertDialog.show();

                    Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    pbutton.setBackgroundColor(Color.GRAY);
                    pbutton.setTextColor(Color.WHITE);
                } else {
                    // Projection projection = mMap.getProjection();
                    // LatLng pinLoc = projection.fromScreenLocation(new Point(location[0], location[1]));
                    // Log.i("Fantel", "location x : " + String.valueOf(location[0]) + " location y: " + String.valueOf(location[1]));
                    FanTelSQLiteHelper sqLiteHelper = new FanTelSQLiteHelper(MapsActivity.super.getApplicationContext());

                    // get today's date
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date();

                    TCODb tcoDb = new TCODb();
                    tcoDb.setLatitude(pinLoc.latitude);
                    tcoDb.setLongitude(pinLoc.longitude);
                    tcoDb.setUserID(sqLiteHelper.UserID);
                    tcoDb.setDateTagged(dateFormat.format(date));

                    try {
                        sqLiteHelper.createTCODb(tcoDb);
                        markLocation(tcoDb);

                    } catch (Exception ex) {
                        Log.i("Fantel", ex.getMessage());
                    }
                }
            }
        });
    }
}
