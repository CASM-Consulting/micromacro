package uk.ac.susx.shl.micromacro.jdbi;

import org.junit.jupiter.api.Test;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;
import uk.ac.susx.tag.method51.twitter.LabelDecision;

import static org.junit.jupiter.api.Assertions.*;

class DatumMapperTest {

    @Test
    void labelDecision2Label() {

        String ns = "classifier";
        String model = "model1";
        String label1 = "label1";
        String label2 = "label2";
        String[] labels = new String[]{label1, label1};

        Key<LabelDecision> label1Key = Key.of(ns, model+"-"+label1, RuntimeType.of(LabelDecision.class));
        Key<LabelDecision> label2Key = Key.of(ns, model+"-"+label2, RuntimeType.of(LabelDecision.class));

        LabelDecision ld1 = new LabelDecision();
        ld1.label = label1;
        ld1.labels = labels;
        ld1.likelihoods = new double[]{1.0,0.0};

        LabelDecision ld2 = new LabelDecision();
        ld2.label = label2;
        ld2.labels = labels;
        ld2.likelihoods = new double[]{0.0,1.0};

        Datum datum1 = new Datum()
                .with(label1Key, ld1);

        Datum datum2 = new Datum()
                .with(label2Key, ld2);

        datum1 = DatumMapper.processLabelDecisions(datum1);
        datum2 = DatumMapper.processLabelDecisions(datum2);

        assertEquals(datum1.get(Key.of(ns, model, RuntimeType.of(LabelDecision.class))), ld1);
        assertEquals(datum2.get(Key.of(ns, model, RuntimeType.of(LabelDecision.class))), ld2);

    }
}