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

    private static String BASE_URL = "http://ec2-174-129-89-160.compute-1.amazonaws.com:8080/";
    public List<TCODb> allCloudOptions;

    public RestClient(String latitude, String longitude)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiEndpointInterface service = retrofit.create(ApiEndpointInterface.class);
        Call<List<TCODb>> call = service.getOptions(latitude, longitude);

        call.enqueue(new Callback<List<TCODb>>() {
            @Override
            public void onResponse(Call<List<TCODb>> call, Response<List<TCODb>> response) {
                boolean bSuccess = response.isSuccessful();

                if (bSuccess) {
                    allCloudOptions = response.body();
                }
            }

            @Override
            public void onFailure(Call<List<TCODb>> call, Throwable t) {
                Log.e("Fantel", t.toString());
            }
        });
    }
}
