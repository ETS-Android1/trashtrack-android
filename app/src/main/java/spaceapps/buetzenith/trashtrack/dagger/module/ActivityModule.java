package spaceapps.buetzenith.trashtrack.dagger.module;

import dagger.Module;
import dagger.Provides;
import spaceapps.buetzenith.trashtrack.view.MainActivity;

@Module/*(subcomponents = {ActivityComponent.class})*/
public class ActivityModule {
    private final MainActivity mainActivity;

    public ActivityModule(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Provides
    MainActivity provideMainActivity(){
        return mainActivity;
    }
}
