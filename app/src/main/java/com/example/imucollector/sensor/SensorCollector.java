package com.example.imucollector.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public abstract class SensorCollector implements SensorEventListener {
    protected int recordId;
    protected int sessionId;
    public final Sensor sensor;

    public SensorCollector(Sensor sensor){
        this.sensor = sensor;
    }

    public void startNewSession(int recordId, int sessionId){
        this.recordId = recordId;
        this.sessionId = sessionId;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent){
        long timestamp = sensorEvent.timestamp;
        float[] values = sensorEvent.values;
        writeDB(timestamp, values);
    }

    protected abstract void writeDB(long timestamp, float[] values);

    protected abstract void flushDB();

    protected void endSession(){
        flushDB();
    }
}
