package com.example.imucollector.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Index;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.imucollector.data.Session;

/*
其他 class 用 Dao 來跟 database 溝通
可以自行增加 method，@Query 括號內是 SQL 語法
 */

@Dao
public interface SessionDao {

    // sessions
    @Insert
    public void insertSessions(Session... sessions);

    @Delete
    public void deleteSessions(Session... sessions);

    @Query("SELECT * FROM session ORDER BY `record id`, `session id`")
    public Session[] getAllSessions();

    // acc sensor data
    @Insert
    public void insertAccSensorData(AccSensorData... data);

    @Delete
    public void deleteAccSensorData(AccSensorData... data);

    @Query("SELECT * FROM `acc data` ORDER BY timestamp")
    public AccSensorData[] getAllAccSensorData();

    // gyro sensor data
    @Insert
    public void insertGyroSensorData(GyroSensorData... data);

    @Delete
    public void deleteGyroSensorData(GyroSensorData... data);

    @Query("SELECT * FROM `gyro data` ORDER BY timestamp")
    public GyroSensorData[] getAllGyroSensorData();


}
