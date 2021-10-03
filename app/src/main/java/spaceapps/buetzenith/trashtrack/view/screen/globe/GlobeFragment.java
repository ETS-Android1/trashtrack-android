package spaceapps.buetzenith.trashtrack.view.screen.globe;

import android.animation.ValueAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.TransitionInflater;

import android.os.Handler;
import android.util.Log;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.neosensory.tlepredictionengine.Tle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import gov.nasa.worldwind.shape.Ellipse;
import spaceapps.buetzenith.trashtrack.R;
import spaceapps.buetzenith.trashtrack.databinding.FragmentGlobeBinding;
import spaceapps.buetzenith.trashtrack.experimental.AtmosphereLayer;
import spaceapps.buetzenith.trashtrack.service.model.DebrisFragment;
import spaceapps.buetzenith.trashtrack.service.model.Satellite;
import spaceapps.buetzenith.trashtrack.service.model.Trajectory;
import spaceapps.buetzenith.trashtrack.service.model.TrajectoryData;
import spaceapps.buetzenith.trashtrack.utils.DebrisCatalog;
import spaceapps.buetzenith.trashtrack.utils.LatLngInterpolator;
import spaceapps.buetzenith.trashtrack.utils.tle.TleToGeo;
import spaceapps.buetzenith.trashtrack.view.adapter.SatelliteListAdapter;
import spaceapps.buetzenith.trashtrack.view.custom.DateTimerPickerBottomDialog;
import spaceapps.buetzenith.trashtrack.view.screen.googlemap.DeviceLocationFinder;
import spaceapps.buetzenith.trashtrack.viewModel.CelestrackViewModel;
import spaceapps.buetzenith.trashtrack.viewModel.MainViewModel;
import spaceapps.buetzenith.trashtrack.view.MainActivity;
import gov.nasa.worldwind.Navigator;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.shape.Label;
import gov.nasa.worldwind.shape.TextAttributes;


public class GlobeFragment extends Fragment implements Choreographer.FrameCallback {

    public static final String TAG = "GlobeFragment:";
    public static final int ALT = 40000 * 1000;

    public long timeIntervalBetweenTwoData = 20 * 1000; //20 sec

    //for accessing common method of all the fragments
    MainActivity mainActivity;

    private FragmentGlobeBinding mVB;

    @Inject
    DeviceLocationFinder deviceLocationFinder;

    @Inject
    CelestrackViewModel celestrackViewModel;

    @Inject
    DateTimerPickerBottomDialog dateTimerPickerBottomDialog;

    @Inject
    MainViewModel mainViewModel;

    @Inject
    WorldWindow worldWindow;

    private RenderableLayer renderableLayer; //layer in which we can render our satellites and locations

    //private Placemark lastplackmark;
    private boolean scaleUp = false;

    private DebrisCatalog.Debris debris;

    //day night Animation settings
    private Location sunLocation = new Location(1.6, 18.6);
    //protected double cameraDegreesPerSecond = 2.0;
    protected double lightLatDegreesPerSec = (0 + 15 / 60 + 0.1 / 3600) / 60; //dif 0° 15' 00.1" direction west
    protected double lightLngDegreesPerSec = (0 + 0 / 60 + 1 / 3600) / 60; //dif 0° 00' 01.0" south
    protected long lastFrameTimeNanos;
    private AtmosphereLayer atmosphereLayer;
    protected boolean fragmentPaused;

    //for currently tracked satellite
    public String prevSatCode = "";
    public long intervalInSec = 10; //in seconds
    public boolean satelliteMoving = false; //after camera moved to activated satellite position start moving the satellite
    public ValueAnimator activeCameraValueAnimator; //move the camera to animatedly


    // timeline
    private Date currentActiveDate = new Date(System.currentTimeMillis());


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        super.setEnterTransition(inflater.inflateTransition(R.transition.slide_in_from_right));

