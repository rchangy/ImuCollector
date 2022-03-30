package com.example.imucollector.sensor;

import android.hardware.Sensor;

import com.example.imucollector.data.GyroSensorData;
import com.example.imucollector.database.SessionRepository;

import java.util.ArrayList;
import java.util.List;

public class GyroSensorCollector extends SensorCollector{
    private static final int CACHE_SIZE = 10000;

    private List<GyroSensorData> cache = new ArrayList<>();

    public GyroSensorCollector(Sensor sensor){
        super(sensor);
    }

    @Override
    protected void writeDB(long timestamp, float[] values) {
        cache.add(new GyroSensorData(timestamp, recordId, sessionId, values[0], values[1], values[2]));
        if(cache.size() >= CACHE_SIZE){
            flushDB();
        }
    }
    @Override
    protected void flushDB() {
        SessionRepository.getInstance().insertGyroData(cache.toArray(new GyroSensorData[0]));
        cache = new ArrayList<>();
    }

}
