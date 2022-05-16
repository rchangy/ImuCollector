package com.example.imucollector.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.imucollector.data.AccSensorData;

@Dao
public interface AccSensorDataDao {
    // acc sensor data
    @Insert
    public void insertAccSensorData(AccSensorData... data);

    @Delete
    public void deleteAccSensorData(AccSensorData... data);

    @Query("SELECT * FROM `acc data` ORDER BY timestamp")
    public AccSensorData[] getAllAccSensorData();

    @Query("SELECT * FROM `acc data` WHERE `record id` == :recordId AND `session id` == :sessionId")
    public AccSensorData[] getAccSensorDataBySession(int recordId, int sessionId);

    @Query("DELETE FROM `acc data`")
    public void deleteAllAccSensorData();

    @Query("DELETE FROM `acc data` WHERE `record id` == :recordId AND `session id` == :sessionId")
    public void deleteAccSensorDataBySession(int recordId, int sessionId);
}
