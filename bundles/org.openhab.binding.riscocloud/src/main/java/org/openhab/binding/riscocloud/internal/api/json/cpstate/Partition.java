
package org.openhab.binding.riscocloud.internal.api.json.cpstate;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Partition {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("armedState")
    @Expose
    private Integer armedState;
    @SerializedName("readyState")
    @Expose
    private Integer readyState;
    @SerializedName("alarmState")
    @Expose
    private Integer alarmState;
    @SerializedName("groups")
    @Expose
    private List<Group> groups = null;
    @SerializedName("exitDelayTO")
    @Expose
    private Integer exitDelayTO;
    @SerializedName("lastArmFailReasons")
    @Expose
    private Object lastArmFailReasons;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getArmedState() {
        return armedState;
    }

    public void setArmedState(Integer armedState) {
        this.armedState = armedState;
    }

    public Integer getReadyState() {
        return readyState;
    }

    public void setReadyState(Integer readyState) {
        this.readyState = readyState;
    }

    public Integer getAlarmState() {
        return alarmState;
    }

    public void setAlarmState(Integer alarmState) {
        this.alarmState = alarmState;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public Integer getExitDelayTO() {
        return exitDelayTO;
    }

    public void setExitDelayTO(Integer exitDelayTO) {
        this.exitDelayTO = exitDelayTO;
    }

    public Object getLastArmFailReasons() {
        return lastArmFailReasons;
    }

    public void setLastArmFailReasons(Object lastArmFailReasons) {
        this.lastArmFailReasons = lastArmFailReasons;
    }

}
