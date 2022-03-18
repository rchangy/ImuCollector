package com.example.imucollector.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
/*
room database
目前有三個 table，存 session 和兩種 session data
session table 是為了快速檢查目前有幾筆資料
session data table 就是存收集到的資料，不同 record id 和 session id 都會放在一起（暫時沒有想到更好的存法）
sensor data 存的時候會存 record id, session id, timestamp，取資料的時候可以用 timestamp sort
 */
@Database(entities = {Session.class, AccSensorData.class, GyroSensorData.class}, version = 1)
public abstract class SessionDatabase extends RoomDatabase {
    private static final String DB_NAME = "sessionDatabase.db";
    private static volatile SessionDatabase instance;
    protected SessionDatabase() { };
    public static synchronized SessionDatabase getInstance(Context context){
        if(instance == null){
            instance = create(context);
        }
        return instance;
    }

    private static SessionDatabase create(Context context){
        return Room.databaseBuilder(context, SessionDatabase.class, DB_NAME).build();
    }

    public abstract SessionDao sessionDao();

}
