package spaceapps.buetzenith.trashtrack.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import spaceapps.buetzenith.trashtrack.R;
import spaceapps.buetzenith.trashtrack.dagger.component.ActivityComponent;
import spaceapps.buetzenith.trashtrack.dagger.component.AppComponent;
import spaceapps.buetzenith.trashtrack.dagger.module.ActivityModule;
import spaceapps.buetzenith.trashtrack.databinding.ActivityMainBinding;
import spaceapps.buetzenith.trashtrack.service.model.TrajectoryData;
import spaceapps.buetzenith.trashtrack.utils.Utils;
import spaceapps.buetzenith.trashtrack.view.adapter.SatelliteListAdapter;
import spaceapps.buetzenith.trashtrack.view.screen.googlemap.DeviceLocationFinder;
import spaceapps.buetzenith.trashtrack.viewModel.MainViewModel;
import spaceapps.buetzenith.trashtrack.viewModel.SpaceTrackViewModel;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity:";

    public ActivityComponent activityComponent;

    ActivityMainBinding mVB;

    @Inject
    SatelliteListAdapter satelliteListAdapter;

    @Inject
    MainViewModel mainViewModel;

    @Inject
    SpaceTrackViewModel spaceTrackViewModel;

    @Inject
    DeviceLocationFinder deviceLocationFinder;

    //views datas
    public String activeSatCode = "ISS (ZARYA)"; //this is iss by default but will change when another one selected
    public Map<String, ArrayList<TrajectoryData>> allSatDatFromSSCMap = new HashMap<>();

    //views
    private NavController navController;
    private Integer activeFragmentId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        // init view
        mVB = ActivityMainBinding.inflate(super.getLayoutInflater());
        setContentView(mVB.getRoot());

        // init dagger
        if (activityComponent == null)
            initDaggerActivityComponent();
        activityComponent.inject(this);

        // data
        fetchInitialData();

        // nav controller
        navController = Navigation.findNavController(this, R.id.mainActv_nav_host_frag);

        // calculate 6 hour risk analysis
        riskAnalysis();
    }


    public void initDaggerActivityComponent() {
        AppComponent appComponent = ((App) getApplication()).getAppComponent();
        activityComponent = appComponent.activityComponentBuilder()
                .activityModule(new ActivityModule(this))
                .build();
    }


    private void riskAnalysis() {
        Log.d(TAG, "getDeviceLocation: ");
        deviceLocationFinder.requestDeviceLocation(new DeviceLocationFinder.OnDeviceLocationFoundListener() {
            @Override
            public void onDeviceLocationFound(LatLng latLng) {
                Log.d(TAG, "onDeviceLocationFound: " + latLng);

            }
        });
    }

    private void fetchInitialData() {
        //data from nasa ssc api
        ArrayList<String> satCodeList = new ArrayList<>(Collections.singletonList(
                "sun"
        ));
        fetchSatDataFromSSC(satCodeList);
        spaceTrackViewModel.login();
    }

    /**
     * name of the function suggest what it does
     *
     * @param satCodeList
     */
    public void fetchSatDataFromSSC(ArrayList<String> satCodeList) {
        //fetch satellite data from nasa SSC
        long t = System.currentTimeMillis();
        String fromTime = Utils.getTimeAsString(t - (1000 * 60 * 5)); //before 5 min of current timestamp
        String toTime = Utils.getTimeAsString(t + 1000 * 60 * 20); //after 20 min of current timestamp
        Log.d(TAG, "SSC API: " + fromTime + " && " + toTime);
        mainViewModel.getLocationOfSatellite(satCodeList, fromTime, toTime)
                .observe(this, satelliteBasicDataMap -> {
                    Log.d(TAG, "fetchSatDataFromSSC: number of sat data in the map:" + satelliteBasicDataMap.size());
                    this.allSatDatFromSSCMap = satelliteBasicDataMap;
                });
    }


    public void setStartDestination(Integer fragmentId) {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.mainActv_nav_host_frag);
        NavInflater inflater = navHostFragment.getNavController().getNavInflater();
        NavGraph navGraph = inflater.inflate(R.navigation.main_nav_graph);
        navGraph.setStartDestination(fragmentId);
        navHostFragment.getNavController().setGraph(navGraph);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult: code: " + requestCode);
        Log.d(TAG, "onRequestPermissionsResult: length: " + permissions.length);
        for (String s :
                permissions) {
            Log.d(TAG, "onRequestPermissionsResult: " + s);
            if (s.equals("android.permission.ACCESS_FINE_LOCATION")) {
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
        }
    }
}