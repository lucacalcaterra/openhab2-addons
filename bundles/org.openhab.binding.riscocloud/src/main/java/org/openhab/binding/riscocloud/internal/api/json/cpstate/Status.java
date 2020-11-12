
package org.openhab.binding.riscocloud.internal.api.json.cpstate;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Status {

    @SerializedName("classVersion")
    @Expose
    private Integer classVersion;
    @SerializedName("partitions")
    @Expose
    private List<Partition> partitions = null;
    @SerializedName("systemStatus")
    @Expose
    private Integer systemStatus;
    @SerializedName("systemReady")
    @Expose
    private Boolean systemReady;
    @SerializedName("trouble")
    @Expose
    private Boolean trouble;
    @SerializedName("bellStatus")
    @Expose
    private Integer bellStatus;
    @SerializedName("alarmPending")
    @Expose
    private Boolean alarmPending;
    @SerializedName("batteryLow")
    @Expose
    private Boolean batteryLow;
    @SerializedName("acLost")
    @Expose
    private Boolean acLost;
    @SerializedName("haEnabled")
    @Expose
    private Boolean haEnabled;
    @SerializedName("bellOn")
    @Expose
    private Boolean bellOn;
    @SerializedName("zones")
    @Expose
    private List<Zone> zones = null;
    @SerializedName("haDevices")
    @Expose
    private Object haDevices;
    @SerializedName("users")
    @Expose
    private List<User> users = null;
    @SerializedName("devCollection")
    @Expose
    private List<DevCollection> devCollection = null;
    @SerializedName("exitDelayTimeout")
    @Expose
    private Integer exitDelayTimeout;
    @SerializedName("armNotAllowed")
    @Expose
    private Boolean armNotAllowed;
    @SerializedName("disarmNotAllowed")
    @Expose
    private Boolean disarmNotAllowed;
    @SerializedName("cpTime")
    @Expose
    private String cpTime;
    @SerializedName("partReadySupported")
    @Expose
    private Boolean partReadySupported;
    @SerializedName("partFullReady")
    @Expose
    private Boolean partFullReady;
    @SerializedName("part1Ready")
    @Expose
    private Boolean part1Ready;
    @SerializedName("part2Ready")
    @Expose
    private Boolean part2Ready;
    @SerializedName("type")
    @Expose
    private Integer type;

    public Integer getClassVersion() {
        return classVersion;
    }

    public void setClassVersion(Integer classVersion) {
        this.classVersion = classVersion;
    }

    public List<Partition> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<Partition> partitions) {
        this.partitions = partitions;
    }

    public Integer getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(Integer systemStatus) {
        this.systemStatus = systemStatus;
    }

    public Boolean getSystemReady() {
        return systemReady;
    }

    public void setSystemReady(Boolean systemReady) {
        this.systemReady = systemReady;
    }

    public Boolean getTrouble() {
        return trouble;
    }

    public void setTrouble(Boolean trouble) {
        this.trouble = trouble;
    }

    public Integer getBellStatus() {
        return bellStatus;
    }

    public void setBellStatus(Integer bellStatus) {
        this.bellStatus = bellStatus;
    }

    public Boolean getAlarmPending() {
        return alarmPending;
    }

    public void setAlarmPending(Boolean alarmPending) {
        this.alarmPending = alarmPending;
    }

    public Boolean getBatteryLow() {
        return batteryLow;
    }

    public void setBatteryLow(Boolean batteryLow) {
        this.batteryLow = batteryLow;
    }

    public Boolean getAcLost() {
        return acLost;
    }

    public void setAcLost(Boolean acLost) {
        this.acLost = acLost;
    }

    public Boolean getHaEnabled() {
        return haEnabled;
    }

    public void setHaEnabled(Boolean haEnabled) {
        this.haEnabled = haEnabled;
    }

    public Boolean getBellOn() {
        return bellOn;
    }

    public void setBellOn(Boolean bellOn) {
        this.bellOn = bellOn;
    }

    public List<Zone> getZones() {
        return zones;
    }

    public void setZones(List<Zone> zones) {
        this.zones = zones;
    }

    public Object getHaDevices() {
        return haDevices;
    }

    public void setHaDevices(Object haDevices) {
        this.haDevices = haDevices;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<DevCollection> getDevCollection() {
        return devCollection;
    }

    public void setDevCollection(List<DevCollection> devCollection) {
        this.devCollection = devCollection;
    }

    public Integer getExitDelayTimeout() {
        return exitDelayTimeout;
    }

    public void setExitDelayTimeout(Integer exitDelayTimeout) {
        this.exitDelayTimeout = exitDelayTimeout;
    }

    public Boolean getArmNotAllowed() {
        return armNotAllowed;
    }

    public void setArmNotAllowed(Boolean armNotAllowed) {
        this.armNotAllowed = armNotAllowed;
    }

    public Boolean getDisarmNotAllowed() {
        return disarmNotAllowed;
    }

    public void setDisarmNotAllowed(Boolean disarmNotAllowed) {
        this.disarmNotAllowed = disarmNotAllowed;
    }

    public String getCpTime() {
        return cpTime;
    }

    public void setCpTime(String cpTime) {
        this.cpTime = cpTime;
    }

    public Boolean getPartReadySupported() {
        return partReadySupported;
    }

    public void setPartReadySupported(Boolean partReadySupported) {
        this.partReadySupported = partReadySupported;
    }

    public Boolean getPartFullReady() {
        return partFullReady;
    }

    public void setPartFullReady(Boolean partFullReady) {
        this.partFullReady = partFullReady;
    }

    public Boolean getPart1Ready() {
        return part1Ready;
    }

    public void setPart1Ready(Boolean part1Ready) {
        this.part1Ready = part1Ready;
    }

    public Boolean getPart2Ready() {
        return part2Ready;
    }

    public void setPart2Ready(Boolean part2Ready) {
        this.part2Ready = part2Ready;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

}
