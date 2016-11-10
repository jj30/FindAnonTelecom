package bldg5.jj.findanontelecom;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiEndpointInterface {
    @GET("/")
    Call<List<TCODb>> getOptions();
}
