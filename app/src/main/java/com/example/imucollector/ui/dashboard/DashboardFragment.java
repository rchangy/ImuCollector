package com.example.imucollector.ui.dashboard;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.databinding.DataBindingUtil;

import android.media.tv.TvInputService;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
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
    private ActivityResultLauncher<Uri> resultLauncher;


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
        resultLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                homeViewModel.exportSessions(result);
            }
        });
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
        resultLauncher.launch(null);
    }

}