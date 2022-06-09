package com.example.imucollector.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.imucollector.MainActivity;
import com.example.imucollector.R;
import com.example.imucollector.data.Session;
import com.example.imucollector.database.SessionRepository;

import com.example.imucollector.sensor.SensorCollectorManager;
import com.example.imucollector.ui.home.HomeViewModel;

public class MotionDataService extends Service {

    private static final String LOG_TAG = "MotionDataService";

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private String PREFERENCE_FILE_KEY_SESSION_ID;
    private String PREFERENCE_FILE_KEY_SAMPLE_RATE;
    private String PREFERENCE_FILE_KEY_TIMESTAMP;
    private String PREFERENCE_FILE_KEY_IS_COLLECTING;

    private String INTENT_EXTRA_KEY_RECORD_ID;

    private PowerManager.WakeLock wakeLock;
    private SensorCollectorManager scm;

    // session
    private int currentSampleRate;
    private int currentSessionId;
    private int currentRecordId;
    private long sessionStartTimestamp;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
        editor = sharedPref.edit();

        PREFERENCE_FILE_KEY_SESSION_ID = getApplication().getString(R.string.shared_pref_session_id);
        PREFERENCE_FILE_KEY_SAMPLE_RATE = getApplication().getString(R.string.shared_pref_sample_rate);
        PREFERENCE_FILE_KEY_TIMESTAMP = getApplication().getString(R.string.shared_pref_timestamp);
        PREFERENCE_FILE_KEY_IS_COLLECTING = getApplication().getString(R.string.shared_pref_is_collecting);

        INTENT_EXTRA_KEY_RECORD_ID = getApplication().getString(R.string.intent_extra_key_record_id);

        scm = new SensorCollectorManager(getApplicationContext());
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "imucollector::WakelockTag");
        SessionRepository.getInstance().init(getApplication());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "receive start command");
        super.onStartCommand(intent, flags, startId);

        // get session data
        boolean isCollecting = sharedPref.getBoolean(PREFERENCE_FILE_KEY_IS_COLLECTING, false);
        currentRecordId = intent.getIntExtra(INTENT_EXTRA_KEY_RECORD_ID, -1);
        currentSessionId = sharedPref.getInt(PREFERENCE_FILE_KEY_SESSION_ID, 0);
        currentSampleRate = sharedPref.getInt(PREFERENCE_FILE_KEY_SAMPLE_RATE, -1);

        if(!isCollecting && currentRecordId != -1 && currentSampleRate != -1){
            startRecording();
        }
        else{
            Log.d(LOG_TAG, "start session failed, record id: " + currentRecordId +
                    ", session id: " + currentSessionId + ", freq: " + currentSampleRate +
                    ", timestamp: " + sessionStartTimestamp);
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    private void writeSessionToDB(){
        Session session = new Session(sessionStartTimestamp, currentSampleRate, currentRecordId, currentSessionId);
        SessionRepository.getInstance().insertSession(session);
    }

    private void startRecording(){
        Log.d(LOG_TAG, "start recording");
        startForeground();
        wakeLock.acquire();
        scm.startNewSession(currentRecordId, currentSessionId, currentSampleRate);
        editor.putBoolean(PREFERENCE_FILE_KEY_IS_COLLECTING, true);
        sessionStartTimestamp = System.currentTimeMillis();
        editor.putLong(PREFERENCE_FILE_KEY_TIMESTAMP, sessionStartTimestamp);
        editor.apply();
        writeSessionToDB();
    }

    private void stopRecording(){
        Log.d(LOG_TAG, "stop recording");
        scm.endSession();
        wakeLock.release();
        int nextSessionId = (currentSessionId+1 > 1000)? 0 : currentSessionId+1;
        editor.putInt(PREFERENCE_FILE_KEY_SESSION_ID, nextSessionId);
        editor.putBoolean(PREFERENCE_FILE_KEY_IS_COLLECTING, false);
        editor.apply();
        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "service stopped, unregister listener");
//        unregisterReceiver(receiver);
        if(wakeLock.isHeld()) stopRecording();
//        sendBroadcast(new Intent(BROADCAST_INTENT_ACTION));
        SessionRepository.getInstance().shutDownDatabase();
        super.onDestroy();
    }

    private void startForeground(){
        String channelId = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            channelId = createNotificationChannel("example.imucollector", "MotionDataService");
        }
        else channelId = NotificationChannel.DEFAULT_CHANNEL_ID;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = builder.setContentTitle("Imu Data Collecting")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentIntent(pendingIntent)
                .setTicker("Imu data collecting")
                .build();
        Log.d(LOG_TAG, "start foreground");
        startForeground(1, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(channel);

        return channelId;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
