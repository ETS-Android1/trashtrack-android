package spaceapps.buetzenith.trashtrack.service.model;

import com.neosensory.tlepredictionengine.Tle;

public class DebrisFragment {
   public String name;
   public String tleLine1;
   public String tleLine2;

    public DebrisFragment(String name, String tleLine1, String tleLine2) {
        this.name = name;
        this.tleLine1 = tleLine1;
        this.tleLine2 = tleLine2;
    }

    public Tle extractTle(){
        return new Tle(tleLine1, tleLine2);
    }
}
