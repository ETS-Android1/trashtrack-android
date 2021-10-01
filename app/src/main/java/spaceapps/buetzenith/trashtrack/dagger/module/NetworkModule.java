package spaceapps.buetzenith.trashtrack.dagger.module;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import spaceapps.buetzenith.trashtrack.service.api.ApiEndPoints;

@Module
public abstract class NetworkModule{
    public static final String TLE_API_BASE_URL = "https://tle.ivanstanojevic.me/api/tle/";
    public static final String NASA_SSC_API = "https://sscweb.gsfc.nasa.gov/WS/sscr/2/";
    public static final String CELESTRACK_API = "https://celestrak.com/NORAD/elements/";
    public static final String SPACETRACK_API = "https://www.space-track.org/";

    @Provides
    @Singleton
    static SpaceTrackApi provideSpaceTrackApi(){
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .addInterceptor(interceptor)
                .followRedirects(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SPACETRACK_API)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return new SpaceTrackApi(retrofit);
    }

    @Provides
    @Singleton
    static CelesTrackApi provideCelesTrackApi(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .followRedirects(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CELESTRACK_API)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return new CelesTrackApi(retrofit);
    }

    // https://celestrak.com/NORAD/documentation/gp-data-formats.php
    // https://tle.ivanstanojevic.me/
    @Provides
    @Singleton
    static SatelliteTLEApi provideTLEApi() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .followRedirects(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TLE_API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return new SatelliteTLEApi(retrofit);
    }

    @Provides
    @Singleton
    static NasaSSCApi provideNasaSSCApi() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .followRedirects(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NASA_SSC_API)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return new NasaSSCApi(retrofit);
    }


    public static class SatelliteTLEApi {

        private final Retrofit retrofit;

        public SatelliteTLEApi(Retrofit retrofit) {
            this.retrofit = retrofit;
        }

        public Retrofit getRetrofit() {
            return retrofit;
        }
    }

    public static class SpaceTrackApi {
        private final Retrofit retrofit;
        private final ApiEndPoints apiEndPoints;

        public SpaceTrackApi(Retrofit retrofit) {
            this.retrofit = retrofit;
            apiEndPoints = retrofit.create(ApiEndPoints.class);
        }

        public Retrofit getRetrofit() {
            return retrofit;
        }

        public ApiEndPoints getApiEndPoints(){
            return apiEndPoints;
        }
    }

    public static class CelesTrackApi {
        private final Retrofit retrofit;
        private final ApiEndPoints apiEndPoints;

        public CelesTrackApi(Retrofit retrofit) {
            this.retrofit = retrofit;
            apiEndPoints = retrofit.create(ApiEndPoints.class);
        }

        public Retrofit getRetrofit() {
            return retrofit;
        }

        public ApiEndPoints getApiEndPoints(){
            return apiEndPoints;
        }
    }

    public static class NasaSSCApi{

        private final Retrofit retrofit;

        public NasaSSCApi(Retrofit retrofit) {
            this.retrofit = retrofit;
        }

        public Retrofit getRetrofit() {
            return retrofit;
        }
    }
}
