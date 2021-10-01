package spaceapps.buetzenith.trashtrack.dagger.module;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import spaceapps.buetzenith.trashtrack.view.App;

@Module
public class AppModule{
    private App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    App provideApp(){
        return app;
    }

    @Provides
    @Singleton
    Context provideContext(){return app.getApplicationContext();}

    @Provides
    @Singleton
    Application provideApplication(){
        return app;
    }

    @Provides
    @Singleton
    SharedPreferences provideAppSharedPref(){
        return  provideContext().getSharedPreferences("debrisX", Context.MODE_PRIVATE);
    }
}
