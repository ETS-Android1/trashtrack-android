package spaceapps.buetzenith.trashtrack.dagger.component;

import javax.inject.Singleton;

import dagger.Component;
import spaceapps.buetzenith.trashtrack.dagger.module.AppModule;
import spaceapps.buetzenith.trashtrack.dagger.module.WorldWindModule;
import spaceapps.buetzenith.trashtrack.dagger.module.NetworkModule;
import spaceapps.buetzenith.trashtrack.dagger.module.RoomModule;

@Singleton
@Component(modules = {AppModule.class, RoomModule.class,
        NetworkModule.class, WorldWindModule.class})
public interface AppComponent{
    ActivityComponent.Builder activityComponentBuilder();
}
