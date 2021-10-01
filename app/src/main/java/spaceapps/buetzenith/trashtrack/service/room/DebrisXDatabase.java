package spaceapps.buetzenith.trashtrack.service.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import spaceapps.buetzenith.trashtrack.service.model.Satellite;

@Database(entities = {Satellite.class},exportSchema = false, version = 1)
public abstract class DebrisXDatabase extends RoomDatabase {
    public abstract SatelliteDao satelliteDao(); // room will implement this
}
