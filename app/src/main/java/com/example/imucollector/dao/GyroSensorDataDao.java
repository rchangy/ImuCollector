package com.example.imucollector.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.imucollector.data.GyroSensorData;

@Dao
public interface GyroSensorDataDao {
    @Insert
    public void insertGyroSensorData(GyroSensorData... data);

    @Delete
    public void deleteGyroSensorData(GyroSensorData... data);

    @Query("SELECT * FROM `gyro data` ORDER BY timestamp")
    public GyroSensorData[] getAllGyroSensorData();

    @Query("SELECT * FROM `gyro data` WHERE `record id` == :recordId AND `session id` == :sessionId")
    public GyroSensorData[] getGyroSensorDataBySession(int recordId, int sessionId);

    @Query("DELETE FROM `gyro data`")
    public void deleteAllGyroSensorData();

    @Query("DELETE FROM `gyro data` WHERE `record id` == :recordId AND `session id` == :sessionId")
    public void deleteGyroSensorDataBySession(int recordId, int sessionId);

}
