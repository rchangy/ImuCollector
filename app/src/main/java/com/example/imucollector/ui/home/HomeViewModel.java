package com.example.imucollector.ui.home;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.SavedStateHandle;
import androidx.preference.PreferenceManager;

import com.example.imucollector.R;
import com.example.imucollector.data.Session;
import com.example.imucollector.database.SessionRepository;
import com.example.imucollector.export.CsvExporter;
import com.example.imucollector.service.MotionDataService;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel{
    private static final String LOG_TAG = "HomeViewModel";

    private final SharedPreferences sharedPref;
    private final SharedPreferences.Editor editor;

    // shared preference keys
    private final String PREFERENCE_FILE_KEY_RECORD_ID;
    private final String PREFERENCE_FILE_KEY_SESSION_ID;
    private final String PREFERENCE_FILE_KEY_SAMPLE_RATE;
    private final String PREFERENCE_FILE_KEY_TIMESTAMP;
    private final String PREFERENCE_FILE_KEY_IS_COLLECTING;

    // live data
    public MutableLiveData<Integer> currentSampleRate;
    public MutableLiveData<Integer> currentSessionId;
    public MutableLiveData<Integer> currentRecordId;
    public MutableLiveData<Boolean> isCollecting;
    public MutableLiveData<String> startStopTimerButtonText = new MutableLiveData<>();

    // timer ui
    public MutableLiveData<String> timerText = new MutableLiveData<>("00 : 00 : 000");
    private long sessionStartTimestamp;

    // export
    private final CsvExporter csvExporter = new CsvExporter();
    private final List<Long> selectedSession = new ArrayList<>();

    // service intent
    private Intent intentService;
    public final String INTENT_EXTRA_KEY_RECORD_ID;

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if(s.equals(PREFERENCE_FILE_KEY_RECORD_ID)){
                setCurrentRecordId(sharedPreferences.getInt(s, 0));
            }
            else if(s.equals(PREFERENCE_FILE_KEY_SESSION_ID)){
                setCurrentSessionId(sharedPreferences.getInt(s, 0));
            }
            else if(s.equals(PREFERENCE_FILE_KEY_TIMESTAMP)){
                setSessionStartTimestamp(sharedPreferences.getLong(s, 0));
            }
            else if(s.equals(PREFERENCE_FILE_KEY_SAMPLE_RATE)){
                setCurrentSampleRate(sharedPreferences.getInt(s, 0));
            }
            else if(s.equals(PREFERENCE_FILE_KEY_IS_COLLECTING)){
                setIsCollecting(sharedPreferences.getBoolean(s, false));
            }
        }
    };

    public HomeViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
        editor = sharedPref.edit();
        sharedPref.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        PREFERENCE_FILE_KEY_RECORD_ID = getApplication().getString(R.string.shared_pref_record_id);
        PREFERENCE_FILE_KEY_SESSION_ID = getApplication().getString(R.string.shared_pref_session_id);
        PREFERENCE_FILE_KEY_SAMPLE_RATE = getApplication().getString(R.string.shared_pref_sample_rate);
        PREFERENCE_FILE_KEY_TIMESTAMP = getApplication().getString(R.string.shared_pref_timestamp);
        PREFERENCE_FILE_KEY_IS_COLLECTING = getApplication().getString(R.string.shared_pref_is_collecting);

        INTENT_EXTRA_KEY_RECORD_ID = getApplication().getString(R.string.intent_extra_key_record_id);

        load();
        SessionRepository.getInstance().init(getApplication());

    }

    public void load(){
        int defaultFreq = 60;
        currentSampleRate = new MutableLiveData<>();
        setCurrentSampleRate(sharedPref.getInt(PREFERENCE_FILE_KEY_SAMPLE_RATE, defaultFreq));
        currentSessionId = new MutableLiveData<>();
        setCurrentSessionId(sharedPref.getInt(PREFERENCE_FILE_KEY_SESSION_ID, 0));
        currentRecordId = new MutableLiveData<>();
        setCurrentRecordId(sharedPref.getInt(PREFERENCE_FILE_KEY_RECORD_ID, 0));
        setSessionStartTimestamp(sharedPref.getLong(PREFERENCE_FILE_KEY_TIMESTAMP, 0));
        isCollecting = new MutableLiveData<>();
        setIsCollecting(sharedPref.getBoolean(PREFERENCE_FILE_KEY_IS_COLLECTING, false));
        setIsCollecting(false);
        editor.putBoolean(PREFERENCE_FILE_KEY_IS_COLLECTING, false);
        editor.apply();
        Log.d(LOG_TAG, "load data, is collecting: " + isCollecting.getValue());
    }

    public void save(){
        editor.putInt(PREFERENCE_FILE_KEY_RECORD_ID, currentRecordId.getValue());
        editor.putInt(PREFERENCE_FILE_KEY_SAMPLE_RATE, currentSampleRate.getValue());
        editor.apply();
    }

    // for slider on change
    public void setCurrentSampleRate(int freq){
        currentSampleRate.setValue(freq);
    }

    public void setCurrentSessionId(int id){
        currentSessionId.setValue(id);
    }

    public void setCurrentRecordId(int id){
        currentRecordId.setValue(id);
    }

    private void setIsCollecting(boolean collecting){
        isCollecting.setValue(collecting);
        if(collecting){
            startStopTimerButtonText.setValue("Stop");
        }
        else{
            startStopTimerButtonText.setValue("Start");
        }
    }

    private void setSessionStartTimestamp(long ts){
        sessionStartTimestamp = ts;
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
            getApplication().stopService(intentService);
        }
        else{
            // start timer
            save();
            intentService = new Intent(getApplication(), MotionDataService.class);
            intentService.putExtra(INTENT_EXTRA_KEY_RECORD_ID, currentRecordId.getValue());
            getApplication().startService(intentService);
        }
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
        int passedTime = (int) (System.currentTimeMillis() - getSessionStartTimestamp());
        int ms = ((passedTime % 3600000) % 60000) % 1000;
        int seconds = (passedTime % 3600000) % 60000 / 1000;
        int minutes = (passedTime % 3600000) / 60000;

        return formatTime(ms, seconds, minutes);
    }

    private String formatTime(int ms, int seconds, int minutes) { return String.format("%02d",minutes) + " : " + String.format("%02d",seconds) + " : " + String.format("%03d",ms); }

    @Override
    protected void onCleared() {
        if(isCollecting.getValue()){
            startStopTimer();
        }
        save();
        sharedPref.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        SessionRepository.getInstance().shutDownDatabase();
        super.onCleared();
    }
}
