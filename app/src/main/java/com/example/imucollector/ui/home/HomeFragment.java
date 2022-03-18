package com.example.imucollector.ui.home;

import android.app.Activity;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
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

/*
計時器畫面 fragment，UI 設計在 res/layout/fragment_home.xml
number picker 調整 record id
slider 調整 sample rate
按鈕開始/結束錄製，fragment 只負責更新 UI 上的時間字串
 */

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
    private Intent intent;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider((ViewModelStoreOwner) requireActivity(), new SavedStateViewModelFactory(requireActivity().getApplication(), requireActivity())).get(HomeViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setHomeViewModel(homeViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        sliderFreq = binding.sliderFreq;
        sliderFreq.addOnChangeListener(changeListener);
        numberPicker = binding.numberPickerRecordId;
        numberPicker.setOnValueChangedListener(changeListenerNumberPicker);
        numberPicker.setMaxValue(NUMBER_PICKER_MAX_VALUE);
        numberPicker.setMinValue(NUMBER_PICKER_MIN_VALUE);
        numberPicker.setValue(homeViewModel.currentRecordId.getValue());
        buttonTimer = binding.buttonTimer;
        buttonTimer.setOnClickListener(
                view1 -> {
                    homeViewModel.startStopTimer();
                    if(homeViewModel.isCollecting.getValue()){
                        Log.d(LOG_TAG, "start timer");
                        intent = new Intent(getContext(), MotionDataService.class);
                        intent.putExtra("freq", homeViewModel.currentFreq.getValue());
                        intent.putExtra("recordId", homeViewModel.currentRecordId.getValue());
                        intent.putExtra("sessionId", homeViewModel.currentSessionId.getValue());
//                            getContext().startForegroundService(intent);
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
//                            getContext().stopService(intent);
                        homeViewModel.timerText.setValue("00:00:000");
                        timer.cancel();
                        timer = new Timer();
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "fragment resume");
        if(homeViewModel.isCollecting.getValue()){
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}