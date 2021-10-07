package spaceapps.buetzenith.trashtrack.utils;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import spaceapps.buetzenith.trashtrack.service.model.TLEParsed;
import spaceapps.buetzenith.trashtrack.service.model.Trajectory;
import spaceapps.buetzenith.trashtrack.utils.tle.TleToGeo;

/**
 *
 */
public class RiskAnalysis {
    private static final String TAG = "RiskAnalysis";
    private LatLng latLng;
    private int alt; // km
    private int radius; // km
    private int hours;

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    private OnAnalysisFinish onAnalysisFinish;

    public void setOnAnalysisFinish(OnAnalysisFinish onAnalysisFinish) {
        this.onAnalysisFinish = onAnalysisFinish;
    }

    public interface  OnAnalysisFinish{
        void onFinish(Map<Integer, Integer> map);
    }

    public RiskAnalysis(LatLng latLng, int atl, int radius, int durationInHour) {
        this.latLng = latLng;
        this.alt = atl;
        this.radius = radius;
        this.hours = durationInHour;
    }

    public void countInside(List<TLEParsed> tleParsedList){
        Log.d(TAG, "countInside: ");
        executorService.execute(()->{
            Map<Integer, Integer> map = new HashMap<>();
            for (TLEParsed df : tleParsedList) {
                for (int i = 0; i < hours; i++) {
                    if (isInside(df, i)){
                        map.put(i, map.get(i)!=null? map.get(i)+1 : 1);
                        Log.d(TAG, "countInside: hour: "+i+" inside: "+df.name);
                    }
                }
            }
            Log.d(TAG, "countInside: analysis finished.");

            if(onAnalysisFinish!=null)
                onAnalysisFinish.onFinish(map);
        });
    }

    public boolean isInside(TLEParsed TLEParsed, int hour) {
       // Log.d(TAG, "isInside: "+TLEParsed.name);
        // step-1 calculate trajectory
        long start = System.currentTimeMillis() + hour*3600*1000;
        // calculate geolocation in every 10 sec interval
        for (long j = 0; j <= 3600; j += 400) {
            Trajectory trajectory = TleToGeo
                    .getPosition(TLEParsed.extractTle(), start + j);
            if (isInside(trajectory))
                return true;
        }
        return false;
    }

    private boolean isInside(Trajectory trajectory) {
        boolean flag = (alt - 25) <= trajectory.getHeight() && trajectory.getHeight() <= (alt + 25);

        double distance = getDistanceFromLatLonInKm(trajectory.getLatLng(), latLng);
       // Log.d(TAG, "isInside: distance: "+distance);
        flag &= distance<= this.radius;

        return flag;
    }

    // https://www.movable-type.co.uk/scripts/latlong.html
    private double haversine_distance(LatLng mk1, LatLng mk2) {
        double R = 6371; // Radius of the Earth in miles
        double rlat1 = mk1.latitude * (Math.PI / 180); // Convert degrees to radians
        double rlat2 = mk2.latitude * (Math.PI / 180); // Convert degrees to radians
        double difflat = rlat2 - rlat1; // Radian difference (latitudes)
        double difflon = (mk2.longitude - mk1.longitude) * (Math.PI / 180); // Radian difference (longitudes)

        double d = 2 * R * Math.asin(Math.sqrt(Math.sin(difflat / 2) * Math.sin(difflat / 2) + Math.cos(rlat1) * Math.cos(rlat2) * Math.sin(difflon / 2) * Math.sin(difflon / 2)));
        return d;
    }

    private double getDistanceFromLatLonInKm(LatLng latLng1,LatLng latLng2) {
        double lat1 = latLng1.latitude;
        double lat2 = latLng2.latitude;
        double lon1 = latLng.longitude;
        double lon2 = latLng.longitude;

        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }
    private double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }
}
