package uk.ac.susx.shl.micromacro.core.data.text;

import java.io.Serializable;

/**
 * Created by sw206 on 16/04/2018.
 */
public class Candidate implements Serializable {

    private final String text;

    public static Candidate of(String text){
        return new Candidate(text);
    }

    private Candidate(String t) {
        text = t;

//        StringBuilder sb = new StringBuilder();
//
//        for(int i = fromI; i < toI; ++i) {
//            for(int j = fromJ; j < toJ; ++j) {
//                String token = sentences.get(i).get(j);
//                sb.append(token);
//            }
//        }
    }


    public String getText() {
        return text;
    }

}
