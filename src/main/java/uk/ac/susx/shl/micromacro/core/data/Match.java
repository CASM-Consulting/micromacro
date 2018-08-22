package uk.ac.susx.shl.micromacro.core.data;

import uk.ac.susx.shl.micromacro.core.data.text.Candidate;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by sw206 on 16/04/2018.
 */
public class Match implements Serializable {


    private final String match;
    private final Candidate candidate;
    private final double score;
    private final Map<String, String> metadata;

    public static Match of(String m, Candidate c, double s, Map<String, String> meta) {
        return new Match(m, c, s, meta);
    }

    private Match(String m, Candidate c, double s, Map<String, String> meta) {
        match = m;
        candidate = c;
        score = s;
        metadata = meta;
    }

    @Override
    public String toString() {
        return String.format("%s %.2f", match, score);
    }
    public String getText() {
        return match;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public double getScore() {
        return score;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

}
