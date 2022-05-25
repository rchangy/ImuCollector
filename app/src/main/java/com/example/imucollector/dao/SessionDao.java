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

}
