package uk.ac.susx.shl.micromacro.core;

import com.google.gson.Gson;

import java.util.Map;

public class GeoMapFactory {

    private final Gson gson;

    public GeoMapFactory(Gson gson) {
        this.gson = gson;
    }

    public Map rep(GeoMap map) {
        return gson.fromJson(gson.toJson(map), Map.class);
    }

    public GeoMap map(Map map){
        return gson.fromJson(gson.toJson(map), GeoMap.class);
    }
}
