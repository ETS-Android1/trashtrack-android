package spaceapps.buetzenith.trashtrack.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import spaceapps.buetzenith.trashtrack.dagger.anotation.MainActivityScope;
import spaceapps.buetzenith.trashtrack.service.model.TrajectoryData;
import spaceapps.buetzenith.trashtrack.service.repository.MainRepo;
import spaceapps.buetzenith.trashtrack.service.repository.NasaSSCApiRepo;


@MainActivityScope
public class MainViewModel extends ViewModel{
    public static final String TAG = "MainViewModel:";

    @Inject
    MainRepo mainRepo;

    @Inject
    NasaSSCApiRepo nasaSSCApiRepo;

    @Inject
    public MainViewModel() {

    }

    // fetch sun location from nasa sse api
    public LiveData<Map<String,ArrayList<TrajectoryData>>> getLocationOfSatellite(ArrayList<String> satIdList, String fromTime, String toTime){
        return nasaSSCApiRepo.getLocationOfSatelliteFromSSC(satIdList,fromTime,toTime);
    }

}
