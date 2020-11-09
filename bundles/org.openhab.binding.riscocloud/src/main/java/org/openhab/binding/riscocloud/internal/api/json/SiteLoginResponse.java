package org.openhab.binding.riscocloud.internal.api.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SiteLoginResponse {

    public class Root {

        @SerializedName("response")
        @Expose
        private Response response;
        @SerializedName("status")
        @Expose
        private Integer status;

        public Response getResponse() {
            return response;
        }

        public void setResponse(Response response) {
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

        @SerializedName("cpId")
        @Expose
        private String cpId;
        @SerializedName("expiresAt")
        @Expose
        private String expiresAt;
        @SerializedName("sessionId")
        @Expose
        private String sessionId;

        public String getCpId() {
            return cpId;
        }

        public void setCpId(String cpId) {
            this.cpId = cpId;
        }

        public String getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(String expiresAt) {
            this.expiresAt = expiresAt;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }
}
