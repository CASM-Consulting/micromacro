package uk.ac.susx.shl.micromacro.core;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import uk.ac.susx.tag.method51.core.meta.JsonCodec;
import uk.ac.susx.tag.method51.core.meta.Key;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.susx.tag.method51.core.meta.JsonCodecUtil.*;

public class GeoMapTypeAdapter extends JsonCodec.Delegate<GeoMap> {

    public GeoMapTypeAdapter() {
        super((JsonElement json, Type typeOfT, JsonDeserializationContext context) -> {

            JsonObject obj = json.getAsJsonObject();

            String id = obj.get("id").getAsString();
            Key geoKey = context.deserialize(obj.get("geoKey"), Key.class);
            Key contextKey = context.deserialize(obj.get("contextKey"), Key.class);
            Key entryKey = context.deserialize(obj.get("entryKey"), Key.class);
            Key idKey = context.deserialize(obj.get("idKey"), Key.class);
            List<String> queries = context.deserialize(obj.get("queries"), new TypeToken<List<String>>(){}.getType());

            Map options = ifThere(obj, "options", (e) -> context.deserialize(e, Map.class), (Map)new HashMap<>());
            Map<String, Object> metadata = ifThere(obj, "options", (e) -> context.deserialize(e,  new TypeToken<Map<String, Object>>(){}.getType()), (Map<String,Object>)new HashMap<String,Object>());

            GeoMap map = new GeoMap(id, queries, geoKey, contextKey, entryKey, idKey, options, metadata);

            return map;
        }, (GeoMap src, Type typeOfSrc, JsonSerializationContext context) -> {

            JsonObject result = new JsonObject();

            result.addProperty("id", src.id());
            result.add("geoKey", context.serialize(src.geoKey(), Key.class));
            result.add("geoKey", context.serialize(src.geoKey(), Key.class));
            result.add("contextKey", context.serialize(src.contextKey(), Key.class));
            result.add("entryKey", context.serialize(src.entryKey(), Key.class));
            result.add("idKey", context.serialize(src.idKey(), Key.class));
            result.add("queries", context.serialize(src.queries(), List.class));
            result.add("options", context.serialize(src.options(), Map.class));
            result.add("metadata", context.serialize(src.metadata(), Map.class));

            return result;
        });
    }

}
