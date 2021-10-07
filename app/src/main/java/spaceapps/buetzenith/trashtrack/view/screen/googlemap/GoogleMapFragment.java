package spaceapps.buetzenith.trashtrack.view.screen.googlemap;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.TransitionInflater;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.neosensory.tlepredictionengine.Tle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import spaceapps.buetzenith.trashtrack.R;
import spaceapps.buetzenith.trashtrack.databinding.FragmentGoogleMapBinding;
import spaceapps.buetzenith.trashtrack.service.model.TLEParsed;
import spaceapps.buetzenith.trashtrack.service.model.Trajectory;
import spaceapps.buetzenith.trashtrack.utils.DebrisCatalog;
import spaceapps.buetzenith.trashtrack.utils.LatLngInterpolator;
import spaceapps.buetzenith.trashtrack.utils.MapUtils;
import spaceapps.buetzenith.trashtrack.utils.tle.TleToGeo;
import spaceapps.buetzenith.trashtrack.view.custom.DateTimerPickerBottomDialog;
import spaceapps.buetzenith.trashtrack.viewModel.CelestrackViewModel;
import spaceapps.buetzenith.trashtrack.view.MainActivity;

// life cycle
// onAttach->onCreate->onCreateView->onViewCreated->onResume->initMap->onMapReady->
// getSelectedSatelliteData()-> updateTrajectoryData(), initSatellitePosition() ->
// updateSatelliteLocation()
public class GoogleMapFragment extends Fragment implements OnMapReadyCallback {
    public static final String TAG = "GoogleMapFragment:";

    //for accessing common method of all the fragments
    MainActivity mainActivity;

    @Inject
    DeviceLocationFinder deviceLocationFinder;

    private DebrisCatalog.Debris debris;


    @Inject
    CelestrackViewModel celestrackViewModel;

    @Inject
    DateTimerPickerBottomDialog dateTimerPickerBottomDialog;

    //view
    private FragmentGoogleMapBinding mVB;

    private GoogleMap mMap;
    int[] rawMapStyles = {R.raw.dark_map, R.raw.night_map, R.raw.aubergine_map, R.raw.assassins_creed_map};
   // private Map<String, Marker> debrisMarkerMap = new HashMap<>();
    private volatile Map<String, Circle> debrisObjectMap = new HashMap<>();
    private Marker movingSatelliteMarker; //satellite icon as marker
    private ArrayList<Polyline> drawnPolyLine = new ArrayList<>(); //polyline drawn to show a satellite trajectory

    //to show satellite
    public Trajectory startPoint;
    public Trajectory endPoint;
    public long timeIntervalBetweenTwoData = 20 * 1000; //20 sec
    public boolean satelliteMoving = false; //after camera moved to activated satellite position start moving the satellite
    private Handler satelliteMoveHandler;
    public ValueAnimator activeSatAnimator;
    public ViewPropertyAnimator satelliteAnimation;

    private List<TLEParsed> TLEParsedList;

