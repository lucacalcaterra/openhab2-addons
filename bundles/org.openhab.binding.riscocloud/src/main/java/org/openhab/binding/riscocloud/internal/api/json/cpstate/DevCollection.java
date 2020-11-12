
package org.openhab.binding.riscocloud.internal.api.json.cpstate;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DevCollection {

    @SerializedName("devType")
    @Expose
    private Integer devType;
    @SerializedName("devList")
    @Expose
    private List<DevList> devList = null;

    public Integer getDevType() {
        return devType;
    }

    public void setDevType(Integer devType) {
        this.devType = devType;
    }

    public List<DevList> getDevList() {
        return devList;
    }

    public void setDevList(List<DevList> devList) {
        this.devList = devList;
    }

}
