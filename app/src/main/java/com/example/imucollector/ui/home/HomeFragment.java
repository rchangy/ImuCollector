package com.example.imucollector.ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;

import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.imucollector.R;
import com.example.imucollector.databinding.FragmentHomeBinding;
import com.example.imucollector.service.MotionDataService;
import com.google.android.material.slider.Slider;

import java.util.Timer;
import java.util.TimerTask;


public class HomeFragment extends Fragment {

    private static final String LOG_TAG = "HomeFragment";

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private Slider sliderFreq;
    private final Slider.OnChangeListener changeListener = new Slider.OnChangeListener() {
        @Override
        public void onValueChange(@androidx.annotation.NonNull Slider slider, float value, boolean fromUser) {
            homeViewModel.setCurrentFreq((int) value);
            Log.d(LOG_TAG, "slider value changed to " + value);
        }
    };

    private NumberPicker numberPicker;
    private NumberPicker.OnValueChangeListener changeListenerNumberPicker = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker numberPicker, int i, int i1) {
            homeViewModel.setCurrentRecordId(i1);
            Log.d(LOG_TAG, "number picker value changed to " + i1);
        }
    };
    private final int NUMBER_PICKER_MAX_VALUE = 1000;
    private final int NUMBER_PICKER_MIN_VALUE = 0;

    private Timer timer = new Timer();

    private Button buttonTimer;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MotionDataService.BROADCAST_INTENT_ACTION)){
                if(timer != null) {
                    // service killed by system
                    timer.cancel();
                    timer = null;
                    Toast.makeText(getContext(), "Service has been killed", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider((ViewModelStoreOwner) requireActivity(), new SavedStateViewModelFactory(requireActivity().getApplication(), requireActivity())).get(HomeViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        binding.setHomeViewModel(homeViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sliderFreq = binding.sliderFreq;
        sliderFreq.addOnChangeListener(changeListener);
        sliderFreq.setValue(homeViewModel.currentFreq.getValue());
        numberPicker = binding.numberPickerRecordId;
        numberPicker.setOnValueChangedListener(changeListenerNumberPicker);
        numberPicker.setMaxValue(NUMBER_PICKER_MAX_VALUE);
        numberPicker.setMinValue(NUMBER_PICKER_MIN_VALUE);
        numberPicker.setValue(homeViewModel.currentRecordId.getValue());
        buttonTimer = binding.buttonTimer;
        buttonTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStopTimer();
            }
        });
    }

    public void startStopTimer(){

        if(!homeViewModel.isCollecting.getValue()){

            homeViewModel.startStopTimer();
            Log.d(LOG_TAG, "start timer");
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    String text = homeViewModel.getTimeText();
                    getActivity().runOnUiThread(() -> homeViewModel.timerText.setValue(text));
                }
            }, 0, 10);
        }
        else{
            Log.d(LOG_TAG, "stop timer");
            timer.cancel();
            timer = null;
            homeViewModel.startStopTimer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "fragment resume");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MotionDataService.BROADCAST_INTENT_ACTION);
        getActivity().registerReceiver(receiver, intentFilter);

        if(homeViewModel.isCollecting.getValue()){
            Log.d(LOG_TAG, "restart timer");
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    String text = homeViewModel.getTimeText();
                    getActivity().runOnUiThread(() -> homeViewModel.timerText.setValue(text));
                }
            }, 0, 10);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "fragment pause");
        if(homeViewModel.isCollecting.getValue()){
            timer.cancel();
        }
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}