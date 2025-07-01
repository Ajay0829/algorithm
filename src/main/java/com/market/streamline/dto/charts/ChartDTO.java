package com.market.streamline.dto.charts;

public class ChartDTO {
    private String type;
    private String action;
    private Object data;

    public ChartDTO() {}

    public ChartDTO(String type, String action, Object data) {
        this.type = type;
        this.action = action;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
