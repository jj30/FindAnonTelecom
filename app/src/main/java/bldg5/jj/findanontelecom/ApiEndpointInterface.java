package bldg5.jj.findanontelecom;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiEndpointInterface {
    @GET("/")
    Call<List<TCODb>> getOptions(
        @Query("latitude") String latitude,
        @Query("longitude") String longitude
    );

    @GET("/tag")
    Call<Void> saveToCloud(
        @Query("latitude") String latitude,
        @Query("longitude") String longitude,
        @Query("userid") String userID,
        @Query("datetagged") String dateTagged,
        @Query("dateuntagged") String dateUntagged
    );
}
