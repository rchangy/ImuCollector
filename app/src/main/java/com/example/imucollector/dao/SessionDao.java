package com.example.imucollector.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.imucollector.data.AccSensorData;
import com.example.imucollector.data.GyroSensorData;
import com.example.imucollector.data.Session;

import java.util.List;


@Dao
public interface SessionDao {

    // sessions
    @Insert
    public void insertSessions(Session... sessions);

    @Delete
    public void deleteSessions(Session... sessions);

    @Query("SELECT * FROM session ORDER BY timestamp")
    public LiveData<List<Session>> getAllSessions();

    @Query("SELECT * FROM session WHERE timestamp IN (:timestamps)")
    public Session[] getSelectedSessions(Long... timestamps);

    @Query("SElECT * FROM session ORDER BY timestamp")
    public Session[] getAllSessionsArray();

    @Query("DELETE FROM session")
    public void deleteAllSessions();

//    // acc sensor data
//    @Insert
//    public void insertAccSensorData(AccSensorData... data);
//
//    @Delete
//    public void deleteAccSensorData(AccSensorData... data);
//
//    @Query("SELECT * FROM `acc data` ORDER BY timestamp")
//    public AccSensorData[] getAllAccSensorData();
//
//    @Query("SELECT * FROM `acc data` WHERE `record id` == :recordId AND `session id` == :sessionId")
//    public AccSensorData[] getAccSensorDataBySession(int recordId, int sessionId);
//
//    @Query("DELETE FROM `acc data`")
//    public void deleteAllAccSensorData();
//
//    @Query("DELETE FROM `acc data` WHERE `record id` == :recordId AND `session id` == :sessionId")
//    public void deleteAccSensorDataBySession(int recordId, int sessionId);


//    // gyro sensor data
//    @Insert
//    public void insertGyroSensorData(GyroSensorData... data);
//
//    @Delete
//    public void deleteGyroSensorData(GyroSensorData... data);
//
//    @Query("SELECT * FROM `gyro data` ORDER BY timestamp")
//    public GyroSensorData[] getAllGyroSensorData();
//
//    @Query("SELECT * FROM `gyro data` WHERE `record id` == :recordId AND `session id` == :sessionId")
//    public GyroSensorData[] getGyroSensorDataBySession(int recordId, int sessionId);
//
//    @Query("DELETE FROM `gyro data`")
//    public void deleteAllGyroSensorData();
//
//    @Query("DELETE FROM `gyro data` WHERE `record id` == :recordId AND `session id` == :sessionId")
//    public void deleteGyroSensorDataBySession(int recordId, int sessionId);
//

}
