package com.example.imucollector;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.imucollector.database.SessionRepository;
import com.example.imucollector.sensor.MotionDataService;
import com.example.imucollector.ui.home.HomeViewModel;

import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.imucollector.databinding.ActivityMainBinding;

/*
主畫面，UI 設計在 res/layout/activity_main.xml，預設包含一個 fragment container 和下面的 appbar(用來切換 fragment container 的內容)
 */

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private HomeViewModel homeViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        startService(new Intent(this, MotionDataService.class));
        homeViewModel = new ViewModelProvider((ViewModelStoreOwner) this, new SavedStateViewModelFactory(getApplication(), this)).get(HomeViewModel.class);
    }

    @Override
    protected void onPause() {
        homeViewModel.save();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, MotionDataService.class));
        SessionRepository.getInstance().onActivityDestroyed();
        super.onDestroy();
    }
}