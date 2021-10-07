package spaceapps.buetzenith.trashtrack.service.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.neosensory.tlepredictionengine.Tle;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "tle")
public class TLEParsed{
    @PrimaryKey
    @NonNull
   public String name;
   public String tleLine1;
   public String tleLine2;
   public String url;

    public TLEParsed(String url, @NotNull String name, String tleLine1, String tleLine2) {
        this.name = name;
        this.tleLine1 = tleLine1;
        this.tleLine2 = tleLine2;
        this.url = url;
    }

    public Tle extractTle(){
        return new Tle(tleLine1, tleLine2);
    }
}
