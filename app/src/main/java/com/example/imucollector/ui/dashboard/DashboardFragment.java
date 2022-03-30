package com.example.imucollector.ui.dashboard;

import android.app.Activity;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;

import android.media.tv.TvInputService;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imucollector.R;
import com.example.imucollector.data.Session;
import com.example.imucollector.databinding.FragmentDashboardBinding;
import com.example.imucollector.ui.home.HomeViewModel;
import com.opencsv.CSVWriter;

import org.w3c.dom.Text;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
整理、輸出資料的 fragment（沒改預設名字所以還是叫 dashboard），UI 設計在 res/layout/fragment_dashboard.xml
從 room database 拿出 session data 並顯示在 UI
把所有資料生成 csv (這部分還沒寫完) 然後存在手機的 shared memory
然後可以把指定資料刪掉
 */
public class DashboardFragment extends Fragment {
    private static final String LOG_TAG = "DashboardFragment";

    private HomeViewModel homeViewModel;
    private FragmentDashboardBinding binding;

    // session table
    private RecyclerView recyclerView;
    private SessionListAdapter adapter;
    private Button buttonDelete;

    // export file
    private static final int OPEN_DOCUMENT_TREE = 1;
    private Button buttonExport;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new androidx.lifecycle.ViewModelProvider((ViewModelStoreOwner) requireActivity(),  new SavedStateViewModelFactory(requireActivity().getApplication(), requireActivity())).get(HomeViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false);
        binding.setHomeViewModel(homeViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonExport = binding.buttonExport;
        buttonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportSessions();
            }
        });
        buttonDelete = binding.buttonDelete;
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSessions();
            }
        });

        recyclerView = binding.recyclerView;
        adapter = new SessionListAdapter(new SessionListAdapter.SessionDiff(), homeViewModel.getSelectedSession());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        homeViewModel.getAllSessions().observe(getViewLifecycleOwner(), sessions -> {
            adapter.submitList(sessions);
        });
//        new RefreshSessionTask().execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void deleteSessions(){
        homeViewModel.deleteSessions();
    }

    public void exportSessions(){
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

//    private class RefreshSessionTask extends AsyncTask<Void, Void, Void> {
//        Session[] sessions;
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            tableLayout.removeAllViews();
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            sessions = homeViewModel.getAllSessionData();
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void unused) {
//            super.onPostExecute(unused);
//            if(sessions != null) refreshTableView(sessions);
//        }
//    }

//    private class DeleteSessionTask extends AsyncTask<Session, Void, Void>{
//        Session[] remainingSessions;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            tableLayout.removeAllViews();
//        }
//
//        @Override
//        protected Void doInBackground(Session... sessions) {
//            homeViewModel.deleteSessions(sessions);
//            remainingSessions = homeViewModel.getAllSessionData();
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void unused) {
//            super.onPostExecute(unused);
//            if(remainingSessions != null) refreshTableView(remainingSessions);
//        }
//    }

}