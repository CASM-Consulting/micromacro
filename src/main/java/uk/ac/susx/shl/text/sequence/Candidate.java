package uk.ac.susx.shl.text.sequence;

/**
 * Created by sw206 on 16/04/2018.
 */
public class Candidate {

    private final String text;

    public static Candidate of(String text){
        return new Candidate(text);
    }

    private Candidate(String t) {
        text = t;
    }


    public String getText() {
        return text;
    }

}
