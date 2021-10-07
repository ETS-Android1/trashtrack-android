package spaceapps.buetzenith.trashtrack.dagger.module;

import android.util.Log;

import androidx.room.Room;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import spaceapps.buetzenith.trashtrack.service.room.TLEParsedDao;
import spaceapps.buetzenith.trashtrack.service.room.DebrisXDatabase;
import spaceapps.buetzenith.trashtrack.view.App;

@Module
public abstract class RoomModule{
    private static final String TAG = "RoomModule";

    /**
     * create 5 executor thread to perform
     * room operations in background thread
     *
     * @return {@link ExecutorService} with 5 fixed thread
     */
    @Provides
    @Singleton
    static ExecutorService provideRoomExecutorService(){
        Log.d(TAG, "provideRoomExecutorService: creating room executor service...");
        return Executors.newFixedThreadPool(5);
    }

    @Provides
    @Singleton
    static DebrisXDatabase provideDebrisXDataBase(App app){
        Log.d(TAG, "provideNoteDataBase: creating note database...");
        return Room.databaseBuilder(app.getApplicationContext(), DebrisXDatabase.class , "debris_x_db")
                .fallbackToDestructiveMigration() ///delete existing database
                .build();
    }

    @Provides
    @Singleton
    static TLEParsedDao provideNoteDao(App app){
        Log.d(TAG, "provideNoteDao: creating note dao...");
        return provideDebrisXDataBase(app).tleParsedDao();
    }
}
