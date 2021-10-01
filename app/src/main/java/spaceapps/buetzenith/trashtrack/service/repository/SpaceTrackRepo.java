package spaceapps.buetzenith.trashtrack.service.repository;

import android.content.SharedPreferences;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import spaceapps.buetzenith.trashtrack.BuildConfig;
import spaceapps.buetzenith.trashtrack.dagger.module.NetworkModule;

public class SpaceTrackRepo{
    private static final String TAG = "SpaceTrackRepo";
    public static final String IS_LOGGED_IN_TO_SPACETRACK = "is_logged_in_to_spacetrack";

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    NetworkModule.SpaceTrackApi spaceTrackApi;


    @Inject
    public SpaceTrackRepo(){
    }

    private boolean isLoggedIn(){
        return sharedPreferences.getBoolean(IS_LOGGED_IN_TO_SPACETRACK,false);
    }

    public void login(){
        if(isLoggedIn())
            return;

        String relativePath ="ajaxauth/login";
        String data = "identity="+ BuildConfig.SPACE_TRACK_USERNAME+"&password="+BuildConfig.SPACE_TRACK_PASSWORD;

        // call
        Call<ResponseBody> call = spaceTrackApi.getApiEndPoints()
                .GETResponseBody(relativePath);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        String body = response.body().string();
                        Log.d(TAG, "onResponse: "+body);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.e(TAG, "onResponse: response code: "+response.code());
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void fetchData(){
        String relativePath = "basicspacedata/query/class/gp/decay_date/null-val//object_type/debris/epoch/>now-7/orderby/norad_cat_id/format/3le";
        Call<ResponseBody> call = spaceTrackApi.getApiEndPoints()
                .GETResponseBody(relativePath);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        String body = response.body().string();
                        Log.d(TAG, "onResponse: "+body);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.e(TAG, "onResponse: response code: "+response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