    // timeline
    private Date currentActiveDate = new Date(System.currentTimeMillis());

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    GoogleMap.OnCameraIdleListener cameraIdleListener = new GoogleMap.OnCameraIdleListener() {
        @Override
        public void onCameraIdle() {
            if(TLEParsedList !=null){
               //executorService.execute(GoogleMapFragment.this::plotDebrisList);
                plotDebrisList();
            }
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        super.setEnterTransition(inflater.inflateTransition(R.transition.slide_in_from_right));

        if(getArguments()!=null){
            debris = new Gson()
                    .fromJson(getArguments().getString("debris"), DebrisCatalog.Debris.class);
        }

        mainActivity = (MainActivity) this.getActivity();
        if (mainActivity.activityComponent == null)
            mainActivity.initDaggerActivityComponent();
        mainActivity.activityComponent.inject(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        mVB = FragmentGoogleMapBinding.inflate(inflater, container, false);
        return mVB.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        satelliteMoveHandler = new Handler();

        mVB.backButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
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
            plotDebrisList();
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

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        // initialize map
        initMap();
    }

    /**
     * get the map fragment and start the MapAsync
     */
    public void initMap() {
        Log.d(TAG, "initMap: ");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.googleMap);
        try {
            mapFragment.getMapAsync(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called when map async task is finished
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this.getContext(),rawMapStyles[2]));
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.getCameraPosition();
        
        mMap.setMinZoomPreference(3);

        mMap.setOnCameraIdleListener(cameraIdleListener);

        deviceLocationFinder.requestDeviceLocation(latLng -> {
            Log.d(TAG, "onMapReady: location found: " + latLng);

            // enable user location
            enableUserLocation();

            animateCamera(latLng);

            getDebrisTrajectoryData();
        });
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(mainActivity
                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Log.d(TAG, "enableUserLocation: ");
        mMap.setMyLocationEnabled(true);
    }

    private void getDebrisTrajectoryData(){
        if(debris == null){
            return;
        }
        celestrackViewModel.getDebrisPieces(debris)
                .observe(this, debrisFragments -> {
                    this.TLEParsedList = debrisFragments;
                    if(TLEParsedList !=null){
                        Log.d(TAG, "getDebrisTrajectoryData: total fragments: "+debrisFragments.size());
                        for (int i = 0; i < debrisFragments.size(); i++) {
                            TLEParsed df = TLEParsedList.get(i);
                            df.name = i + df.name;
                        }
                        //executorService.execute(GoogleMapFragment.this::plotDebrisList);
                        plotDebrisList();
                    }
                });
    }


    private void plotDebrisList(){
        for (TLEParsed df : TLEParsedList) {
            initSpaceObjectPosition(df);
        }
    }


    private Trajectory getDebrisTrajectoryData(long timestamp, Tle tle){
       // Log.d(TAG, "getDebrisTrajectoryData: ");
        return TleToGeo.getPosition(tle, timestamp, deviceLocationFinder.getDeviceLatLng());
    }


    public void initSpaceObjectPosition(TLEParsed TLEParsed) {
        //Log.d(TAG, "initSpaceObjectPosition: inside.");
        //make null so that know line is drawn
        /*satelliteMoving = false; // finish the satellite move thread
        if (activeSatAnimator != null && activeSatAnimator.isRunning()) {
            Log.d(TAG, "Previous satellite animator is removed");
            activeSatAnimator.end();
        }*/

        // remove all the polyline drawn for previous satellite
        /*for (Polyline polyline : drawnPolyLine) {
            polyline.remove();
        }
        drawnPolyLine = new ArrayList<>();*/

        // add satellite marker in startPoint
        // init new selected satellite position.
        startPoint = getDebrisTrajectoryData(currentActiveDate.getTime(), TLEParsed.extractTle());

        Log.d(TAG, "initSpaceObjectPosition: "+ TLEParsed.name+" speed: "+startPoint.getSpeed());

        //mainActivity.runOnUiThread(()->{
            if(!isInsideCamera(startPoint.getLatLng())){
               // Log.d(TAG, "initSpaceObjectPosition: removing "+debrisFragment.name);
               removeDebris(TLEParsed.name);
            }else{
                Log.d(TAG, "initSpaceObjectPosition: adding "+ TLEParsed.name);
                addNewDebrisToGoogleMap(TLEParsed.name, startPoint.getLatLng());
            }
       // });



        //satelliteMoving = true;
       //moveSatellite(debrisFragment); // recursive function that animates the satellite
    }

    private boolean isInsideCamera(LatLng latLng){
        LatLng topRight = mMap.getProjection().getVisibleRegion().latLngBounds.northeast;
        LatLng bottomLeft = mMap.getProjection().getVisibleRegion().latLngBounds.southwest;


        double left = bottomLeft.latitude;
        double right = topRight.longitude;

        double lng = latLng.longitude;
        if(right<left){
            right+=360;
            lng = lng <0 ? lng+360 : lng;
        }

        return (bottomLeft.latitude<=latLng.latitude && latLng.latitude <= topRight.latitude)
                &&(left<=lng && lng <= right);
    }

    private void removeDebris(String debrisName){
        if(debrisObjectMap.get(debrisName)!=null){
            Circle circle =  debrisObjectMap.get(debrisName); // remove the previous marker to redraw the marker
            circle.remove();
        }
    }

    private void addNewDebrisToGoogleMap(String debrisName, LatLng latLng) {
        removeDebris(debrisName);

        // draw new satellite
        Circle circle = addDebrisCircle(latLng);
        debrisObjectMap.put(debrisName, circle);

        /*marker.setPosition(latLng);
        marker.setAnchor(0.4f, 0.4f);*/

        // animate and move camera to new lat lng
        //animateCamera(latLng);
    }

    private Circle addDebrisCircle(LatLng latLng){
        Log.d(TAG, "addDebrisCircle: ");
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .fillColor(Color.RED)
                .strokeColor(Color.RED)
                .radius(10_000);// in meter

        return mMap.addCircle(circleOptions);
    }

    /**
     * This recursive function helps to move the satellite
     */
    public void moveSatellite(TLEParsed TLEParsed) {
        if (!satelliteMoving) { // thread finish condition.
            return;
        }

        if (activeSatAnimator != null && activeSatAnimator.isRunning()) {
            activeSatAnimator.end();
        }
        // next end point
        endPoint = getDebrisTrajectoryData(System.currentTimeMillis() + timeIntervalBetweenTwoData, TLEParsed.extractTle());
        updateSatelliteLocation(endPoint, timeIntervalBetweenTwoData);

        satelliteMoveHandler.postDelayed(()->{
            moveSatellite(TLEParsed);
        }, timeIntervalBetweenTwoData);
    }

    /**
     * update the satellite location which help us to achieve the satellite animation
     */
    private void updateSatelliteLocation(Trajectory to, long durationMilli) {
        final LatLng startPosition = movingSatelliteMarker.getPosition();
        final LatLng endPosition = to.getLatLng();

        if(activeSatAnimator!=null && activeSatAnimator.isRunning()){
            activeSatAnimator.end();
        }

        activeSatAnimator = ValueAnimator.ofInt(0,(int)(durationMilli/1000.0));
        activeSatAnimator.setInterpolator(new LinearInterpolator());
        activeSatAnimator.setDuration(durationMilli);
        int prevValue = -1;
        activeSatAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private int prevValue = -1;
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (satelliteMoving) {
                    int v = (int) animation.getAnimatedValue();

                    // we will have so many call backs with the same value
                    // which freezes the screen
                    // here will handle it
                    if(!isValueUpdate(v))
                        return;

                    Log.d(TAG, "updateSatelliteLocation: value: "+v);
                    float fraction = v/(durationMilli/1000.0f);
                    Log.d(TAG, "updateSatelliteLocation: fraction: " + fraction);
                    //ease in the value
                    LatLng nextLocation = LatLngInterpolator.interpolate(fraction, startPosition, endPosition);

                    movingSatelliteMarker.setPosition(nextLocation);

                    // animateCamera(nextLocation);
                    // moveCamera(nextLocation,0f);
                    addLineBetweenTwoPoints(startPosition, nextLocation);
                }
            }

            private boolean isValueUpdate(int v){
                boolean isUpdated = prevValue != v;
                if(isUpdated){
                    prevValue = v;
                }
                return isUpdated;
            }
        });
        activeSatAnimator.start();
    }


    /**
     * Move the map camera to a certain lat lng
     *
     * @param latLng
     * @param zoom
     */
    public void moveCamera(LatLng latLng, float zoom) {
        //  Log.w(TAG,"moveCamera: moving camera to: lat: "+latLng.latitude+" , lng: "+latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    /**
     * add satellite icon as marker at a certain lat lng
     *
     * @param latLng
     * @return
     */
    private Marker addDebrisMarker(LatLng latLng) {
        Log.d(TAG, "addSatelliteAndGet: ");
        BitmapDescriptor defaultSatBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getSatelliteBitmap(mainActivity));
        // BitmapDescriptor urlSatBitmapDescriptor = getMarkerIconFromDrawable(GlobeUtils.satelliteIconMap.get(name));

        //if satellite image is not loaded from the link use the default satellite image as descriptor for marker
        BitmapDescriptor bitmapDescriptor = /*urlSatBitmapDescriptor != null ? urlSatBitmapDescriptor :*/ defaultSatBitmapDescriptor;
        return mMap.addMarker(new MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor));
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        if (drawable == null) {
            Log.d(TAG, " drawable is null");
            return null;
        }
        Log.d(TAG, " drawable is not null");

        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * animate and move camera
     *
     * @param latLng is the end destination from current position.
     */
    public void animateCamera(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(0f).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    public void addLineBetweenTwoPoints(final LatLng from, final LatLng to) {
        final PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(from);
        polylineOptions.add(to);
        polylineOptions.width(8f);
        polylineOptions.color(Color.RED);

        Polyline polyline = mMap.addPolyline(polylineOptions);
        drawnPolyLine.add(polyline);
        // animatePolyLineBetweenTwoPoints(polyline,from,to);
    }

    @Override
    public void onPause() {
        super.onPause();
        satelliteMoving = false;
        if (satelliteAnimation != null)
            satelliteAnimation.cancel();
        if (activeSatAnimator != null && activeSatAnimator.isRunning()) {
            activeSatAnimator.end();
        }
    }
}