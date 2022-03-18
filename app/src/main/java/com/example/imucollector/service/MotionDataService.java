package com.example.imucollector.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.imucollector.MainActivity;
import com.example.imucollector.data.AccSensorData;
import com.example.imucollector.data.GyroSensorData;
import com.example.imucollector.data.SensorData;
import com.example.imucollector.data.Session;
import com.example.imucollector.data.SessionDao;
import com.example.imucollector.data.SessionDatabase;

import java.util.ArrayList;
import java.util.Date;

import com.example.imucollector.R;
/*
因為錄製過程中應該會關螢幕，所以把取資料的部分做成 foreground service (foreground 比較不容易被 kill 掉)
錄製開始時會啟動這個 service，用這個 service 註冊 sensor listener
取資料的部分主要在 onSensorChanged ，拿到新資料根據他的 sensor 存進 database 的不同 table
（有點擔心這樣高頻率的 database 操作會不會負擔太大，但還沒測試，另一個可能的做法是先把資料存在 vector，等 service 結束再一起存進 database，但是如果 service 中途被 kill 掉就沒了）


 */
public class MotionDataService extends Service implements SensorEventListener {

    private static final String LOG_TAG = "MotionDataService";

    // data collecting
    private Session currentSession;
//    private ArrayList<SensorData> accData;
//    private ArrayList<SensorData> gyroData;

    // sensors
    private SensorManager sensorManager;
    private Sensor accSensor;
    private Sensor gyroSensor;

    // data
    private int currentFreq;
    private int currentSessionId;
    private int currentRecordId;

    // database
    private SessionDatabase db = SessionDatabase.getInstance(getApplication());
    private SessionDao sessionDao = db.sessionDao();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO: null intent

        currentFreq = intent.getIntExtra("freq", 60);
        currentRecordId = intent.getIntExtra("recordId", 0);
        currentSessionId = intent.getIntExtra("sessionId", 0);
        if(currentRecordId == 0 || currentSessionId == 0){
            // TODO: notify user
            stopSelf();
        }
        // TODO: start foreground
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, NotificationChannel.DEFAULT_CHANNEL_ID)
                        .setContentTitle("Imu Data Collecting")
                        .setContentText(":))")
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentIntent(pendingIntent)
                        .setTicker("Start collecting imu data :)))))))")
                        .build();

        startForeground(1, notification);

        Log.i(LOG_TAG, "service started, Freq: " + currentFreq +
                ", Record Id: " + currentRecordId + ", Session Id: " + currentSessionId);

        currentSession = new Session(System.currentTimeMillis(), currentFreq, currentRecordId, currentSessionId);
        sessionDao.insertSessions(currentSession);

        // register sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        int sampleRateUs = 1000000 / currentFreq;
        sensorManager.registerListener(this, accSensor, sampleRateUs,SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gyroSensor, sampleRateUs, SensorManager.SENSOR_DELAY_GAME);

        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        Log.d(LOG_TAG, "service destroy, unregister listener");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor == accSensor){
            AccSensorData data = new AccSensorData(sensorEvent.timestamp, currentRecordId, currentSessionId,
                    sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
            Log.v(LOG_TAG, "receive acc data at " + sensorEvent.timestamp);
            sessionDao.insertAccSensorData(data);
        }
        else if(sensorEvent.sensor == gyroSensor){
            GyroSensorData data = new GyroSensorData(sensorEvent.timestamp, currentRecordId, currentSessionId,
                    sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
            Log.v(LOG_TAG, "receive gyro data at " + sensorEvent.timestamp);
            sessionDao.insertGyroSensorData(data);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }



}
