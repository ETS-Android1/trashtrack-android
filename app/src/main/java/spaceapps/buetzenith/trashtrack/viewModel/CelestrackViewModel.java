package spaceapps.buetzenith.trashtrack.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import spaceapps.buetzenith.trashtrack.dagger.anotation.MainActivityScope;
import spaceapps.buetzenith.trashtrack.service.model.TLEParsed;
import spaceapps.buetzenith.trashtrack.service.repository.CelestrackRepo;
import spaceapps.buetzenith.trashtrack.utils.DebrisCatalog;

@MainActivityScope
public class CelestrackViewModel extends ViewModel{

    @Inject
    CelestrackRepo celestrackRepo;

    @Inject
    public CelestrackViewModel() {
    }

    public LiveData<List<TLEParsed>> getDebrisPieces(DebrisCatalog.Debris debris){
        return celestrackRepo.getDebrisPieceList(debris);
    }

    public LiveData<List<TLEParsed>> getAllDebrisPiecesData(){
        return celestrackRepo.getAllDebrisPieceList();
    }
}
