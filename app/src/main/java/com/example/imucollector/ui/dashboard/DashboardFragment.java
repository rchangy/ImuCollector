package com.example.imucollector.ui.dashboard;

import android.content.DialogInterface;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imucollector.R;
import com.example.imucollector.databinding.FragmentDashboardBinding;
import com.example.imucollector.ui.home.HomeViewModel;

public class DashboardFragment extends Fragment {
    private static final String LOG_TAG = "DashboardFragment";

    private HomeViewModel homeViewModel;
    private FragmentDashboardBinding binding;

    // session table
    private RecyclerView recyclerView;
    private SessionListAdapter adapter;
    private Button buttonDelete;
    private CheckBox checkBoxAll;

    // export file
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

        checkBoxAll = binding.checkboxAll;
        checkBoxAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = adapter.selectAll();
                checkBoxAll.setChecked(isChecked);
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void deleteSessions(){
        if(homeViewModel.getSelectedSession().isEmpty()){
            return;
        }
        AlertDialog alertDialog = createDeleteSessionAlertDialog();
        alertDialog.show();
    }

    public void exportSessions(){
        resultLauncher.launch(null);
    }

    private AlertDialog createDeleteSessionAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Selected Sessions?");
        builder.setMessage("This action cannot be undone.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                homeViewModel.deleteSessions();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return builder.create();
    }

}