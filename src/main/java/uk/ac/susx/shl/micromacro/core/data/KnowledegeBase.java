package uk.ac.susx.shl.micromacro.core.data;

import uk.ac.susx.shl.micromacro.core.data.text.Candidate;

import java.util.List;

/**
 * Created by sw206 on 16/04/2018.
 */
public interface KnowledegeBase {

    List<Match> getMatches(Candidate candidate);

}
