package com.example.imucollector.ui.home;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import androidx.lifecycle.SavedStateHandle;
import androidx.preference.PreferenceManager;

import com.example.imucollector.data.Session;
import com.example.imucollector.database.SessionRepository;
import com.opencsv.CSVWriter;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
存所有 UI 相關資料的地方（因為 view model 存活時間會比 activity 長、也不會因為手機轉方向就重生一個）
因為使用 data binding 所以 xml 檔案可以直接拿到這裡的資料（不用透過 activity 或 fragment 去設定）， data binding 的資料必須為 livedata
兩個 fragment 會共用這個 view model
 */
public class HomeViewModel extends AndroidViewModel{
    private static final String LOG_TAG = "HomeViewModel";

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private SavedStateHandle savedStateHandle;

    public MutableLiveData<Integer> currentFreq;
    public MutableLiveData<Integer> currentSessionId;
    public MutableLiveData<Integer> currentRecordId;

    public MutableLiveData<Boolean> isCollecting = new MutableLiveData<>(false);


    // timer for ui
    public MutableLiveData<String> timerText = new MutableLiveData<>("00 : 00 : 000");
    private long sessionStartTimestamp;

    // saved state
    private final String PREFERENCE_FILE_KEY_FREQ = "currentFreq";
    private final String PREFERENCE_FILE_KEY_SESSION_ID = "currentSessionId";
    private final String PREFERENCE_FILE_KEY_RECORD_ID = "currentRecordId";
    private final String PREFERENCE_FILE_KEY_TIMESTAMP = "sessionStartTimestamp";
    private final String PREFERENCE_FILE_KEY_IS_COLLECTING = "isCollecting";

    private List<Session> selectedSession = new ArrayList<>();

