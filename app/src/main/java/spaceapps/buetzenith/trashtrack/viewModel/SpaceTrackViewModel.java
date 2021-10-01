package spaceapps.buetzenith.trashtrack.viewModel;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import spaceapps.buetzenith.trashtrack.dagger.anotation.MainActivityScope;
import spaceapps.buetzenith.trashtrack.service.repository.SpaceTrackRepo;

@MainActivityScope
public class SpaceTrackViewModel extends ViewModel {
    @Inject
    SpaceTrackRepo spaceTrackRepo;


    @Inject
    public SpaceTrackViewModel(){
    }

    public void login(){
        spaceTrackRepo.login();
    }

    public void fetchData(){
        spaceTrackRepo.fetchData();
    }


}
