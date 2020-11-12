
package org.openhab.binding.riscocloud.internal.api.json.cpstate;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class State {

    @SerializedName("classVersion")
    @Expose
    private Integer classVersion;
    @SerializedName("isOnline")
    @Expose
    private Boolean isOnline;
    @SerializedName("type")
    @Expose
    private Integer type;
    @SerializedName("media")
    @Expose
    private Integer media;
    @SerializedName("lastConnectedTime")
    @Expose
    private String lastConnectedTime;
    @SerializedName("lastLogUpdate")
    @Expose
    private String lastLogUpdate;
    @SerializedName("lastStatusUpdate")
    @Expose
    private String lastStatusUpdate;
    @SerializedName("lastEvReported")
    @Expose
    private String lastEvReported;
    @SerializedName("features")
    @Expose
    private List<Integer> features = null;
    @SerializedName("status")
    @Expose
    private Status status;
    @SerializedName("cpDescEncName")
    @Expose
    private String cpDescEncName;

    public Integer getClassVersion() {
        return classVersion;
    }

    public void setClassVersion(Integer classVersion) {
        this.classVersion = classVersion;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getMedia() {
        return media;
    }

    public void setMedia(Integer media) {
        this.media = media;
    }

    public String getLastConnectedTime() {
        return lastConnectedTime;
    }

    public void setLastConnectedTime(String lastConnectedTime) {
        this.lastConnectedTime = lastConnectedTime;
    }

    public String getLastLogUpdate() {
        return lastLogUpdate;
    }

    public void setLastLogUpdate(String lastLogUpdate) {
        this.lastLogUpdate = lastLogUpdate;
    }

    public String getLastStatusUpdate() {
        return lastStatusUpdate;
    }

    public void setLastStatusUpdate(String lastStatusUpdate) {
        this.lastStatusUpdate = lastStatusUpdate;
    }

    public String getLastEvReported() {
        return lastEvReported;
    }

    public void setLastEvReported(String lastEvReported) {
        this.lastEvReported = lastEvReported;
    }

    public List<Integer> getFeatures() {
        return features;
    }

    public void setFeatures(List<Integer> features) {
        this.features = features;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCpDescEncName() {
        return cpDescEncName;
    }

    public void setCpDescEncName(String cpDescEncName) {
        this.cpDescEncName = cpDescEncName;
    }

}
