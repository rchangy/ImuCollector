package com.example.imucollector.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.example.imucollector.database.SessionDao;

import java.util.ArrayList;
import java.util.List;

public class SensorCollectorManager {
    private final Context context;
    private SensorManager sensorManager;
    private List<SensorCollector> sensorCollectors = new ArrayList<>();


    private int currentRecordId;
    private int currentSessionId;
    private int currentFreq;
    private int sampleRateUs;

    public SensorCollectorManager(Context context){
        this.context = context;
        getSensorCollectors();
    }

    private void getSensorCollectors(){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorCollectors.add(new AccSensorCollector(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)));
        sensorCollectors.add(new GyroSensorCollector(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)));
    }


    public void startNewSession(int currentRecordId, int currentSessionId, int currentFreq){
        this.currentRecordId = currentRecordId;
        this.currentSessionId = currentSessionId;
        this.currentFreq = currentFreq;
        sampleRateUs = 1000000 / currentFreq;
        registerAllSensors();
    }

    public void endSession(){
        unregisterAllSensors();
        for(SensorCollector sc : sensorCollectors){
            sc.endSession();
        }
    }

    private void registerAllSensors(){
        for(SensorCollector sc : sensorCollectors){
            sensorManager.registerListener(sc, sc.sensor, sampleRateUs, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    private void unregisterAllSensors(){
        for(SensorCollector sc : sensorCollectors){
            sensorManager.unregisterListener(sc);
        }
    }

}
