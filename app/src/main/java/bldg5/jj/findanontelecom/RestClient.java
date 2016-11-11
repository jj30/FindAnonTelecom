package bldg5.jj.findanontelecom;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {

    private static String BASE_URL = "http://ec2-54-162-115-46.compute-1.amazonaws.com:8080/";
    public List<TCODb> allCloudOptions;
    public FanTelSQLiteHelper sqLiteHelper;

    public void PullDown(double latitude, double longitude)
    {
        String strLatitude = String.valueOf(latitude);
        String strLongitude = String.valueOf(longitude);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
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
                }
            }

            @Override
            public void onFailure(Call<List<TCODb>> call, Throwable t) {
                Log.e("Fantel", t.toString());
            }
        });
    }

    public void SendToCloud(TCODb tcoDb) {
        String strLatitude = String.valueOf(tcoDb.getLatitude());
        String strLongitude = String.valueOf(tcoDb.getLongitude());
        String strUserID = tcoDb.getUserID();
        String strDateTagged = tcoDb.getDateTagged();
        String strDateUntagged = tcoDb.getDateUntagged();

        if (strDateUntagged == null) {
            strDateUntagged = "";
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiEndpointInterface service = retrofit.create(ApiEndpointInterface.class);
        Call<Void> call = service.saveToCloud(strLatitude, strLongitude, strUserID, strDateTagged, strDateUntagged);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                boolean bSuccess = response.isSuccessful();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Fantel", t.toString());
            }
        });
    }

    private void synch() {
        List<TCODb> localDBOptions = sqLiteHelper.getAllTCOs();

        // save cloud option to local db
        for (TCODb tc : allCloudOptions) {
            if (!localDBOptions.contains(tc)) {
                sqLiteHelper.createTCODb(tc);
            }
        }

        // save local db option to cloud
        for (TCODb tc : localDBOptions) {
            if (!allCloudOptions.contains(tc)) {
                SendToCloud(tc);
            }
        }
    }
}
