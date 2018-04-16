package uk.ac.susx.shl.text.sequence;

import java.util.List;

/**
 * Created by sw206 on 16/04/2018.
 */
public interface KnowledegeBase {

    List<Match> getMatches(Candidate candidate);

}
