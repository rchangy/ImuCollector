package com.example.imucollector.data;

import androidx.room.Entity;

@Entity(tableName = "gyro data")
public class GyroSensorData extends SensorData{

    public final double x;
    public final double y;
    public final double z;

    public GyroSensorData(long timestamp, int recordId, int sessionId, double x, double y, double z){
        super(timestamp, recordId, sessionId);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String[] formatData() {
        String[] ret = {String.valueOf(recordId), String.valueOf(sessionId), String.valueOf(timestamp), String.valueOf(x), String.valueOf(y), String.valueOf(z)};
        return ret;
    }
}