        //make the activity landscape for this fragment
        if(getArguments()!=null){
            debris = new Gson()
                    .fromJson(getArguments().getString("debris"), DebrisCatalog.Debris.class);
        }

        mainActivity = (MainActivity) this.getActivity();
        if (mainActivity.activityComponent == null)
            mainActivity.initActivityComponent();
        mainActivity.activityComponent.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        //views
        mVB = FragmentGlobeBinding.inflate(inflater, container, false);

        mVB.globeFrameLayout.addView(this.worldWindow);
        return mVB.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");

        mVB.backButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
        });

        // Create a layer to display the  labels and satellites
        renderableLayer = new RenderableLayer();
        worldWindow.getLayers().addLayer(renderableLayer);
        worldWindow.getNavigator().setAltitude(ALT);


        deviceLocationFinder.requestDeviceLocation(latLng -> {
            //design the label
            TextAttributes textAttributes = new TextAttributes();
            textAttributes.setTextColor(new Color(0, 0, 0, 1));  //black
            textAttributes.setOutlineColor(new Color(1, 1, 1, 1)); //white
            textAttributes.setOutlineWidth(5);
            textAttributes.setTextOffset(Offset.bottomRight());

            Position devicePosition = new Position(latLng.latitude, latLng.longitude, 0);
            Label label = new Label(devicePosition, "Bangladesh", textAttributes);
            label.setRotation(WorldWind.RELATIVE_TO_GLOBE); //will rotate when we will rotate the globe
            renderableLayer.addRenderable(label); // add the label to layer
        });

        initDateTimePicker();

        initDebrisViewData();
    }

    private void initDateTimePicker() {
        mVB.dateTimePickerFab.setOnClickListener(v -> {
            dateTimerPickerBottomDialog.show(mainActivity.getSupportFragmentManager(), "TAG");
        });

        dateTimerPickerBottomDialog.setOnDateTimeSelectedListener(date -> {
            this.currentActiveDate = date;
            updateCurrentDateTimeUI();
            //plotDebrisList();
        });
    }

    private void initDebrisViewData(){
        mVB.debrisNameTv.setText(debris.name);
        String origin = "Origin: "+debris.origin;
        mVB.originTv.setText(origin);
        mVB.geoDataTv.setVisibility(View.GONE);

        updateCurrentDateTimeUI();
    }

    private void updateCurrentDateTimeUI(){
        // https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss a, dd MMM,yyyy");
        String currentTime = "Current time: "+ simpleDateFormat.format(currentActiveDate);
        mVB.dateTv.setText(currentTime);
    }


    /**
     * Resumes the WorldWindow's rendering thread
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        this.worldWindow.onResume(); // resumes a paused rendering thread

        // Resume the day-night cycle animation.
        this.fragmentPaused = false;
        this.lastFrameTimeNanos = 0;
        Choreographer.getInstance().postFrameCallback(this);

        //fetch all the data from mainViewModel
        getInitialData();
    }

    /**
     * Pauses the WorldWindow's rendering thread
     */
    @Override
    public void onPause() {
        super.onPause();
        // Stop running the night animation when this activity is paused.
        if (activeCameraValueAnimator != null
                && activeCameraValueAnimator.isRunning())
            activeCameraValueAnimator.end();

        Choreographer.getInstance().removeFrameCallback(this);
        this.fragmentPaused = true;
        this.satelliteMoving = false;
        this.lastFrameTimeNanos = 0;
        this.worldWindow.onPause();
        Log.w(TAG, " globe fragment paused and detached ");
        mVB.globeFrameLayout.removeView(this.worldWindow);
    }

    /**
     * fetch all the necessary data from mainViewModel
     */
    public void getInitialData() {
        locateSun();
        // when selected satellite is changed
        // we init the new satellite data
        //satelliteListAdapter.setSelectedSatelliteUpdateListener(this::initSatPosition);
        celestrackViewModel.getTrajectoryTLE(debris)
                .observe(this, debrisFragmentList -> {
                    if(debrisFragmentList!=null)
                        Log.d(TAG, "getDebrisTrajectoryData: total fragments: "+debrisFragmentList.size());
                    for (DebrisFragment df : debrisFragmentList) {
                        initSatPosition(df);
                    }
                });
    }


    /**
     * This method get sun data from the map that is got from Nasa SSC web service
     * Then locate the sun at its sub solar point to make day night animation
     * called from fetchSatDataFromSSE method
     */
    public void locateSun() {
        ArrayList<TrajectoryData> sunDataList = mainActivity.allSatDatFromSSCMap.get("sun");

        if (sunDataList == null || sunDataList.size() < 2)
            return;

        long timeDiff = (sunDataList.get(1).getTimestamp() - sunDataList.get(0).getTimestamp()) / 1000; //in sec

        long currentTimestamp = System.currentTimeMillis();
        long duration = (currentTimestamp - sunDataList.get(0).getTimestamp()) / 1000;
        double percent = (double) duration / timeDiff;

        int currentIdx = (int) percent;
        currentIdx = Math.max(currentIdx, 0);

        Log.w(TAG, " init sun position : currentIdx:" + currentIdx);
        percent -= currentIdx;

        TrajectoryData startData = sunDataList.get(currentIdx);
        TrajectoryData secondData = sunDataList.get(1 + currentIdx);

        double lat = startData.getLat() * (1 - percent) + percent * secondData.getLat();
        double lng = (1 - percent) * startData.getLng() + percent * secondData.getLng();

        sunLocation = new Location(lat, lng);
        Log.w(TAG, "subSolarPoint: " + "sun lat:" + lat + " sun lng: " + lng);
        //dummy sun location
        //for day-night feature add the sun location
        atmosphereLayer = new AtmosphereLayer();
        atmosphereLayer.setLightLocation(sunLocation);
        worldWindow.getLayers().addLayer(atmosphereLayer);
    }

    /**
     * locate the satellite and navigator camera accordingly after satellite data is initialized
     * called from fetchSatDataFromSSE method
     */
    public void initSatPosition(DebrisFragment debrisFragment) {
        Log.d(TAG, "initSatPosition: satellite name: " + debrisFragment.name);

      /*  satelliteMoving = false;
        if (activeCameraValueAnimator != null && activeCameraValueAnimator.isRunning()) {
            activeCameraValueAnimator.end();
            Log.w(TAG, " stopped running camera value animator");
        }*/

        //Tle tle = satellite.extractTle();
        deviceLocationFinder.requestDeviceLocation(devicesLatLng -> {
            Trajectory startPoint = TleToGeo.getPosition(debrisFragment.extractTle(), System.currentTimeMillis(), devicesLatLng);

            double lat = startPoint.getLat();
            double lng = startPoint.getLng();
            double altitude = startPoint.getHeight()*1000; // meter

            Ellipse ellipse = new Ellipse(new Position(lat, lng, altitude), 100*1000, 100*1000);

            renderableLayer.addRenderable(ellipse);

           /* // move camera to new selected satellite position in 500 milli seconds
            moveCamera(latLng, altitude, 500);

            new Handler().postDelayed(() -> {
                satelliteMoving = true;
                lastTimeStamp = System.currentTimeMillis();
                animateCamera();
            }, 500);*/
        });
    }

    private long lastTimeStamp;

    private void animateCamera() {
        if (!satelliteMoving) { // thread finish condition.
            return;
        }
        lastTimeStamp = lastTimeStamp + 60 * 60 * 1000;
        // calculate new trajectory data
        //Trajectory data = getSatelliteTrajectoryData(lastTimeStamp);

        // move camera to new position with predefined timestamp
      //  moveCamera(data.getLatLng(), data.getHeight(), timeIntervalBetweenTwoData);

        // recursively animate again after pre specified timestamp.
        new Handler().postDelayed(this::animateCamera, timeIntervalBetweenTwoData);
    }

    private Trajectory getSatelliteTrajectoryData(long timestamp, Tle tle) {
        Log.d(TAG, "updateTrajectoryData: ");
        return TleToGeo.getPosition(tle, timestamp, deviceLocationFinder.getDeviceLatLng());
    }


    /**
     * @param moveDuration duration in milli seconds
     */
    public void moveCamera(LatLng newtLatLng, double alt, long moveDuration) {
        // current location of the satellite
        LatLng currentLatLng = new LatLng(
                worldWindow.getNavigator().getLatitude(),
                worldWindow.getNavigator().getLongitude()
        );
        double currentCameraHeightInKm = this.getCurrentAltitudeInKm(); //converts to km

        // animator to animate the satellite
        activeCameraValueAnimator = ValueAnimator.ofFloat(
                (float) currentLatLng.latitude,
                (float) newtLatLng.latitude);

        activeCameraValueAnimator.setInterpolator(new LinearInterpolator());

        activeCameraValueAnimator.addUpdateListener(animation -> {
            if (fragmentPaused)
                return;
            float fraction = animation.getAnimatedFraction();
            LatLng interpolatedLatLng = LatLngInterpolator.interpolate(fraction, currentLatLng, newtLatLng);
            double altitude = alt + (alt - currentCameraHeightInKm) * fraction;

            worldWindow.getNavigator().setLatitude(interpolatedLatLng.latitude);
            worldWindow.getNavigator().setLongitude(interpolatedLatLng.longitude);
            worldWindow.getNavigator().setAltitude(Math.max(ALT, altitude * 1000)); //converts to meter agian
        });
        activeCameraValueAnimator.setDuration(moveDuration);
        activeCameraValueAnimator.start();
    }


    private double getCurrentAltitudeInKm() {
        return worldWindow.getNavigator().getAltitude() / 1000.0;
    }


    /**
     * provide us every frame which helps to animate the satellite smoothly
     * idea taken from World wind api example of day night animation
     *
     * @param frameTimeNanos
     */
    @Override
    public void doFrame(long frameTimeNanos) {
        if (this.lastFrameTimeNanos != 0 && atmosphereLayer != null) {
            // Compute the frame duration in seconds.
            double frameDurationSeconds = (frameTimeNanos - this.lastFrameTimeNanos) * 1.0e-9;
            // double cameraDegrees = (frameDurationSeconds * this.cameraDegreesPerSecond);

            // find lat and lng difference in degree
            double lightLatDiffDegrees = (frameDurationSeconds * this.lightLatDegreesPerSec);
            double lightLngDiffDegrees = (frameDurationSeconds * this.lightLngDegreesPerSec);

            this.sunLocation.latitude -= lightLatDiffDegrees;
            double lat = this.sunLocation.latitude < -90 ? -this.sunLocation.latitude - 90 : this.sunLocation.latitude;
            this.sunLocation.longitude -= lightLngDiffDegrees;
            double lng = this.sunLocation.longitude < -180 ? -this.sunLocation.longitude - 180 : this.sunLocation.longitude;

            // Move the navigator to simulate the Earth's rotation about its axis.
            Navigator navigator = worldWindow.getNavigator();
            //navigator.setLongitude(navigator.getLongitude() - cameraDegrees);

            // Move the sun location to simulate the Sun's rotation about the Earth.
            this.sunLocation.set(lat, lng);
            this.atmosphereLayer.setLightLocation(this.sunLocation);

            // Redraw the WorldWindow to display the above changes.
            this.worldWindow.requestRedraw();
        }

        // if the fragment is not stopped then call the function again to
        // continue the animation
        if (!this.fragmentPaused) {
            Choreographer.getInstance().postFrameCallback(this);
        } else {
            Choreographer.getInstance().removeFrameCallback(this);
        }

        this.lastFrameTimeNanos = frameTimeNanos;
    }
}

