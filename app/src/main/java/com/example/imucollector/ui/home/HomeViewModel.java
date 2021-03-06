package com.example.imucollector.ui.home;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.SavedStateHandle;
import androidx.preference.PreferenceManager;

import com.example.imucollector.data.Session;
import com.example.imucollector.database.SessionDatabase;
import com.example.imucollector.database.SessionRepository;
import com.example.imucollector.export.CsvExporter;
import com.example.imucollector.service.MotionDataService;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel{
    private static final String LOG_TAG = "HomeViewModel";
    private static final int MAX_SESSION = 1000;
    public static final String BROADCAST_INTENT_ACTION = "ViewModelDestroyed";

    private final SharedPreferences sharedPref;
    private final SharedPreferences.Editor editor;
    private final SavedStateHandle savedStateHandle;

    private final String PREFERENCE_FILE_KEY_FREQ = "currentFreq";
    private final String PREFERENCE_FILE_KEY_SESSION_ID = "currentSessionId";
    private final String PREFERENCE_FILE_KEY_RECORD_ID = "currentRecordId";
    private final String PREFERENCE_FILE_KEY_TIMESTAMP = "sessionStartTimestamp";
    private final String PREFERENCE_FILE_KEY_IS_COLLECTING = "isCollecting";

    public MutableLiveData<Integer> currentFreq;
    public MutableLiveData<Integer> currentSessionId;
    public MutableLiveData<Integer> currentRecordId;

    public MutableLiveData<Boolean> isCollecting = new MutableLiveData<>(false);
    public MutableLiveData<String> startStopTimerButtonText = new MutableLiveData<>();

    // timer ui
    public MutableLiveData<String> timerText = new MutableLiveData<>("00 : 00 : 000");
    private long sessionStartTimestamp;

    private final CsvExporter csvExporter = new CsvExporter();
    private final List<Long> selectedSession = new ArrayList<>();

    private Intent intentService;
    public static final String INTENT_EXTRA_KEY_ACTION = "Action";
    public static final String INTENT_EXTRA_ACTION_START = "Start";
    public static final String INTENT_EXTRA_KEY_SESSION_ID = "SessionId";
    public static final String INTENT_EXTRA_KEY_RECORD_ID = "RecordId";
    public static final String INTENT_EXTRA_KEY_FREQ = "Freq";
    public static final String INTENT_EXTRA_KEY_TIMESTAMP = "Timestamp";
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MotionDataService.BROADCAST_INTENT_ACTION)){
                if(isCollecting.getValue()){
                    Log.d(LOG_TAG, "receive stop");
                    setIsCollecting(false);
                    incCurrentSessionId();
                    timerText.setValue("00 : 00 : 000");
                }
            }
        }
    };

    public HomeViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
        editor = sharedPref.edit();
        this.savedStateHandle = savedStateHandle;
        load();
        SessionRepository.getInstance().init(SessionDatabase.getInstance(getApplication()));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MotionDataService.BROADCAST_INTENT_ACTION);
        getApplication().registerReceiver(receiver, intentFilter);
    }

    public void load(){
        int defaultFreq = 60;
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
        editor.putBoolean(PREFERENCE_FILE_KEY_IS_COLLECTING, isCollecting.getValue());
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
        if(collecting){
            startStopTimerButtonText.setValue("Stop");
        }
        else{
            startStopTimerButtonText.setValue("Start");
        }
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
            getApplication().stopService(intentService);
        }
        else{
            // start timer
            setSessionStartTimestamp(System.currentTimeMillis());
            setIsCollecting(true);
            intentService = new Intent(getApplication(), MotionDataService.class);
            intentService.putExtra(INTENT_EXTRA_KEY_ACTION, INTENT_EXTRA_ACTION_START);
            intentService.putExtra(INTENT_EXTRA_KEY_RECORD_ID, currentRecordId.getValue());
            intentService.putExtra(INTENT_EXTRA_KEY_SESSION_ID, currentSessionId.getValue());
            intentService.putExtra(INTENT_EXTRA_KEY_FREQ, currentFreq.getValue());
            intentService.putExtra(INTENT_EXTRA_KEY_TIMESTAMP, getSessionStartTimestamp());
            getApplication().startService(intentService);
        }
    }

    @Override
    protected void onCleared() {
        if(isCollecting.getValue()){
            Log.d(LOG_TAG, "on cleared");
            setIsCollecting(false);
            incCurrentSessionId();
            getApplication().sendBroadcast(new Intent(BROADCAST_INTENT_ACTION));
        }
        else{
            SessionRepository.getInstance().shutDownDatabaseThreadPool();
        }
        getApplication().unregisterReceiver(receiver);
        save();
        super.onCleared();
    }

    public void deleteSessions(){
        if(selectedSession.isEmpty()){
            return;
        }
        SessionRepository.getInstance().deleteSessions(selectedSession.toArray(new Long[0]));
        selectedSession.clear();
    }

    public void exportSessions(Uri uri){
        if(uri == null){
            Log.d(LOG_TAG, "export failed: null result uri");
            Toast.makeText(getApplication(), "Export failed ;(", Toast.LENGTH_LONG);
            return;
        }
        List<Long> copy = new ArrayList<>(selectedSession);
        csvExporter.export(getApplication(), copy, uri);
    }

    public String getTimeText(){
        int passedTime = (int) (System.currentTimeMillis() - sessionStartTimestamp);
        int ms = ((passedTime % 3600000) % 60000) % 1000;
        int seconds = (passedTime % 3600000) % 60000 / 1000;
        int minutes = (passedTime % 3600000) / 60000;

        return formatTime(ms, seconds, minutes);
    }

    private String formatTime(int ms, int seconds, int minutes) { return String.format("%02d",minutes) + " : " + String.format("%02d",seconds) + " : " + String.format("%03d",ms); }
}
