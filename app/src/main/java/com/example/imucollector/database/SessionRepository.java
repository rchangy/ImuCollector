package com.example.imucollector.database;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.imucollector.dao.AccSensorDataDao;
import com.example.imucollector.dao.GyroSensorDataDao;
import com.example.imucollector.dao.SessionDao;
import com.example.imucollector.data.AccSensorData;
import com.example.imucollector.data.GyroSensorData;
import com.example.imucollector.data.Session;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SessionRepository {
    private static final String LOG_TAG = "SessionRepository";
    private static SessionRepository INSTANCE = null;
    private static final int NUMBER_OF_THREADS = 1;

    private static int userCnt = 0;

    private ExecutorService pool;
    private SessionDatabase db;
    private SessionDao sessionDao;
    private AccSensorDataDao accSensorDataDao;
    private GyroSensorDataDao gyroSensorDataDao;
    private LiveData<List<Session>> allSessions;


    private SessionRepository(){ }

    public synchronized void init(Context context) {
        if(userCnt == 0){
            db = SessionDatabase.getInstance(context);
            sessionDao = db.sessionDao();
            accSensorDataDao = db.accSensorDataDao();
            gyroSensorDataDao = db.gyroSensorDataDao();
            allSessions = sessionDao.getAllSessions();
            pool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        }
        userCnt++;
    }

    public synchronized static SessionRepository getInstance(){
        if(INSTANCE == null){
            INSTANCE = new SessionRepository();
        }
        return INSTANCE;
    }

    public synchronized void shutDownDatabase(){
        userCnt--;
        if(userCnt == 0){
            Log.d(LOG_TAG, "close db");
//            db.close();
            pool.shutdown();
        }
    }

    public LiveData<List<Session>> getAllSessions(){
        return allSessions;
    }

    public void insertSession(Session session){
        pool.execute(() ->{
            sessionDao.insertSessions(session);
        });
    }

    public void insertAccData(AccSensorData[] data){
        pool.execute(() ->{
            accSensorDataDao.insertAccSensorData(data);
        });
    }

    public void insertGyroData(GyroSensorData[] data){
        pool.execute(() ->{
            gyroSensorDataDao.insertGyroSensorData(data);
        });
    }

    public Session[] getAllSessionsInBackground(){
        Session[] sessions = sessionDao.getAllSessionsArray();
        return getSensorDataForSessions(sessions);
    }

    public Session[] getSelectedSessionsInBackground(Long[] timestamps){
        Session[] sessions = sessionDao.getSelectedSessions(timestamps);
        return getSensorDataForSessions(sessions);
    }

    private Session[] getSensorDataForSessions(Session[] sessions){
        for(Session session : sessions){
            session.setAccSensorData(accSensorDataDao.getAccSensorDataBySession(session.recordId, session.sessionId));
            session.setGyroSensorData(gyroSensorDataDao.getGyroSensorDataBySession(session.recordId, session.sessionId));
        }
        return sessions;
    }

    public void deleteSessions(Long[] timestamps){
        pool.execute(()->{
            Session[] sessions = sessionDao.getSelectedSessions(timestamps);
            for(Session session : sessions){
                deleteSessionInBackground(session);
            }
        });
    }

    private void deleteSessionInBackground(Session session){
        sessionDao.deleteSessions(session);
        accSensorDataDao.deleteAccSensorDataBySession(session.recordId, session.sessionId);
        gyroSensorDataDao.deleteGyroSensorDataBySession(session.recordId, session.sessionId);
    }
}
