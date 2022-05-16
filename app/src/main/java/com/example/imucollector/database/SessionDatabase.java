package com.example.imucollector.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.imucollector.dao.AccSensorDataDao;
import com.example.imucollector.dao.GyroSensorDataDao;
import com.example.imucollector.dao.SessionDao;
import com.example.imucollector.data.AccSensorData;
import com.example.imucollector.data.GyroSensorData;
import com.example.imucollector.data.Session;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Session.class, AccSensorData.class, GyroSensorData.class}, version = 2)
public abstract class SessionDatabase extends RoomDatabase {
    private static final String DB_NAME = "sessionDatabase.db";
    private static volatile SessionDatabase instance;
    private static final int NUMBER_OF_THREADS = 1;
    static final ExecutorService pool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    protected SessionDatabase() { };
    public static synchronized SessionDatabase getInstance(Context context){
        if(instance == null){
            instance = create(context);
        }
        return instance;
    }

    private static SessionDatabase create(Context context){
        return Room.databaseBuilder(context, SessionDatabase.class, DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .build();
    }

    public abstract SessionDao sessionDao();
    public abstract AccSensorDataDao accSensorDataDao();
    public abstract GyroSensorDataDao gyroSensorDataDao();

    // migration
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE new_session(timestamp INTEGER NOT NULL, " +
                    "freq INTEGER NOT NULL, " +
                    "recordId INTEGER NOT NULL, " +
                    "sessionId INTEGER NOT NULL," +
                    "PRIMARY KEY(timestamp) )");
            database.execSQL("INSERT INTO new_session (timestamp, freq, recordId, sessionId) " +
                    "SELECT timestamp, freq, `record id`, `session id` FROM session");
            database.execSQL("DROP TABLE session");
            database.execSQL("ALTER TABLE new_session RENAME TO session");
        }
    };
}
