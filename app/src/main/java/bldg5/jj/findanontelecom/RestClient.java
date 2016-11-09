package bldg5.jj.findanontelecom;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public class RestClient {

    private static String BASE_URL = "http://192.168.7.151:8080/";
    private ApiEndpointInterface postResponse;

    public RestClient()
    {
        OkHttpClient okHttpClient = new OkHttpClient();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(TCOption.class, new TCOAdapter());
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        postResponse = retrofit.create(ApiEndpointInterface.class);
    }

    public ApiEndpointInterface getPostResponse()
    {
        try
        {
            return postResponse;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /*public void Execute(Context _context) {
    // OkClient okClient = new OkClient(getOkClient(_context));

        //RestAdapter builder = new RestAdapter.Builder()
          //      .setEndpoint(strURL)
            //    .setLogLevel(RestAdapter.LogLevel.FULL)
              //  .setClient(okClient)
                //.build();

    RestAdapter builder = createAdapter(_context);

        try {

            ApiEndpointInterface lolwut = builder.create(ApiEndpointInterface.class);
            Callback<List<Location>> whata = new Callback<List<Location>>() {
                @Override
                public void success (List<Location> contributors, Response response){
                    // got the list of contributors
                    Log.i("", "TRUE");
                    //  System.console().writer().write("WHAT");
                };
                @Override
                public void failure(RetrofitError error) {
                    Log.i("", "FALSE");
                    // Code for when something went wrong
                    //  System.console().writer().write("WHAT");
                }
            };
            lolwut.getLocations("HOSP", whata);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
