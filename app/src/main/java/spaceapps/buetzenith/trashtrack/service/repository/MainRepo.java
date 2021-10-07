package spaceapps.buetzenith.trashtrack.service.repository;

import android.util.Log;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import spaceapps.buetzenith.trashtrack.dagger.anotation.MainActivityScope;
import spaceapps.buetzenith.trashtrack.dagger.module.NetworkModule;
import spaceapps.buetzenith.trashtrack.service.api.ApiCaller;
import spaceapps.buetzenith.trashtrack.service.api.OnFinishListener;
import spaceapps.buetzenith.trashtrack.service.model.Satellite;
import spaceapps.buetzenith.trashtrack.service.room.TLEParsedDao;

@MainActivityScope
public class MainRepo {
    private static final String TAG = "MainRepo";

    @Inject
    ExecutorService roomExecutorService;

    @Inject
    TLEParsedDao TLEParsedDao;

    @Inject
    NetworkModule.SpaceTrackApi spaceTrackApi;

    @Inject
    NetworkModule.SatelliteTLEApi SatelliteTLEApi;

    @Inject
    public MainRepo() {
    }

    private void fetchTLEData(List<Satellite> satellites) {
        for (Satellite sat : satellites) {
            ApiCaller<String> caller = new ApiCaller<>(String.class, SatelliteTLEApi.getRetrofit());
            caller.GETString("" + sat.getId())
                    .addOnFinishListener(new OnFinishListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            try {
                                JSONObject tle = new JSONObject(s);
                                if(tle.has("line1")){
                                    Log.d(TAG, "onSuccess: tle found, sat: "+tle.optString("name"));
                                    sat.setTleLine1(tle.optString("line1"));
                                    sat.setTleLine2(tle.optString("line2"));
                                    /*roomExecutorService.execute(()->{
                                        TLEParsedDao.insert(sat);
                                    });*/
                                }else
                                    throw  new Exception();
                            } catch (Exception e) {
                                Log.d(TAG, "onSuccess: tle data not found, sat code: " + sat.getId());
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "onFailure: failed fetching tle for " + sat.getId(), e);
                        }
                    });
        }
    }

}
