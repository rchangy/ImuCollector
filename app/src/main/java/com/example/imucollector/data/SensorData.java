package com.example.imucollector.data;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import java.util.Date;

public abstract class SensorData {
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    protected long timestamp;

    @ColumnInfo(name = "session id")
    protected int sessionId;

    @ColumnInfo(name = "record id")
    protected int recordId;

    public SensorData(long timestamp, int recordId, int sessionId){
        this.timestamp = timestamp;
        this.recordId = recordId;
        this.sessionId = sessionId;
    }
    public abstract String[] formatData();
}
