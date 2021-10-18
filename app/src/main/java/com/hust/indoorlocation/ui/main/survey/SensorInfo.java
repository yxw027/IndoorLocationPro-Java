package com.hust.indoorlocation.ui.main.survey;

public class SensorInfo {
    /** 传感器名称 */
    public String sensorTypeName;
    /** 返回的字段含义 */
    public String[] metaData;

    public SensorInfo(String sensorTypeName, String[] metaData) {
        this.sensorTypeName = sensorTypeName;
        this.metaData = metaData;
    }
}
