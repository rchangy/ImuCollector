package com.example.imucollector.sensor;

import android.hardware.Sensor;

import com.example.imucollector.data.AccSensorData;
import com.example.imucollector.database.SessionRepository;

import java.util.ArrayList;
import java.util.List;

public class AccSensorCollector extends SensorCollector{

    private static final int CACHE_SIZE = 10000;

    private List<AccSensorData> cache = new ArrayList<>();

    public AccSensorCollector(Sensor sensor){
        super(sensor);
    }

    @Override
    protected void writeDB(long timestamp, float[] values) {
        cache.add(new AccSensorData(timestamp, recordId, sessionId, values[0], values[1], values[2]));
        if(cache.size() >= CACHE_SIZE){
            flushDB();
        }

    }

    @Override
    protected void flushDB() {
        SessionRepository.getInstance().insertAccData(cache.toArray(new AccSensorData[0]));
        cache = new ArrayList<>();
    }
}
