package com.example.imucollector.ui.dashboard;

import android.app.Activity;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.imucollector.R;
import com.example.imucollector.data.Session;
import com.example.imucollector.databinding.FragmentDashboardBinding;
import com.example.imucollector.ui.home.HomeViewModel;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
整理、輸出資料的 fragment（沒改預設名字所以還是叫 dashboard），UI 設計在 res/layout/fragment_dashboard.xml
從 room database 拿出 session data 並顯示在 UI
把所有資料生成 csv (這部分還沒寫完) 然後存在手機的 shared memory（我記得應該是）
然後可以把所有資料刪掉
bug: main thread 上不能存取 database
 */
public class DashboardFragment extends Fragment {
    private static final String LOG_TAG = "DashboardFragment";

    private HomeViewModel homeViewModel;
    private FragmentDashboardBinding binding;

    private TableLayout tableLayout;

    // export file
    private static final int OPEN_DOCUMENT_TREE = 1;
    ArrayList<Session> currentSessions;
    ArrayList<CheckBox> checkBoxes;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new androidx.lifecycle.ViewModelProvider((ViewModelStoreOwner) requireActivity(),  new SavedStateViewModelFactory(requireActivity().getApplication(), requireActivity())).get(HomeViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setHomeViewModel(homeViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        tableLayout = binding.tableSessionData;
        refreshTableView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void refreshTableView(){
        // get session data from view model
        tableLayout.removeAllViews();
        Session[] sessions = homeViewModel.getAllSessionData();
        currentSessions = new ArrayList<>(sessions.length);
        checkBoxes = new ArrayList<>(sessions.length);
        DateFormat df = DateFormat.getDateInstance();

        Log.d(LOG_TAG, "creating session table, total " + currentSessions.size() + " sessions");

        // first row
        TableRow row1 = new TableRow(getContext());
        TextView tvBlank = new TextView(getContext());
        tvBlank.setText("");
        TextView tvRecordId = new TextView(getContext());
        tvRecordId.setText("Record Id");
        TextView tvSessionId = new TextView(getContext());
        tvSessionId.setText("Session Id");
        TextView tvDate = new TextView(getContext());
        tvDate.setText("Date");

        row1.addView(tvBlank);
        row1.addView(tvRecordId);
        row1.addView(tvSessionId);
        row1.addView(tvDate);

        tableLayout.addView(row1);

        for(Session session : sessions){
            TableRow row = new TableRow(getContext());

            CheckBox checkBox = new CheckBox(getContext());
            row.addView(checkBox);


            TextView tv = new TextView(getContext());
            String recordId = String.valueOf(session.getRecordId());
            tv.setText(recordId);
            row.addView(tv);

            String sessionId = String.valueOf(session.getSessionId());
            tv.setText(sessionId);
            row.addView(tv);

            String sessionDate = df.format(session.getDate());
            tv.setText(sessionDate);
            row.addView(tv);

            tableLayout.addView(row);
            currentSessions.add(session);
            checkBoxes.add(checkBox);
        }
    }

    public void deleteSessions(View view){
        ArrayList<Session> deleteSessions = new ArrayList<>();
        for(int i = 0; i < checkBoxes.size(); i++){
            if(checkBoxes.get(i).isChecked()){
                deleteSessions.add(currentSessions.get(i));
            }
        }
        if(deleteSessions.size() > 0){
            homeViewModel.deleteSessions((Session[]) deleteSessions.toArray());
        }
    }

    public void exportSessions(View view){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        // TODO: initial uri
//        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);

//        startActivityForResult(intent, OPEN_DOCUMENT_TREE);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == OPEN_DOCUMENT_TREE && resultCode == Activity.RESULT_OK){
//            Uri uri = null;
//            if(data != null){
//                uri = data.getData();
//                homeViewModel.sessionsToCsv(uri);
//            }
//        }
//    }
}