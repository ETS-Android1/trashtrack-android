package spaceapps.buetzenith.trashtrack.service.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import spaceapps.buetzenith.trashtrack.service.model.TLEParsed;

@Database(entities = {TLEParsed.class},exportSchema = false, version = 1)
public abstract class DebrisXDatabase extends RoomDatabase {
    public abstract TLEParsedDao tleParsedDao(); // room will implement this
}
