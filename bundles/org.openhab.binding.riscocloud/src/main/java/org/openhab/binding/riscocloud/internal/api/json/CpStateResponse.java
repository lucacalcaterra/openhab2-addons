package org.openhab.binding.riscocloud.internal.api.json;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CpStateResponse {
    public class DevCollection {

        @SerializedName("devList")
        @Expose
        private List<DevList> devList = null;
        @SerializedName("devType")
        @Expose
        private Integer devType;

        public List<DevList> getDevList() {
            return devList;
        }

        public void setDevList(List<DevList> devList) {
            this.devList = devList;
        }

        public Integer getDevType() {
            return devType;
        }

        public void setDevType(Integer devType) {
            this.devType = devType;
        }
    }

    public class DevList {

        @SerializedName("desc")
        @Expose
        private String desc;
        @SerializedName("extra")
        @Expose
        private Object extra;
        @SerializedName("num")
        @Expose
        private Integer num;

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public Object getExtra() {
            return extra;
        }

        public void setExtra(Object extra) {
            this.extra = extra;
        }

        public Integer getNum() {
            return num;
        }

        public void setNum(Integer num) {
            this.num = num;
        }
    }

    public class Group {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("state")
        @Expose
        private Integer state;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getState() {
            return state;
        }

        public void setState(Integer state) {
            this.state = state;
        }
    }

    public class Partition {

        @SerializedName("alarmState")
        @Expose
        private Integer alarmState;
        @SerializedName("armedState")
        @Expose
        private Integer armedState;
        @SerializedName("exitDelayTO")
        @Expose
        private Integer exitDelayTO;
        @SerializedName("groups")
        @Expose
        private List<Group> groups = null;
        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("lastArmFailReasons")
        @Expose
        private Object lastArmFailReasons;
        @SerializedName("readyState")
        @Expose
        private Integer readyState;

        public Integer getAlarmState() {
            return alarmState;
        }

        public void setAlarmState(Integer alarmState) {
            this.alarmState = alarmState;
        }

        public Integer getArmedState() {
            return armedState;
        }

        public void setArmedState(Integer armedState) {
            this.armedState = armedState;
        }

        public Integer getExitDelayTO() {
            return exitDelayTO;
        }

        public void setExitDelayTO(Integer exitDelayTO) {
            this.exitDelayTO = exitDelayTO;
        }

        public List<Group> getGroups() {
            return groups;
        }

        public void setGroups(List<Group> groups) {
            this.groups = groups;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Object getLastArmFailReasons() {
            return lastArmFailReasons;
        }

        public void setLastArmFailReasons(Object lastArmFailReasons) {
            this.lastArmFailReasons = lastArmFailReasons;
        }

        public Integer getReadyState() {
            return readyState;
        }

        public void setReadyState(Integer readyState) {
            this.readyState = readyState;
        }
    }

    public class Response {

        @SerializedName("cpid")
        @Expose
        private String cpid;
        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("isAcomEnabled")
        @Expose
        private Boolean isAcomEnabled;
        @SerializedName("state")
        @Expose
        private State state;

        public String getCpid() {
            return cpid;
        }

        public void setCpid(String cpid) {
            this.cpid = cpid;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
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

    public class State {

        @SerializedName("cpDescEncName")
        @Expose
        private String cpDescEncName;
        @SerializedName("features")
        @Expose
        private List<Integer> features = null;
        @SerializedName("isOnline")
        @Expose
        private Boolean isOnline;
        @SerializedName("lastConnectedTime")
        @Expose
        private String lastConnectedTime;
        @SerializedName("lastEvReported")
        @Expose
        private String lastEvReported;
        @SerializedName("lastLogUpdate")
        @Expose
        private String lastLogUpdate;
        @SerializedName("lastStatusUpdate")
        @Expose
        private String lastStatusUpdate;
        @SerializedName("media")
        @Expose
        private Integer media;
        @SerializedName("status")
        @Expose
        private Status status;
        @SerializedName("type")
        @Expose
        private Integer type;

        public String getCpDescEncName() {
            return cpDescEncName;
        }

        public void setCpDescEncName(String cpDescEncName) {
            this.cpDescEncName = cpDescEncName;
        }

        public List<Integer> getFeatures() {
            return features;
        }

        public void setFeatures(List<Integer> features) {
            this.features = features;
        }

        public Boolean getIsOnline() {
            return isOnline;
        }

        public void setIsOnline(Boolean isOnline) {
            this.isOnline = isOnline;
        }

        public String getLastConnectedTime() {
            return lastConnectedTime;
        }

        public void setLastConnectedTime(String lastConnectedTime) {
            this.lastConnectedTime = lastConnectedTime;
        }

        public String getLastEvReported() {
            return lastEvReported;
        }

        public void setLastEvReported(String lastEvReported) {
            this.lastEvReported = lastEvReported;
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

        public Integer getMedia() {
            return media;
        }

        public void setMedia(Integer media) {
            this.media = media;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }
    }

    public class StateResponse {

        @SerializedName("errorText")
        @Expose
        private Object errorText;
        @SerializedName("errorTextCodeID")
        @Expose
        private String errorTextCodeID;
        @SerializedName("response")
        @Expose
        private Response response;
        @SerializedName("result")
        @Expose
        private Integer result;
        @SerializedName("status")
        @Expose
        private Integer status;
        @SerializedName("validationErrors")
        @Expose
        private Object validationErrors;

        public Object getErrorText() {
            return errorText;
        }

        public void setErrorText(Object errorText) {
            this.errorText = errorText;
        }

        public String getErrorTextCodeID() {
            return errorTextCodeID;
        }

        public void setErrorTextCodeID(String errorTextCodeID) {
            this.errorTextCodeID = errorTextCodeID;
        }

        public Response getResponse() {
            return response;
        }

        public void setResponse(Response response) {
            this.response = response;
        }

        public Integer getResult() {
            return result;
        }

        public void setResult(Integer result) {
            this.result = result;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public Object getValidationErrors() {
            return validationErrors;
        }

        public void setValidationErrors(Object validationErrors) {
            this.validationErrors = validationErrors;
        }
    }

    public class Status {

        @SerializedName("acLost")
        @Expose
        private Boolean acLost;
        @SerializedName("alarmPending")
        @Expose
        private Boolean alarmPending;
        @SerializedName("armNotAllowed")
        @Expose
        private Boolean armNotAllowed;
        @SerializedName("batteryLow")
        @Expose
        private Boolean batteryLow;
        @SerializedName("bellOn")
        @Expose
        private Boolean bellOn;
        @SerializedName("bellStatus")
        @Expose
        private Integer bellStatus;
        @SerializedName("cpTime")
        @Expose
        private String cpTime;
        @SerializedName("devCollection")
        @Expose
        private List<DevCollection> devCollection = null;
        @SerializedName("disarmNotAllowed")
        @Expose
        private Boolean disarmNotAllowed;
        @SerializedName("exitDelayTimeout")
        @Expose
        private Integer exitDelayTimeout;
        @SerializedName("haDevices")
        @Expose
        private Object haDevices;
        @SerializedName("haEnabled")
        @Expose
        private Boolean haEnabled;
        @SerializedName("part1Ready")
        @Expose
        private Boolean part1Ready;
        @SerializedName("part2Ready")
        @Expose
        private Boolean part2Ready;
        @SerializedName("partFullReady")
        @Expose
        private Boolean partFullReady;
        @SerializedName("partReadySupported")
        @Expose
        private Boolean partReadySupported;
        @SerializedName("partitions")
        @Expose
        private List<Partition> partitions = null;
        @SerializedName("systemReady")
        @Expose
        private Boolean systemReady;
        @SerializedName("systemStatus")
        @Expose
        private Integer systemStatus;
        @SerializedName("trouble")
        @Expose
        private Boolean trouble;
        @SerializedName("type")
        @Expose
        private Integer type;
        @SerializedName("users")
        @Expose
        private List<User> users = null;
        @SerializedName("zones")
        @Expose
        private List<Zone> zones = null;

        public Boolean getAcLost() {
            return acLost;
        }

        public void setAcLost(Boolean acLost) {
            this.acLost = acLost;
        }

        public Boolean getAlarmPending() {
            return alarmPending;
        }

        public void setAlarmPending(Boolean alarmPending) {
            this.alarmPending = alarmPending;
        }

        public Boolean getArmNotAllowed() {
            return armNotAllowed;
        }

        public void setArmNotAllowed(Boolean armNotAllowed) {
            this.armNotAllowed = armNotAllowed;
        }

        public Boolean getBatteryLow() {
            return batteryLow;
        }

        public void setBatteryLow(Boolean batteryLow) {
            this.batteryLow = batteryLow;
        }

        public Boolean getBellOn() {
            return bellOn;
        }

        public void setBellOn(Boolean bellOn) {
            this.bellOn = bellOn;
        }

        public Integer getBellStatus() {
            return bellStatus;
        }

        public void setBellStatus(Integer bellStatus) {
            this.bellStatus = bellStatus;
        }

        public String getCpTime() {
            return cpTime;
        }

        public void setCpTime(String cpTime) {
            this.cpTime = cpTime;
        }

        public List<DevCollection> getDevCollection() {
            return devCollection;
        }

        public void setDevCollection(List<DevCollection> devCollection) {
            this.devCollection = devCollection;
        }

        public Boolean getDisarmNotAllowed() {
            return disarmNotAllowed;
        }

        public void setDisarmNotAllowed(Boolean disarmNotAllowed) {
            this.disarmNotAllowed = disarmNotAllowed;
        }

        public Integer getExitDelayTimeout() {
            return exitDelayTimeout;
        }

        public void setExitDelayTimeout(Integer exitDelayTimeout) {
            this.exitDelayTimeout = exitDelayTimeout;
        }

        public Object getHaDevices() {
            return haDevices;
        }

        public void setHaDevices(Object haDevices) {
            this.haDevices = haDevices;
        }

        public Boolean getHaEnabled() {
            return haEnabled;
        }

        public void setHaEnabled(Boolean haEnabled) {
            this.haEnabled = haEnabled;
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

        public Boolean getPartFullReady() {
            return partFullReady;
        }

        public void setPartFullReady(Boolean partFullReady) {
            this.partFullReady = partFullReady;
        }

        public Boolean getPartReadySupported() {
            return partReadySupported;
        }

        public void setPartReadySupported(Boolean partReadySupported) {
            this.partReadySupported = partReadySupported;
        }

        public List<Partition> getPartitions() {
            return partitions;
        }

        public void setPartitions(List<Partition> partitions) {
            this.partitions = partitions;
        }

        public Boolean getSystemReady() {
            return systemReady;
        }

        public void setSystemReady(Boolean systemReady) {
            this.systemReady = systemReady;
        }

        public Integer getSystemStatus() {
            return systemStatus;
        }

        public void setSystemStatus(Integer systemStatus) {
            this.systemStatus = systemStatus;
        }

        public Boolean getTrouble() {
            return trouble;
        }

        public void setTrouble(Boolean trouble) {
            this.trouble = trouble;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public List<User> getUsers() {
            return users;
        }

        public void setUsers(List<User> users) {
            this.users = users;
        }

        public List<Zone> getZones() {
            return zones;
        }

        public void setZones(List<Zone> zones) {
            this.zones = zones;
        }
    }

    public class User {

        @SerializedName("part")
        @Expose
        private Integer part;
        @SerializedName("partAssocMask")
        @Expose
        private Object partAssocMask;
        @SerializedName("passCode")
        @Expose
        private String passCode;
        @SerializedName("userID")
        @Expose
        private Integer userID;
        @SerializedName("userName")
        @Expose
        private Object userName;
        @SerializedName("userType")
        @Expose
        private Integer userType;

        public Integer getPart() {
            return part;
        }

        public void setPart(Integer part) {
            this.part = part;
        }

        public Object getPartAssocMask() {
            return partAssocMask;
        }

        public void setPartAssocMask(Object partAssocMask) {
            this.partAssocMask = partAssocMask;
        }

        public String getPassCode() {
            return passCode;
        }

        public void setPassCode(String passCode) {
            this.passCode = passCode;
        }

        public Integer getUserID() {
            return userID;
        }

        public void setUserID(Integer userID) {
            this.userID = userID;
        }

        public Object getUserName() {
            return userName;
        }

        public void setUserName(Object userName) {
            this.userName = userName;
        }

        public Integer getUserType() {
            return userType;
        }

        public void setUserType(Integer userType) {
            this.userType = userType;
        }
    }

    public class Zone {

        @SerializedName("part")
        @Expose
        private Integer part;
        @SerializedName("partAssocMask")
        @Expose
        private String partAssocMask;
        @SerializedName("pirCam")
        @Expose
        private Object pirCam;
        @SerializedName("presenceMuted")
        @Expose
        private Integer presenceMuted;
        @SerializedName("regDevSN")
        @Expose
        private String regDevSN;
        @SerializedName("status")
        @Expose
        private Integer status;
        @SerializedName("trouble")
        @Expose
        private Boolean trouble;
        @SerializedName("zoneID")
        @Expose
        private Integer zoneID;
        @SerializedName("zoneName")
        @Expose
        private String zoneName;
        @SerializedName("zoneType")
        @Expose
        private Integer zoneType;

        public Integer getPart() {
            return part;
        }

        public void setPart(Integer part) {
            this.part = part;
        }

        public String getPartAssocMask() {
            return partAssocMask;
        }

        public void setPartAssocMask(String partAssocMask) {
            this.partAssocMask = partAssocMask;
        }

        public Object getPirCam() {
            return pirCam;
        }

        public void setPirCam(Object pirCam) {
            this.pirCam = pirCam;
        }

        public Integer getPresenceMuted() {
            return presenceMuted;
        }

        public void setPresenceMuted(Integer presenceMuted) {
            this.presenceMuted = presenceMuted;
        }

        public String getRegDevSN() {
            return regDevSN;
        }

        public void setRegDevSN(String regDevSN) {
            this.regDevSN = regDevSN;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public Boolean getTrouble() {
            return trouble;
        }

        public void setTrouble(Boolean trouble) {
            this.trouble = trouble;
        }

        public Integer getZoneID() {
            return zoneID;
        }

        public void setZoneID(Integer zoneID) {
            this.zoneID = zoneID;
        }

        public String getZoneName() {
            return zoneName;
        }

        public void setZoneName(String zoneName) {
            this.zoneName = zoneName;
        }

        public Integer getZoneType() {
            return zoneType;
        }

        public void setZoneType(Integer zoneType) {
            this.zoneType = zoneType;
        }
    }
}
