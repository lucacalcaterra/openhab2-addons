
package org.openhab.binding.riscocloud.internal.api.json.cpstate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Response {

    @SerializedName("classVersion")
    @Expose
    private Integer classVersion;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("cpid")
    @Expose
    private String cpid;
    @SerializedName("isAcomEnabled")
    @Expose
    private Boolean isAcomEnabled;
    @SerializedName("state")
    @Expose
    private State state;

    public Integer getClassVersion() {
        return classVersion;
    }

    public void setClassVersion(Integer classVersion) {
        this.classVersion = classVersion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCpid() {
        return cpid;
    }

    public void setCpid(String cpid) {
        this.cpid = cpid;
    }

    public Boolean getIsAcomEnabled() {
        return isAcomEnabled;
    }

    public void setIsAcomEnabled(Boolean isAcomEnabled) {
        this.isAcomEnabled = isAcomEnabled;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

}
