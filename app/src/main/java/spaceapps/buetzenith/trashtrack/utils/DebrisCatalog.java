package spaceapps.buetzenith.trashtrack.utils;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import spaceapps.buetzenith.trashtrack.R;

public class DebrisCatalog{

    public static List<Debris> debrisList = Arrays.asList(
            new Debris("IRIDIUM 33 Debris",
                    "10 February, 2009",
                    "Iridium 33 satellite", R.drawable.iridium_satellite_debris
            , "iridium-33-debris.txt"),
            new Debris("COSMOS 2251 Debris",
                    "10 February, 2009",
                    "COSMOS 2251 Satellite", R.drawable.strela_debris
            , "cosmos-2251-debris.txt"),
            new Debris("FENGYUN 1C Debris",
                    "11 January, 2007",
                    "FENGYUN 1C weather Satellite", R.drawable.fy1,
                    "1999-025.txt")
    );

    public static class Debris{
        public String name;
        public String date;
        public String origin;
        public int image;
        public String tleUrl;

        public Debris(String name, String date, String origin, int image, String tleUrl) {
            this.name = name;
            this.date = date;
            this.origin = origin;
            this.image = image;
            this.tleUrl = tleUrl;
        }

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }
}
