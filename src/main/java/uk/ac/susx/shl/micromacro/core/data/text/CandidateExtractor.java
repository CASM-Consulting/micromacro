package uk.ac.susx.shl.micromacro.core.data.text;

import java.util.List;

/**
 * Created by sw206 on 16/04/2018.
 */
public interface CandidateExtractor<T> {

    List<Candidate> extractCandidates(T input);
}
