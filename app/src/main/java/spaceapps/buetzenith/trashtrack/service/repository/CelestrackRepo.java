package spaceapps.buetzenith.trashtrack.service.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import spaceapps.buetzenith.trashtrack.dagger.module.NetworkModule;
import spaceapps.buetzenith.trashtrack.service.model.DebrisFragment;
import spaceapps.buetzenith.trashtrack.utils.DebrisCatalog;

public class CelestrackRepo{
    private static final String TAG = "CelestrackRepo";

    @Inject
    NetworkModule.CelesTrackApi celesTrackApi;

    @Inject
    public CelestrackRepo(){
    }

    public LiveData<List<DebrisFragment>> getTLE(DebrisCatalog.Debris debris){
        Call<ResponseBody> call = celesTrackApi.getApiEndPoints()
                .GETResponseBody(debris.tleUrl);

        MutableLiveData<List<DebrisFragment>> mutableLiveData = new MutableLiveData<>();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        String body = response.body().string();
                        Log.d(TAG, "onResponse: "+debris.name+" data found");
                        String[] lines = body.split("\n");

                        List<DebrisFragment> debrisFragmentList = new ArrayList<>();
                        for (int i = 0; i < lines.length; i+=3) {
                            debrisFragmentList.add(new DebrisFragment(
                                    lines[i], lines[i+1], lines[i+2]
                            ));
                        }

                        mutableLiveData.postValue(debrisFragmentList);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {

            }
        });

        return mutableLiveData;
    }
}
