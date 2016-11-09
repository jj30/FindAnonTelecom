package bldg5.jj.findanontelecom;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiEndpointInterface {
    @GET("users/{username}")
    Call<TCOption> getUser(@Path("username") String username);

    @GET("group/{id}/users")
    Call<List<TCOption>> groupList(@Path("id") int groupId, @Query("sort") String sort);

    @POST("users/new")
    Call<TCOption> createUser(@Body TCOption user);
}
