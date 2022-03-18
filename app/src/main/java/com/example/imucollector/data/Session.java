package com.example.imucollector.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity(tableName = "session")
public class Session {
    public final long timestamp;
    public final int freq;

    @PrimaryKey
    @ColumnInfo(name = "record id")
    public final int recordId;

    @ColumnInfo(name = "session id")
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

//    public void setSensorData(ArrayList<SensorData> acc, ArrayList<SensorData> gyro){
//        this.acc = acc;
//        this.gyro = gyro;
//    }

//    public List<String[]> formatAccData(){
//        ArrayList<String[]> ret = new ArrayList<>();
//        String[] sessionData = {String.valueOf(id), String.valueOf(freq)};
//
//        for(SensorData data : acc){
//            String[] formatData = concatArray(sessionData, data.formatData());
//            ret.add(formatData);
//        }
//        return ret;
//    }
//
//    public List<String[]> formatGyroData(){
//        ArrayList<String[]> ret = new ArrayList<>();
//        String[] sessionData = {String.valueOf(id), String.valueOf(freq)};
//
//        for(SensorData data : gyro){
//            String[] formatData = concatArray(sessionData, data.formatData());
//            ret.add(formatData);
//        }
//        return ret;
//    }

//    private String[] concatArray(String[] arr1, String[] arr2){
//        String[] ret = Arrays.copyOf(arr1, arr1.length + arr2.length);
//        System.arraycopy(arr2, 0, ret, arr1.length, arr2.length);
//        return ret;
//    }
}
