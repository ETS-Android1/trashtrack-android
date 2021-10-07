package spaceapps.buetzenith.trashtrack.service.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import spaceapps.buetzenith.trashtrack.service.model.Satellite;
import spaceapps.buetzenith.trashtrack.service.model.TLEParsed;


@Dao
public interface TLEParsedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TLEParsed... tleParseds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<TLEParsed> tleParsedList);

    @Query("SELECT * FROM tle where tle.url = :tleUrl")
    LiveData<List<TLEParsed>> getTLEParsedListLiveData(String tleUrl);

    @Query("SELECT * FROM tle")
    LiveData<List<TLEParsed>> getTLEParsedListLiveData();


    @Query("SELECT * FROM tle where url = :url")
    List<TLEParsed> getTLEParsedList(String url);

}
