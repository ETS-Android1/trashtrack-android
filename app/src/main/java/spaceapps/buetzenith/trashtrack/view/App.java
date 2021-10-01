package spaceapps.buetzenith.trashtrack.view;

import android.app.Application;

import spaceapps.buetzenith.trashtrack.dagger.component.AppComponent;


import spaceapps.buetzenith.trashtrack.dagger.component.DaggerAppComponent;
import spaceapps.buetzenith.trashtrack.dagger.module.AppModule;

public class App extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
