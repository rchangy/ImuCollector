package com.example.imucollector.ui.home;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import androidx.lifecycle.SavedStateHandle;
import androidx.preference.PreferenceManager;

import com.example.imucollector.data.AccSensorData;
import com.example.imucollector.data.GyroSensorData;
import com.example.imucollector.data.Session;
import com.example.imucollector.database.SessionRepository;
import com.opencsv.CSVWriter;


import org.apache.commons.lang3.mutable.Mutable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/*
存所有 UI 相關資料的地方（因為 view model 存活時間會比 activity 長、也不會因為手機轉方向就重生一個）
因為使用 data binding 所以 xml 檔案可以直接拿到這裡的資料（不用透過 activity 或 fragment 去設定）， data binding 的資料必須為 livedata
兩個 fragment 會共用這個 view model
 */
public class HomeViewModel extends AndroidViewModel{
    private static final String LOG_TAG = "HomeViewModel";
    private static final int MAX_SESSION = 1000;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private SavedStateHandle savedStateHandle;

    public MutableLiveData<Integer> currentFreq;
    public MutableLiveData<Integer> currentSessionId;
    public MutableLiveData<Integer> currentRecordId;

    public MutableLiveData<Boolean> isCollecting = new MutableLiveData<>(false);

    public MutableLiveData<Boolean> isExporting = new MutableLiveData<>(false);


    // timer for ui
    public MutableLiveData<String> timerText = new MutableLiveData<>("00 : 00 : 000");
    private long sessionStartTimestamp;

    // saved state
    private final String PREFERENCE_FILE_KEY_FREQ = "currentFreq";
    private final String PREFERENCE_FILE_KEY_SESSION_ID = "currentSessionId";
    private final String PREFERENCE_FILE_KEY_RECORD_ID = "currentRecordId";
    private final String PREFERENCE_FILE_KEY_TIMESTAMP = "sessionStartTimestamp";
    private final String PREFERENCE_FILE_KEY_IS_COLLECTING = "isCollecting";

    private List<Long> selectedSession = new ArrayList<>();

    public HomeViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
        editor = sharedPref.edit();
        this.savedStateHandle = savedStateHandle;
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
        if(id > MAX_SESSION) id = 0;
        setCurrentSessionId(id);
    }

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

    public List<Long> getSelectedSession() { return selectedSession; }

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
        SessionRepository.getInstance().deleteSessions(selectedSession.toArray(new Long[0]));
        selectedSession.clear();
    }

    public void exportSessions(Uri uri){
        if(uri == null){
            Log.d(LOG_TAG, "export failed: null result uri");
            return;
        }
        new ExportFileTask().execute(uri);
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
        private final String ACC_DIR_NAME = "Accelerometer";
        private final String GYRO_DIR_NAME = "Gyroscope";

        @Override
        protected Integer doInBackground(Uri... resultUri) {
            Uri dirUri = resultUri[0];
            DocumentFile imuCollectorDocumentFile = DocumentFile.fromTreeUri(getApplication(), dirUri);
            Log.d(LOG_TAG, imuCollectorDocumentFile.getUri().getPath());
            DocumentFile accDirDocumentFile = imuCollectorDocumentFile.findFile(ACC_DIR_NAME);
            if(accDirDocumentFile == null){
                accDirDocumentFile = imuCollectorDocumentFile.createDirectory(ACC_DIR_NAME);
            }
            DocumentFile gyroDirDocumentFile = imuCollectorDocumentFile.findFile(GYRO_DIR_NAME);
            if(gyroDirDocumentFile == null){
                gyroDirDocumentFile = imuCollectorDocumentFile.createDirectory(GYRO_DIR_NAME);
            }
            String currentTime = String.valueOf(System.currentTimeMillis());
            String accFilename = "acc_" + currentTime;
            String gyroFilename = "gyro_" + currentTime;

            String[] header = {"record id", "session id", "timestamp", "X", "Y", "Z"};

            try{
                Session[] sessions;
                if(selectedSession.isEmpty()){
                    sessions = getAllSessions().getValue().toArray(new Session[0]);
                }
                else{
                    sessions = SessionRepository.getInstance().getSelectedSessionsInBackground(selectedSession.toArray(new Long[0]));
                }

                DocumentFile accDocumentFile = accDirDocumentFile.createFile("text/csv", accFilename);
                Uri accUri = accDocumentFile.getUri();
                ParcelFileDescriptor accPdf = getApplication().getContentResolver().openFileDescriptor(accUri, "w");
                FileOutputStream accOutputStream = new FileOutputStream(accPdf.getFileDescriptor());
                accOutputStream.write(stringJoiner(header).getBytes());
                for(Session session : sessions){
                    AccSensorData[] accData = SessionRepository.getInstance().getSessionAccDataInBackground(session);
                    for(AccSensorData data : accData){
                        accOutputStream.write(stringJoiner(data.formatData()).getBytes());
                    }
                }
                accOutputStream.close();
                accPdf.close();

                DocumentFile gyroDocumentFile = gyroDirDocumentFile.createFile("text/csv", gyroFilename);
                Uri gyroUri = gyroDocumentFile.getUri();
                ParcelFileDescriptor gyroPdf = getApplication().getContentResolver().openFileDescriptor(gyroUri, "w");
                FileOutputStream gyroOutputStream = new FileOutputStream(gyroPdf.getFileDescriptor());
                gyroOutputStream.write(stringJoiner(header).getBytes());
                for(Session session : sessions){
                    GyroSensorData[] gyroData = SessionRepository.getInstance().getSessionGyroDataInBackground(session);
                    for(GyroSensorData data : gyroData){
                        gyroOutputStream.write(stringJoiner(data.formatData()).getBytes());
                    }
                }
                gyroOutputStream.close();
                gyroPdf.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            Log.d(LOG_TAG, "export success");
            return 0;
        }
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            // TODO: process result
        }

        public String stringJoiner(String[] arr) {
            StringJoiner joiner = new StringJoiner(",", "", "\n");
            for (String el : arr) {
                joiner.add(el);
            }
            return joiner.toString();
        }
    }
}
