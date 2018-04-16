package uk.ac.susx.shl.text.sequence;


import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.GeoJSONUtil;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sw206 on 16/04/2018.
 */
public class GeoJsonKnowledgeBase implements KnowledegeBase {

    private final Map data;
    private final List<String> keyList;

    public GeoJsonKnowledgeBase(Path path) throws Exception {
        data = load(path);
        keyList = new ArrayList<>(data.keySet());
    }

    private Map load(Path path) throws Exception {

        Reader reader = GeoJSONUtil.toReader(path.toFile());

        GeometryJSON gjson = new GeometryJSON();

        FeatureJSON fjson = new FeatureJSON(gjson);

        CoordinateReferenceSystem utmCrs = CRS.decode("epsg:4326");

        FeatureCollection fc = fjson.readFeatureCollection(reader);

        SimpleFeatureType type = (SimpleFeatureType) fc.getSchema();

        FeatureIterator itr = fc.features();

        Map<String, List<Map>> data = new HashMap<>();

        while(itr.hasNext()) {
            Feature f = itr.next();

            String name = (String)f.getProperty("P_NAME").getValue();

            Map<String, String> datum = new HashMap<>();

            if(!data.containsKey(name)) {
                data.put(name, new ArrayList<>());
            }

            data.get(name).add(datum);
        }

        return data;
    }

    public List<Match> getMatches(Candidate candidate) {

        List<ExtractedResult> top = FuzzySearch.extractTop(candidate.getText(), keyList, 1);



        return top.stream().map(e -> Match.of(keyList.get(e.getIndex()), candidate,e.getScore(),new HashMap<>())).collect(Collectors.toList());
    }
}
