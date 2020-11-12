
package org.openhab.binding.riscocloud.internal.api.json.cpstate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CpStateResponse {

    @SerializedName("result")
    @Expose
    private Integer result;
    @SerializedName("validationErrors")
    @Expose
    private Object validationErrors;
    @SerializedName("errorText")
    @Expose
    private Object errorText;
    @SerializedName("errorTextCodeID")
    @Expose
    private String errorTextCodeID;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("response")
    @Expose
    private Response response;

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public Object getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Object validationErrors) {
        this.validationErrors = validationErrors;
    }

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

}
