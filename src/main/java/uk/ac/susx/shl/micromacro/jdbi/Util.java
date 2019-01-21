package uk.ac.susx.shl.micromacro.jdbi;

import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;
import uk.ac.susx.tag.method51.twitter.LabelDecision;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Util {
    private static final Logger LOG = Logger.getLogger(DatumMapper.class.getName());

    public static Datum processLabelDecisions(Datum datum) {

        Set<String> models = new HashSet<>();

        for(Key key : datum.getKeys().keys.keys()) {

            if(key.type.isAssignableFrom(RuntimeType.of(LabelDecision.class))) {

                LabelDecision l = (LabelDecision)datum.get(key);

                int idx = key.name.lastIndexOf(l.label);

                String model = key.name.substring(0,idx-1);

                if(!models.add(model)) {
                    LOG.warning("multiple label decisions for " + model);
                }

                datum = datum.without(key)
                        .with(Key.of(key.namespace, model, RuntimeType.of(LabelDecision.class)), l);
            }
        }

        return datum;
    }

//    public static KeySet processLabelDecisions(KeySet keys) {
//
//        Set<String> models = new HashSet<>();
//
//        for(Key key : keys.keys.keys()) {
//
//            if(key.type.isAssignableFrom(RuntimeType.of(LabelDecision.class))) {
//
//                LabelDecision l = (LabelDecision)datum.get(key);
//
//                int idx = key.name.lastIndexOf(l.label);
//
//                String model = key.name.substring(0,idx-1);
//
//                if(!models.add(model)) {
//                    LOG.warning("multiple label decisions for " + model);
//                }
//
//                datum = datum.without(key)
//                        .with(Key.of(key.namespace, model, RuntimeType.of(LabelDecision.class)), l);
//            }
//        }
//
//        return datum;
//    }

}
