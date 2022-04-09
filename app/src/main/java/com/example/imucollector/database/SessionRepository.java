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

    public Session[] getSelectedSessionsInBackground(Long[] timestamps){
        Session[] sessions = sessionDao.getSelectedSessions(timestamps);
        return sessions;
    }

    public void deleteSessions(Long[] timestamps){
        SessionDatabase.pool.execute(()->{
            Session[] sessions = sessionDao.getSelectedSessions(timestamps);
            for(Session session : sessions){
                deleteSessionInBackground(session);
            }
        });
    }

    private void deleteSessionInBackground(Session session){
        sessionDao.deleteSessions(session);
        sessionDao.deleteAccSensorDataBySession(session.recordId, session.sessionId);
        sessionDao.deleteGyroSensorDataBySession(session.recordId, session.sessionId);
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

    public AccSensorData[] getSessionAccDataInBackground(Session session){
        return sessionDao.getAccSensorDataBySession(session.recordId, session.sessionId);
    }

    public AccSensorData[] getAllAccDataInBackground(){
        return sessionDao.getAllAccSensorData();
    }

    public GyroSensorData[] getSessionGyroDataInBackground(Session session){
        return sessionDao.getGyroSensorDataBySession(session.recordId, session.sessionId);
    }
}
