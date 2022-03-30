package com.example.imucollector.database;

import androidx.lifecycle.LiveData;

import com.example.imucollector.data.AccSensorData;
import com.example.imucollector.data.GyroSensorData;
import com.example.imucollector.data.Session;

import java.util.List;

public class SessionRepository {
    private static final String LOG_TAG = "DBController";
    private static SessionRepository INSTANCE = null;

    private SessionDao sessionDao;
    private LiveData<List<Session>> allSessions;

    private SessionRepository(){ }

    public void init(SessionDatabase db) {
        sessionDao = db.sessionDao();
        allSessions = sessionDao.getAllSessions();
    }

    public static SessionRepository getInstance(){
        if(INSTANCE == null){
            INSTANCE = new SessionRepository();
        }
        return INSTANCE;
    }

    public SessionDao getSessionDao(){ return sessionDao; }
    public LiveData<List<Session>> getAllSessions(){
        return allSessions;
    }

    public void onActivityDestroyed(){
//        SessionDatabase.pool.shutdown();
    }

    public void deleteAllData(){
        SessionDatabase.pool.execute(()->{
            sessionDao.deleteAllSessions();
            sessionDao.deleteAllAccSensorData();
            sessionDao.deleteAllGyroSensorData();
        });
    }

    public void deleteSessions(Session... sessions){
        for(Session session : sessions){
            deleteSession(session);
        }
    }

    private void deleteSession(Session session){
        SessionDatabase.pool.execute(()->{
            sessionDao.deleteSessions(session);
            sessionDao.deleteAccSensorDataBySession(session.recordId, session.sessionId);
            sessionDao.deleteGyroSensorDataBySession(session.recordId, session.sessionId);
        });
    }

    public void insertAccData(AccSensorData[] data){
        SessionDatabase.pool.execute(() ->{
            sessionDao.insertAccSensorData(data);
        });
    }

    public void insertGyroData(GyroSensorData[] data){
        SessionDatabase.pool.execute(() ->{
            sessionDao.insertGyroSensorData(data);
        });
    }

    public void insertSession(Session session){
        SessionDatabase.pool.execute(() ->{
            sessionDao.insertSessions(session);
        });
    }
}
