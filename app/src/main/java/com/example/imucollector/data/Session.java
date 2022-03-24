package com.example.imucollector.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity(tableName = "session")
public class Session {
    @PrimaryKey
    public final long timestamp;

    public final int freq;

    public final int recordId;

    public final int sessionId;

//    private String duration;
//    private ArrayList<SensorData> acc;
//    private ArrayList<SensorData> gyro;

    public Session(long timestamp, int freq, int recordId, int sessionId){
        this.timestamp = timestamp;
        this.freq = freq;
        this.recordId = recordId;
        this.sessionId = sessionId;
    }

    public int getRecordId() {return  recordId;}

    public int getSessionId() {return sessionId;}

    public Date getDate() {return new Date(timestamp);}

}
