package uk.ac.susx.shl.text.sequence;

import java.util.Map;

/**
 * Created by sw206 on 16/04/2018.
 */
public class Match {
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
}
