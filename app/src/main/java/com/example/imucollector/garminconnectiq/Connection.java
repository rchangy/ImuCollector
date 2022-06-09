package com.example.imucollector.garminconnectiq;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.exception.InvalidStateException;
import com.garmin.android.connectiq.exception.ServiceUnavailableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Connection {
    private static final String LOG_TAG = "garminconnectiq.Connection";
    private static final String CONNECT_APP_ID = "";
    private final Context context;
    private final ConnectIQ connectIQ;

    private boolean initialized;
    private IQDevice connectedDevice;
    private IQApp connectedIQApp;
    private List<IQDevice> unusedDevices = new ArrayList<>();

    public Connection(Context context){
        initialized = false;
        connectedDevice = null;
        connectedIQApp = null;
        this.context = context;
        connectIQ = ConnectIQ.getInstance(this.context, ConnectIQ.IQConnectType.WIRELESS);
        initialize();
    }

    private void initialize(){

        if(initialized) return;
        connectIQ.initialize(context, true, new ConnectIQ.ConnectIQListener() {
            @Override
            public void onSdkReady() {
                initialized = true;
                if(connectedDevice == null){
                    getDevice();
                }
                else{
                    if(connectedIQApp == null){
                        getApp();
                    }
                }
                if(!unusedDevices.isEmpty()){
                    try{
                        IQDevice copy[] = unusedDevices.toArray(new IQDevice[0]);
                        for(IQDevice device : copy) {
                            connectIQ.unregisterForDeviceEvents(device);
                            unusedDevices.remove(device);
                        }
                    }
                    catch (InvalidStateException e){

                    }
                }
            }

            @Override
            public void onInitializeError(ConnectIQ.IQSdkErrorStatus iqSdkErrorStatus) {
                initialized = false;
            }

            @Override
            public void onSdkShutDown() {
                initialized = false;
            }
        });
    }

    public void getDevice(){
        if(!initialized){
            initialize();
            return;
        }
        if(connectedDevice != null) return;
        try{
            List<IQDevice> devices = connectIQ.getConnectedDevices();
            if(devices != null && !devices.isEmpty()){
                for(IQDevice device : devices){
                    IQDevice.IQDeviceStatus status = connectIQ.getDeviceStatus(device);
                    if(status == IQDevice.IQDeviceStatus.CONNECTED){
                        registerDevice(device);
                    }
                    if(connectedDevice != null) break;
                }
            }
            else{
                Toast.makeText(context, "No connected device found", Toast.LENGTH_LONG).show();
            }

        }
        catch(InvalidStateException e){
            initialized = false;
            initialize();
        }
        catch(ServiceUnavailableException e){

        }
    }

    private synchronized void registerDevice(IQDevice device){
        if(connectedDevice != null) return;
        try{
            connectIQ.registerForDeviceEvents(device, new ConnectIQ.IQDeviceEventListener() {
                @Override
                public void onDeviceStatusChanged(IQDevice iqDevice, IQDevice.IQDeviceStatus iqDeviceStatus) {
                    switch (iqDeviceStatus){
                        case UNKNOWN:
                        case NOT_CONNECTED:
                        case NOT_PAIRED:
                            connectedDevice = null;
                            break;
                    }
                    connectedDevice = null;
                    connectedIQApp = null;
                    try{
                        connectIQ.unregisterForDeviceEvents(iqDevice);
                    }
                    catch(InvalidStateException e){
                        unusedDevices.add(iqDevice);
                        initialized = false;
                        initialize();
                    }
                }
            });
            connectedDevice = device;
            getApp();
        }
        catch (InvalidStateException e){
            initialized = false;
            initialize();
        }
    }

    private void getApp(){
        if(connectedDevice == null) return;
        try{
            connectIQ.getApplicationInfo(CONNECT_APP_ID, connectedDevice, new ConnectIQ.IQApplicationInfoListener() {
                @Override
                public void onApplicationInfoReceived(IQApp iqApp) {
                    connectedIQApp = iqApp;
                    Toast.makeText(context, "Connect IQ device connected :)", Toast.LENGTH_SHORT);
                }

                @Override
                public void onApplicationNotInstalled(String s) {
                    //TODO: install app dialog
                    //connectIQ.openStore(MY_APPLICATION_ID);
                }
            });
        }
        catch (InvalidStateException e){
            initialized = false;
            initialize();
        }
        catch (ServiceUnavailableException e){

        }
    }

    public boolean isConnected(){
        return connectedDevice != null && connectedIQApp != null;
    }

    //TODO: on app destroyed
    //TODO: disconnect device
}
