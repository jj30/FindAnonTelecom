package bldg5.jj.findanontelecom;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InitRequestResponse {
    private static String TAG = "InitRequestResponse.java in Sync.";

    @SerializedName("action")
    @Expose
    private String action;

    @SerializedName("payloadType")
    @Expose
    private String payloadType;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    @Override
    public String toString() {
        return "{" +
                "'action': 'create'," +
                "'payloadType':'" + this.payloadType + "'" +
                "}";
    }
}