package uk.ac.susx.shl.data;

import uk.ac.susx.shl.data.text.sequence.Candidate;

import java.util.List;

/**
 * Created by sw206 on 16/04/2018.
 */
public interface KnowledegeBase {

    List<Match> getMatches(Candidate candidate);

}
