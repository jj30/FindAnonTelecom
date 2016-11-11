package bldg5.jj.findanontelecom;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class TCODb {
    @SerializedName("Latitude")
    @Expose
    private Double latitude;
    @SerializedName("UserID")
    @Expose
    private String userID;
    @SerializedName("DateUntagged")
    @Expose
    private String dateUntagged;
    @SerializedName("DateTagged")
    @Expose
    private String dateTagged;
    @SerializedName("OptionsID")
    @Expose
    private Integer optionsID;
    @SerializedName("GlobalID")
    @Expose
    private String globaID;
    @SerializedName("distance")
    @Expose
    private Double distance;
    @SerializedName("Longitude")
    @Expose
    private Double longitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getGlobalID() { return globaID; }

    public void setGlobalID(String globalID) {
        this.globaID = globalID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDateUntagged() {
        return dateUntagged;
    }

    public void setDateUntagged(String dateUntagged) {
        this.dateUntagged = dateUntagged;
    }

    public String getDateTagged() {
        return dateTagged;
    }

    public void setDateTagged(String dateTagged) {
        this.dateTagged = dateTagged;
    }

    public Integer getOptionsID() {
        return optionsID;
    }

    public void setOptionsID(Integer optionsID) {
        this.optionsID = optionsID;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}



