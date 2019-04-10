package uk.ac.susx.shl.micromacro.core;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import uk.ac.susx.tag.method51.core.meta.JsonCodec;
import uk.ac.susx.tag.method51.core.meta.Key;

import java.lang.reflect.Type;
import java.util.List;


public class GeoMapTypeAdapter extends JsonCodec.Delegate<GeoMap> {

    public GeoMapTypeAdapter() {
        super((JsonElement json, Type typeOfT, JsonDeserializationContext context) -> {

            JsonObject obj = json.getAsJsonObject();

            String id = obj.get("id").getAsString();
            Key geoKey = context.deserialize(obj.get("geoKey"), Key.class);
            Key idKey = context.deserialize(obj.get("idKey"), Key.class);
            List<String> queries = context.deserialize(obj.get("queries"), new TypeToken<List<String>>(){}.getType());

            GeoMap map = new GeoMap(id, queries, geoKey, idKey);

            return map;
        }, (GeoMap src, Type typeOfSrc, JsonSerializationContext context) -> {

            JsonObject result = new JsonObject();

            result.addProperty("id", src.id());
            result.add("geoKey", context.serialize(src.geoKey(), Key.class));
            result.add("idKey", context.serialize(src.idKey(), Key.class));
            result.add("queries", context.serialize(src.queries(), List.class));

            return result;
        });
    }

}
