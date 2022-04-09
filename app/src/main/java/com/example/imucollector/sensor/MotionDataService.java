package com.example.imucollector.sensor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.imucollector.MainActivity;
import com.example.imucollector.R;
import com.example.imucollector.data.Session;
import com.example.imucollector.database.SessionRepository;
import com.example.imucollector.database.SessionDatabase;

import com.example.imucollector.ui.home.HomeFragment;

public class MotionDataService extends Service {

    private static final String LOG_TAG = "MotionDataService";
    private static boolean isRunning;

    // wakelock
    private PowerManager.WakeLock wakeLock;

    // data collecting
    private Session currentSession;
    private SensorCollectorManager scm;

    // data
    private int currentFreq;
    private int currentSessionId;
    private int currentRecordId;
    private long sessionStartTimestamp;

    // database controller
    SessionRepository sessionRepository;

    // broadcast receiver
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!intent.getAction().equals(HomeFragment.BROADCAST_INTENT_ACTION)) return;
            String action = intent.getStringExtra(HomeFragment.INTENT_EXTRA_KEY_ACTION);
            Log.d(LOG_TAG, "receive intent: " + action);
            if(action.equals(HomeFragment.INTENT_EXTRA_ACTION_START)){
                currentRecordId = intent.getIntExtra(HomeFragment.INTENT_EXTRA_KEY_RECORD_ID, -1);
                currentSessionId = intent.getIntExtra(HomeFragment.INTENT_EXTRA_KEY_SESSION_ID, -1);
                currentFreq = intent.getIntExtra(HomeFragment.INTENT_EXTRA_KEY_FREQ, -1);
                sessionStartTimestamp = intent.getLongExtra(HomeFragment.INTENT_EXTRA_KEY_TIMESTAMP, -1);
                if(currentRecordId != -1 && currentSessionId != -1 && currentFreq != -1 && sessionStartTimestamp != -1){
                    wakeLock.acquire();
                    scm.startNewSession(currentRecordId, currentSessionId, currentFreq);
                    writeSessionToDB();
                }
            }
            else if(action.equals(HomeFragment.INTENT_EXTRA_ACTION_STOP)){
                wakeLock.release();
                scm.endSession();
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        scm = new SensorCollectorManager(getApplicationContext());
        sessionRepository = SessionRepository.getInstance();
        registerReceiver(receiver, new IntentFilter(HomeFragment.BROADCAST_INTENT_ACTION));

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "imucollector::WakelockTag");
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "start service");
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    private void writeSessionToDB(){
        Session session = new Session(sessionStartTimestamp, currentFreq, currentRecordId, currentSessionId);
        SessionRepository.getInstance().insertSession(session);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        Log.d(LOG_TAG, "service stopped, unregister listener");
        isRunning = false;
        if(wakeLock.isHeld()){
            wakeLock.release();
        }
        super.onDestroy();
    }

    public static boolean isServiceRunning(){
        return isRunning;
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
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = builder.setContentTitle("Imu Data Collecting")
                .setContentText(":))")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentIntent(pendingIntent)
                .setTicker("Start collecting imu data :)))))))")
                .build();

        startForeground(1, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
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
