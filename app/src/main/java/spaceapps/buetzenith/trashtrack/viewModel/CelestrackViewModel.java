package spaceapps.buetzenith.trashtrack.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import spaceapps.buetzenith.trashtrack.dagger.anotation.MainActivityScope;
import spaceapps.buetzenith.trashtrack.service.model.DebrisFragment;
import spaceapps.buetzenith.trashtrack.service.repository.CelestrackRepo;
import spaceapps.buetzenith.trashtrack.utils.DebrisCatalog;

@MainActivityScope
public class CelestrackViewModel extends ViewModel{

    @Inject
    CelestrackRepo celestrackRepo;

    @Inject
    public CelestrackViewModel() {
    }

    public LiveData<List<DebrisFragment>> getTrajectoryTLE(DebrisCatalog.Debris debris){
        return celestrackRepo.getTLE(debris);
    }
}
