
package org.openhab.binding.riscocloud.internal.api.json.cpstate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Zone {

    @SerializedName("zoneID")
    @Expose
    private Integer zoneID;
    @SerializedName("zoneName")
    @Expose
    private String zoneName;
    @SerializedName("zoneType")
    @Expose
    private Integer zoneType;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("trouble")
    @Expose
    private Boolean trouble;
    @SerializedName("part")
    @Expose
    private Integer part;
    @SerializedName("regDevSN")
    @Expose
    private String regDevSN;
    @SerializedName("pirCam")
    @Expose
    private Object pirCam;
    @SerializedName("partAssocMask")
    @Expose
    private String partAssocMask;
    @SerializedName("presenceMuted")
    @Expose
    private Integer presenceMuted;

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

    public Integer getPart() {
        return part;
    }

    public void setPart(Integer part) {
        this.part = part;
    }

    public String getRegDevSN() {
        return regDevSN;
    }

    public void setRegDevSN(String regDevSN) {
        this.regDevSN = regDevSN;
    }

    public Object getPirCam() {
        return pirCam;
    }

    public void setPirCam(Object pirCam) {
        this.pirCam = pirCam;
    }

    public String getPartAssocMask() {
        return partAssocMask;
    }

    public void setPartAssocMask(String partAssocMask) {
        this.partAssocMask = partAssocMask;
    }

    public Integer getPresenceMuted() {
        return presenceMuted;
    }

    public void setPresenceMuted(Integer presenceMuted) {
        this.presenceMuted = presenceMuted;
    }

}
