package com.example.imucollector.data;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import java.util.Date;

public abstract class SensorData {
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    public final long timestamp;

    @ColumnInfo(name = "session id")
    public final int sessionId;

    @ColumnInfo(name = "record id")
    public final int recordId;

    public SensorData(long timestamp, int recordId, int sessionId){
        this.timestamp = timestamp;
        this.recordId = recordId;
        this.sessionId = sessionId;
    }
    public abstract String[] formatData();
}
