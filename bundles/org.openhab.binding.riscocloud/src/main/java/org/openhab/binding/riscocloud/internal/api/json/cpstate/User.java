
package org.openhab.binding.riscocloud.internal.api.json.cpstate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("userID")
    @Expose
    private Integer userID;
    @SerializedName("userName")
    @Expose
    private Object userName;
    @SerializedName("userType")
    @Expose
    private Integer userType;
    @SerializedName("passCode")
    @Expose
    private String passCode;
    @SerializedName("part")
    @Expose
    private Integer part;
    @SerializedName("partAssocMask")
    @Expose
    private Object partAssocMask;

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

    public String getPassCode() {
        return passCode;
    }

    public void setPassCode(String passCode) {
        this.passCode = passCode;
    }

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

}