    public HomeViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
        editor = sharedPref.edit();
        this.savedStateHandle = savedStateHandle;
//        allSessions = SessionRepository.getInstance().getAllSessions();
        load();
        Log.d(LOG_TAG, "view model init with freq: " + currentFreq.getValue() + ", sessionId: " + currentSessionId.getValue() + ", recordId: " + currentRecordId.getValue());
    }

    public void load(){
        Integer defaultFreq = 60;
        if(savedStateHandle.contains(PREFERENCE_FILE_KEY_FREQ)) currentFreq = savedStateHandle.getLiveData(PREFERENCE_FILE_KEY_FREQ);
        else {
            currentFreq = new MutableLiveData<>();
            setCurrentFreq(sharedPref.getInt(PREFERENCE_FILE_KEY_FREQ, defaultFreq));
        }
        if(savedStateHandle.contains(PREFERENCE_FILE_KEY_SESSION_ID)) currentSessionId = savedStateHandle.getLiveData(PREFERENCE_FILE_KEY_SESSION_ID);
        else {
            currentSessionId = new MutableLiveData<>();
            setCurrentSessionId(sharedPref.getInt(PREFERENCE_FILE_KEY_SESSION_ID, 0));
        }
        if(savedStateHandle.contains(PREFERENCE_FILE_KEY_RECORD_ID)) currentRecordId = savedStateHandle.getLiveData(PREFERENCE_FILE_KEY_RECORD_ID);
        else {
            currentRecordId = new MutableLiveData<>();
            setCurrentRecordId(sharedPref.getInt(PREFERENCE_FILE_KEY_RECORD_ID, 0));
        }
        if(savedStateHandle.contains(PREFERENCE_FILE_KEY_TIMESTAMP)) sessionStartTimestamp = savedStateHandle.get(PREFERENCE_FILE_KEY_TIMESTAMP);
        else setSessionStartTimestamp(sharedPref.getLong(PREFERENCE_FILE_KEY_TIMESTAMP, 0));
        if(savedStateHandle.contains(PREFERENCE_FILE_KEY_IS_COLLECTING)) isCollecting = savedStateHandle.getLiveData(PREFERENCE_FILE_KEY_IS_COLLECTING);
        else {
            isCollecting = new MutableLiveData<>();
            setIsCollecting(sharedPref.getBoolean(PREFERENCE_FILE_KEY_IS_COLLECTING, false));
        }
        Log.d(LOG_TAG, "load data, is collecting: " + isCollecting.getValue());
    }

    public void save(){
        editor.putInt(PREFERENCE_FILE_KEY_SESSION_ID, currentSessionId.getValue());
        editor.putInt(PREFERENCE_FILE_KEY_RECORD_ID, currentRecordId.getValue());
        editor.putInt(PREFERENCE_FILE_KEY_FREQ, currentFreq.getValue());
        editor.putLong(PREFERENCE_FILE_KEY_TIMESTAMP, sessionStartTimestamp);
        editor.apply();
    }

    // for slider on change
    public void setCurrentFreq(int freq){
        currentFreq.setValue(freq);
        savedStateHandle.set(PREFERENCE_FILE_KEY_FREQ, freq);
    }

    public void setCurrentSessionId(int id){
        currentSessionId.setValue(id);
        savedStateHandle.set(PREFERENCE_FILE_KEY_SESSION_ID, id);
    }

    private void incCurrentSessionId(){
        int id = currentSessionId.getValue() + 1;
        setCurrentSessionId(id);
    }

    public void resetCurrentSessionId() {setCurrentFreq(0);}

    public void setCurrentRecordId(int id){
        currentRecordId.setValue(id);
        savedStateHandle.set(PREFERENCE_FILE_KEY_RECORD_ID, id);
    }

    private void setIsCollecting(boolean collecting){
        isCollecting.setValue(collecting);
        savedStateHandle.set(PREFERENCE_FILE_KEY_IS_COLLECTING, collecting);
    }

    private void setSessionStartTimestamp(long ts){
        sessionStartTimestamp = ts;
        savedStateHandle.set(PREFERENCE_FILE_KEY_TIMESTAMP, ts);
    }

    public long getSessionStartTimestamp(){
        return sessionStartTimestamp;
    }

    public LiveData<List<Session>> getAllSessions() {
        return SessionRepository.getInstance().getAllSessions();
    }

    public List<Session> getSelectedSession() { return selectedSession; }

    // for button on click
    public void startStopTimer(){
        if(isCollecting.getValue()){
            // stop timer
            setIsCollecting(false);
            incCurrentSessionId();
            timerText.setValue("00 : 00 : 000");
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

    public void deleteSessions(){
        SessionRepository.getInstance().getSessionDao().deleteSessions(selectedSession.toArray(new Session[0]));
        selectedSession.clear();
    }

    public void sessionsToCsv(Uri dirUri){
        new ExportFileTask().execute(dirUri);
    }

    public String getTimeText(){
        int passedTime = (int) (System.currentTimeMillis() - sessionStartTimestamp);
        int ms = ((passedTime % 3600000) % 60000) % 1000;
        int seconds = (passedTime % 3600000) % 60000 / 1000;
        int minutes = (passedTime % 3600000) / 60000;

        return formatTime(ms, seconds, minutes);
    }

    private String formatTime(int ms, int seconds, int minutes) { return String.format("%02d",minutes) + " : " + String.format("%02d",seconds) + " : " + String.format("%03d",ms); }

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
            // TODO
            Session[] sessions = null;
//            Session[] sessions = SessionRepository.getInstance().getSessionDao().getAllSessions();
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

                SessionRepository.getInstance().getSessionDao().deleteSessions(sessions);
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

//    public void saveFalseSession(){
//        new SaveSessionTask().execute();
//    }
//    private class SaveSessionTask extends AsyncTask<Void, Void, Void>{
//        @Override
//        protected Void doInBackground(Void... voids) {
//            Session testSession = new Session(sessionStartTimestamp, currentFreq.getValue(), currentRecordId.getValue(), currentSessionId.getValue());
//            DBController.getInstance().getSessionDao().insertSessions(testSession);
//            return null;
//        }
//    }
}