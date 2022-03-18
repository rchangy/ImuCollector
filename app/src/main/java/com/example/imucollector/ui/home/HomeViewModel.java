package com.example.imucollector.ui.home;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


import android.app.Application;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.documentfile.provider.DocumentFile;

import androidx.lifecycle.SavedStateHandle;

import com.example.imucollector.data.Session;
import com.example.imucollector.data.SessionDao;
import com.example.imucollector.data.SessionDatabase;
import com.opencsv.CSVWriter;


import java.io.FileWriter;
import java.io.IOException;
/*
存所有 UI 相關資料的地方（因為 view model 存活時間會比 activity 長、也不會因為手機轉方向就重生一個）
因為使用 data binding 所以 xml 檔案可以直接拿到這裡的資料（不用透過 activity 或 fragment 去設定）， data binding 的資料必須為 livedata
SavedStateHandle 類似一個小的 database，可以把一些簡單的變數放進去，變數的值可以在 view model 重生的時候再拿到（fragment 的 bundle 也是類似的東西）
兩個 fragment 會共用這個 view model
 */
public class HomeViewModel extends AndroidViewModel{
    private SavedStateHandle savedStateHandle;

    public MutableLiveData<Integer> currentFreq;
    public MutableLiveData<Integer> currentSessionId;
    public MutableLiveData<Integer> currentRecordId;

    public MutableLiveData<Boolean> isCollecting = new MutableLiveData<>(false);

    // database
    private SessionDatabase db = SessionDatabase.getInstance(getApplication());
    private SessionDao sessionDao = db.sessionDao();

    // timer for ui
    public MutableLiveData<String> timerText = new MutableLiveData<>("00:00:00");
    private long sessionStartTimestamp;

    // saved state
    private final String CURRENT_FREQ = "currentFreq";
    private final String CURRENT_SESSION_ID = "currentSessionId";
    private final String CURRENT_RECORD_ID = "currentRecordId";
    private final String SESSION_START_TIMESTAMP = "sessionStartTimestamp";
    private final String IS_COLLECTING = "isCollecting";

    public HomeViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.savedStateHandle = savedStateHandle;
        Integer defaultFreq = 60;
        currentFreq = savedStateHandle.getLiveData(CURRENT_FREQ, defaultFreq);
        currentSessionId = savedStateHandle.getLiveData(CURRENT_SESSION_ID, 0);
        currentRecordId = savedStateHandle.getLiveData(CURRENT_RECORD_ID, 0);
        if(savedStateHandle.contains(SESSION_START_TIMESTAMP)) sessionStartTimestamp = savedStateHandle.get(SESSION_START_TIMESTAMP);
        else sessionStartTimestamp = 0;
        isCollecting = savedStateHandle.getLiveData(IS_COLLECTING, false);
    }

    // for slider on change
    public void setCurrentFreq(int freq){
        currentFreq.setValue(freq);
        savedStateHandle.set(CURRENT_FREQ, freq);
    }

    private void incCurrentSessionId(){
        int id = currentSessionId.getValue() + 1;
        currentSessionId.setValue(id);
        savedStateHandle.set(CURRENT_SESSION_ID, id);
    }

    public void resetCurrentSessionId(){
        int id = 0;
        currentSessionId.setValue(id);
        savedStateHandle.set(CURRENT_SESSION_ID, id);
    }

    public void setCurrentRecordId(int id){
        currentRecordId.setValue(id);
        savedStateHandle.set(CURRENT_RECORD_ID, id);
    }

    private void setIsCollecting(boolean collecting){
        isCollecting.setValue(collecting);
        savedStateHandle.set(IS_COLLECTING, collecting);
    }

    private void setSessionStartTimestamp(long ts){
        sessionStartTimestamp = ts;
        savedStateHandle.set(SESSION_START_TIMESTAMP, ts);
    }

    public long getSessionStartTimestamp(){
        return sessionStartTimestamp;
    }

    // for button on click
    public void startStopTimer(){
        if(isCollecting.getValue()){
            // stop timer
            setIsCollecting(false);
            incCurrentSessionId();
        }
        else{
            // start timer
            setSessionStartTimestamp(System.currentTimeMillis());
            setIsCollecting(true);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }


    public Session[] getAllSessionData(){
        return sessionDao.getAllSessions();
    }

    public void deleteSessions(Session... sessions){
        sessionDao.deleteSessions(sessions);
    }

    public void sessionsToCsv(Uri dirUri){
        new ExportFileTask().execute(dirUri);
    }

    public void updateTimeText(){
        int passedTime = (int) (System.currentTimeMillis() - sessionStartTimestamp);

        int seconds = ((passedTime % 86400) % 3600) % 60;
        int minutes = ((passedTime % 86400) % 3600) / 60;
        int hours = ((passedTime % 86400) / 3600);
        timerText.setValue(formatTime(seconds, minutes, hours));
        return;
    }

    private String formatTime(int seconds, int minutes, int hours)
    {
        return String.format("%02d",hours) + " : " + String.format("%02d",minutes) + " : " + String.format("%02d",seconds);
    }


    private class ExportFileTask extends AsyncTask<Uri, Void, Integer>{
        @Override
        protected Integer doInBackground(Uri... uris) {
            Uri dirUri = uris[0];
            String accFilename = "";
            String gyroFilename = "";
            // create file
            DocumentFile documentFile = DocumentFile.fromTreeUri(getApplication(), dirUri);
            DocumentFile accFile = documentFile.createFile("text/csv", accFilename);
            DocumentFile gyroFile = documentFile.createFile("text/csv", gyroFilename);
            if(accFile == null || gyroFile == null){
                // TODO: file cannot be created
                return -1;
            }
            Session[] sessions = sessionDao.getAllSessions();
            try {
                // acc
                String[] header = {"id", "freq", "timestamp", "X", "Y", "Z"};
                CSVWriter writer = new CSVWriter( new FileWriter(accFile.getUri().getPath()));
                writer.writeNext(header);
                for(Session session : sessions){
//                    writer.writeAll(session.formatAccData());
                }
                writer.close();

                // gyro
                writer = new CSVWriter( new FileWriter(gyroFile.getUri().getPath()));
                writer.writeNext(header);
                for(Session session : sessions){
//                    writer.writeAll(session.formatGyroData());
                }
                writer.close();

                sessionDao.deleteSessions(sessions);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            // TODO: process result
        }
    }
}