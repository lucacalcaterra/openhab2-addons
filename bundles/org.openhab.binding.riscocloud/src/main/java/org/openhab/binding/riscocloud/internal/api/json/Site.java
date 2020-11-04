package org.openhab.binding.riscocloud.internal.api.json;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Site {

    public class Root {

        @SerializedName("response")
        @Expose
        private List<Response> response = null;
        @SerializedName("status")
        @Expose
        private Integer status;

        public List<Response> getResponse() {
            return response;
        }

        public void setResponse(List<Response> response) {
            this.response = response;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }

    public class Response {

        @SerializedName("companyId")
        @Expose
        private Integer companyId;
        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("masterUserId")
        @Expose
        private Integer masterUserId;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("ownerUserId")
        @Expose
        private Integer ownerUserId;
        @SerializedName("requirePin")
        @Expose
        private Boolean requirePin;
        @SerializedName("serviceState")
        @Expose
        private Integer serviceState;
        @SerializedName("siteDetailsMissing")
        @Expose
        private Boolean siteDetailsMissing;
        @SerializedName("siteUUID")
        @Expose
        private String siteUUID;
        @SerializedName("userId")
        @Expose
        private Integer userId;
        @SerializedName("userInvitationState")
        @Expose
        private Integer userInvitationState;

        public Integer getCompanyId() {
            return companyId;
        }

        public void setCompanyId(Integer companyId) {
            this.companyId = companyId;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getMasterUserId() {
            return masterUserId;
        }

        public void setMasterUserId(Integer masterUserId) {
            this.masterUserId = masterUserId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getOwnerUserId() {
            return ownerUserId;
        }

        public void setOwnerUserId(Integer ownerUserId) {
            this.ownerUserId = ownerUserId;
        }

        public Boolean getRequirePin() {
            return requirePin;
        }

        public void setRequirePin(Boolean requirePin) {
            this.requirePin = requirePin;
        }

        public Integer getServiceState() {
            return serviceState;
        }

        public void setServiceState(Integer serviceState) {
            this.serviceState = serviceState;
        }

        public Boolean getSiteDetailsMissing() {
            return siteDetailsMissing;
        }

        public void setSiteDetailsMissing(Boolean siteDetailsMissing) {
            this.siteDetailsMissing = siteDetailsMissing;
        }

        public String getSiteUUID() {
            return siteUUID;
        }

        public void setSiteUUID(String siteUUID) {
            this.siteUUID = siteUUID;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public Integer getUserInvitationState() {
            return userInvitationState;
        }

        public void setUserInvitationState(Integer userInvitationState) {
            this.userInvitationState = userInvitationState;
        }
    }
}
