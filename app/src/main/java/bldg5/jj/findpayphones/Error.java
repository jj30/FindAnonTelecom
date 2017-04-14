package bldg5.jj.findpayphones;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Error {
    @SerializedName("Error")
    @Expose
    private String error;

    @SerializedName("DateCreated")
    @Expose
    private String datecreated;


    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDateCreated() {
        return this.datecreated;
    }

    public void setDatecreated(String datecreated) {
        this.datecreated = datecreated;
    }
}