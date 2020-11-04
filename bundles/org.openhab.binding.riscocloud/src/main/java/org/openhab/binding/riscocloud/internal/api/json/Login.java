package org.openhab.binding.riscocloud.internal.api.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Login {

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

        @SerializedName("accessToken")
        @Expose
        private String accessToken;
        @SerializedName("expiresAt")
        @Expose
        private String expiresAt;
        @SerializedName("refreshToken")
        @Expose
        private String refreshToken;
        @SerializedName("tokenType")
        @Expose
        private String tokenType;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(String expiresAt) {
            this.expiresAt = expiresAt;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }
    }
}
