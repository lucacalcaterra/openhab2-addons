
package org.openhab.binding.riscocloud.internal.api.json.cpstate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DevList {

    @SerializedName("num")
    @Expose
    private Integer num;
    @SerializedName("desc")
    @Expose
    private String desc;
    @SerializedName("extra")
    @Expose
    private Object extra;

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

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

}
