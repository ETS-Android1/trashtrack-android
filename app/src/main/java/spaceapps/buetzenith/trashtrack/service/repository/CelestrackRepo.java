package spaceapps.buetzenith.trashtrack.service.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import spaceapps.buetzenith.trashtrack.dagger.module.NetworkModule;
import spaceapps.buetzenith.trashtrack.service.model.TLEParsed;
import spaceapps.buetzenith.trashtrack.service.room.TLEParsedDao;
import spaceapps.buetzenith.trashtrack.utils.DebrisCatalog;

public class CelestrackRepo{
    private static final String TAG = "CelestrackRepo";

    @Inject
    NetworkModule.CelesTrackApi celesTrackApi;

    @Inject
    ExecutorService executorService;

    @Inject
    TLEParsedDao tleParsedDao;

    @Inject
    public CelestrackRepo(){
    }

    public LiveData<List<TLEParsed>> getAllDebrisPieceList(){
        return tleParsedDao.getTLEParsedListLiveData();
    }

    public LiveData<List<TLEParsed>> getDebrisPieceList(DebrisCatalog.Debris debris){
        executorService.execute(()->{
            List<TLEParsed> tleParseds =  tleParsedDao.getTLEParsedList(debris.tleUrl);
            if(tleParseds.size()>0){
                long epoch = tleParseds.get(0).extractTle().getEpoch().getTime();
                long diff = System.currentTimeMillis() - epoch;

                long week = 7*24*3600*1000;

                if(diff>week)
                    fetchDebrisPieceList(debris);
            }else{
                fetchDebrisPieceList(debris);
            }
        });
        return tleParsedDao.getTLEParsedListLiveData(debris.tleUrl);
    }

    public void fetchDebrisPieceList(DebrisCatalog.Debris debris){
        Call<ResponseBody> call = celesTrackApi.getApiEndPoints()
                .GETResponseBody(debris.tleUrl);

        MutableLiveData<List<TLEParsed>> mutableLiveData = new MutableLiveData<>();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        String body = response.body().string();
                        Log.d(TAG, "onResponse: "+debris.name+" data found");
                        String[] lines = body.split("\n");

                        List<TLEParsed> tleParsedList = new ArrayList<>();
                        for (int i = 0; i < lines.length; i+=3) {
                            tleParsedList.add(new TLEParsed(debris.tleUrl,
                                   i +"-"+lines[i], lines[i+1], lines[i+2]
                            ));
                        }
                        executorService.execute(()->{
                            tleParsedDao.insert(tleParsedList);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Log.e(TAG, "onFailure: failed", t);
            }
        });
    }
}
