package bldg5.jj.findpayphones;

import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {

    private static String BASE_URL = "http://payphone.chickenkiller.com:8080/";
    public List<TCODb> allCloudOptions;
    public FanTelSQLiteHelper sqLiteHelper;
    public String strResponse;

    public void pullDown(double latitude, double longitude, final android.widget.ProgressBar pg)
    {
        String strLatitude = String.valueOf(latitude);
        String strLongitude = String.valueOf(longitude);

        // Client With Time Out
        OkHttpClient clientWTO = new OkHttpClient
                .Builder()
                .readTimeout(5, TimeUnit.MINUTES)
                .connectTimeout(5, TimeUnit.MINUTES)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientWTO)
                .build();

        ApiEndpointInterface service = retrofit.create(ApiEndpointInterface.class);
        Call<List<TCODb>> call = service.getOptions(strLatitude, strLongitude);

        call.enqueue(new Callback<List<TCODb>>() {
            @Override
            public void onResponse(Call<List<TCODb>> call, Response<List<TCODb>> response) {
                boolean bSuccess = response.isSuccessful();

                if (bSuccess) {
                    allCloudOptions = response.body();
                    synch();

                    pg.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<TCODb>> call, Throwable t) {
                Log.e("FANTEL", t.toString());
                sqLiteHelper.logError(t.getMessage());

                pg.setVisibility(View.GONE);
            }
        });
    }

    public void saveToCloud(TCODb tcoDb) {
        // String strLocalOptionID = String.valueOf(tcoDb.getGlobalID());
        String strLatitude = String.valueOf(tcoDb.getLatitude());
        String strLongitude = String.valueOf(tcoDb.getLongitude());
        String strUserID = tcoDb.getUserID();
        String strDateTagged = tcoDb.getDateTagged();
        String strDateUntagged = tcoDb.getDateUntagged();
        String strBearing = String.valueOf(tcoDb.getBearing());
        String strTilt = String.valueOf(tcoDb.getTilt());
        String strZoom = String.valueOf(tcoDb.getZoom());

        if (strDateUntagged == null) {
            strDateUntagged = "";
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiEndpointInterface service = retrofit.create(ApiEndpointInterface.class);
        Call<String> call = service.saveToCloud(strLatitude, strLongitude, strUserID, strDateTagged, strDateUntagged, strBearing, strTilt, strZoom);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                boolean bSuccess = response.isSuccessful();

                if (bSuccess) {
                    strResponse = response.body();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                sqLiteHelper.logError(t.toString());
            }
        });
    }

    public void errorToCloud(Error error) {
        String strError = String.valueOf(error.getError());
        String strDateTagged = error.getDateCreated();

        if (strDateTagged == null) {
            strDateTagged = "";
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiEndpointInterface service = retrofit.create(ApiEndpointInterface.class);
        Call<String> call = service.errToCloud(strError, strDateTagged);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                boolean bSuccess = response.isSuccessful();

                if (bSuccess) {
                    strResponse = response.body();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    private void synch() {
        List<TCODb> localDBOptions = sqLiteHelper.getAllTCOs(false);

        try {
            // save cloud option to local db
            for (TCODb tc : allCloudOptions) {
                if (!localDBOptions.contains(tc)) {
                    sqLiteHelper.createTCODb(tc);
                }
            }
        } catch (Exception ex) {
            sqLiteHelper.logError(ex.getMessage());
        }

        try {
            // save local db option to cloud
            for (TCODb tc : localDBOptions) {
                // kluge alert: we should send only deltas to the cloud.
                saveToCloud(tc);

                // if it was never set, it never came from the cloud
                if (tc.getGlobalID() == null) {
                    sqLiteHelper.obliterateTCO(tc);
                }
            }
        } catch (Exception ex) {
            sqLiteHelper.logError(ex.getMessage());
        }
    }

    public void synch_errors() {
        List<Error> localErrors = sqLiteHelper.getAllErrors();

        try {
            // save local db err to cloud
            for (Error err : localErrors) {
                errorToCloud(err);
            }

            sqLiteHelper.deleteAllErrors();
        } catch (Exception ex) { }
    }
}
