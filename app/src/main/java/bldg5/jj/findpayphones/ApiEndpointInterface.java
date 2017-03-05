package bldg5.jj.findpayphones;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiEndpointInterface {
    @GET("/")
    Call<List<TCODb>> getOptions(
        @Query("latitude") String latitude,
        @Query("longitude") String longitude
    );

    @GET("/tag")
    Call<String> saveToCloud(
        @Query("latitude") String latitude,
        @Query("longitude") String longitude,
        @Query("userid") String userID,
        @Query("datetagged") String dateTagged,
        @Query("dateuntagged") String dateUntagged,
        @Query("bearing") String bearing,
        @Query("tilt") String tilt,
        @Query("zoom") String zoom
    );

    @GET("/err")
    Call<String> errToCloud(
            @Query("error") String error,
            @Query("datecreated") String datecreated
    );
}
