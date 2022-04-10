package com.example.imucollector.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "session")
public class Session {
    @PrimaryKey
    public final long timestamp;
    public final int freq;
    public final int recordId;
    public final int sessionId;

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
