package com.example.imucollector.export;

import android.app.Application;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.example.imucollector.data.AccSensorData;
import com.example.imucollector.data.GyroSensorData;
import com.example.imucollector.data.Session;
import com.example.imucollector.database.SessionRepository;

import java.io.FileOutputStream;
import java.util.List;
import java.util.StringJoiner;

public class CsvExporter {
    private static final String LOG_TAG = "CsvExporter";
    public void export(Application app, List<Long> selectedSession, Uri uri){
        new ExportFileTask(app, selectedSession).execute(uri);
    }

    private class ExportFileTask extends AsyncTask<Uri, Void, Integer> {
        private Application app;
        private List<Long> selectedSession;
        public ExportFileTask(Application app, List<Long> selectedSession){
            this.app = app;
            this.selectedSession = selectedSession;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(app, "Start exporting", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Integer doInBackground(Uri... resultUri) {
            Uri dirUri = resultUri[0];
            DocumentFile imuCollectorDocumentFile = DocumentFile.fromTreeUri(app, dirUri);
            Log.d(LOG_TAG, imuCollectorDocumentFile.getUri().getPath());
            String currentTime = String.valueOf(System.currentTimeMillis());
            String exportFilename = "imu_" + currentTime;
            String[] header = {"Record Id", "Session Id", "Acc Timestamp", "AccX", "AccY", "AccZ", "Gyro Timestamp", "GyroX", "GyroY", "GyroZ"};
            String[] emptyEntry = {"", "", "", ""};
            Integer sessionNum;
            Session[] sessions;
            if(selectedSession.isEmpty()){
                sessions = SessionRepository.getInstance().getAllSessionsInBackground();
            }
            else{
                sessions = SessionRepository.getInstance().getSelectedSessionsInBackground(selectedSession.toArray(new Long[0]));
            }
            sessionNum = sessions.length;
            try{
                DocumentFile exportDocumentFile = imuCollectorDocumentFile.createFile("text/csv", exportFilename);
                Uri exportUri = exportDocumentFile.getUri();
                ParcelFileDescriptor pdf = app.getContentResolver().openFileDescriptor(exportUri, "w");
                FileOutputStream fileOutputStream = new FileOutputStream(pdf.getFileDescriptor());
                fileOutputStream.write(stringJoiner(header).getBytes());
                for(Session session : sessions){
                    String recordIdStr = String.valueOf(session.recordId);
                    String sessionIdStr = String.valueOf(session.sessionId);
                    AccSensorData[] accData = SessionRepository.getInstance().getSessionAccDataInBackground(session);
                    GyroSensorData[] gyroData = SessionRepository.getInstance().getSessionGyroDataInBackground(session);
                    int dataCount = Math.max(accData.length, gyroData.length);
                    Log.d(LOG_TAG, "record " + recordIdStr + " session " + sessionIdStr +  " data count = " + accData.length + " " +  gyroData.length);
                    for(int i = 0; i < dataCount; i++){
                        String[] data = new String[10];
                        data[0] = recordIdStr;
                        data[1] = sessionIdStr;
                        String[] accFormatData;
                        String[] gyroFormatData;
                        if(i < accData.length){
                            accFormatData = accData[i].formatData();
                        }
                        else accFormatData = emptyEntry;

                        if(i < gyroData.length){
                            gyroFormatData = gyroData[i].formatData();
                        }
                        else gyroFormatData = emptyEntry;

                        System.arraycopy(accFormatData, 0, data, 2, 4);
                        System.arraycopy(gyroFormatData, 0, data, 6, 4);
                        fileOutputStream.write(stringJoiner(data).getBytes());
                    }
                }
                fileOutputStream.close();
                pdf.close();
            }
            catch (Exception e){
                e.printStackTrace();
                return -1;
            }
            if(sessionNum != null){
                return sessionNum;
            }
            else
                return -1;
        }
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer == -1){
                Toast.makeText(app, "Export failed ;(", Toast.LENGTH_LONG).show();
                Log.d(LOG_TAG, "Export failed");
            }
            else{
                Toast.makeText(app, "Export succeeded :)", Toast.LENGTH_LONG).show();
                Log.d(LOG_TAG, "Export succeeded, " + integer + " sessions exported");
            }
        }

        public String stringJoiner(String[] arr) {
            StringJoiner joiner = new StringJoiner(",", "", "\n");
            for (String el : arr) {
                joiner.add(el);
            }
            return joiner.toString();
        }
    }
}

