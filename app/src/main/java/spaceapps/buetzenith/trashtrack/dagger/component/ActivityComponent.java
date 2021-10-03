package spaceapps.buetzenith.trashtrack.dagger.component;

import dagger.Subcomponent;
import spaceapps.buetzenith.trashtrack.dagger.anotation.MainActivityScope;
import spaceapps.buetzenith.trashtrack.dagger.module.ActivityModule;
import spaceapps.buetzenith.trashtrack.view.MainActivity;
import spaceapps.buetzenith.trashtrack.view.screen.GraphFragment;
import spaceapps.buetzenith.trashtrack.view.screen.HomeFragment;
import spaceapps.buetzenith.trashtrack.view.screen.globe.GlobeFragment;
import spaceapps.buetzenith.trashtrack.view.screen.googlemap.GoogleMapFragment;

@MainActivityScope
@Subcomponent(modules = {ActivityModule.class})
public interface ActivityComponent{
    void inject(MainActivity mainActivity);
    void inject(GoogleMapFragment googleMapFragment);
    void inject(GlobeFragment globeFragment);

    void inject(HomeFragment homeFragment);

    void inject(GraphFragment graphFragment);

    @Subcomponent.Builder
    interface Builder{
        ActivityComponent build();
        Builder activityModule(ActivityModule activityModule);
    }
}
