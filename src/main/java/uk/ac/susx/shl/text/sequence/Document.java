package uk.ac.susx.shl.text.sequence;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Document {

    private final List<List<String>> sentences;

    public interface SpanGetter{
        <T> T get(Document document, Span span);
    }

    public static class Span {
        private final int fromI;
        private final int toI;
        private final int fromJ;
        private final int toJ;
        private final Optional<String> label;

        public Span(int fi, int fj, int ti, int tj, Optional<String> l) {
            fromI = fi;
            fromJ = fj;
            toI = ti;
            toJ = tj;
            label = l;
        }
//
//        public <T> T get(SpanGetter getter) {
//            return getter.get(Document.this, this);
//        }
    }


    public Document() {
        sentences = new ArrayList<>();
    }

    public static class Builder {

        public static class Span {
            int from;
            int to;
            Optional<String> label;

            public Span(int f, int t, String l) {
                from = f;
                to = t;
                label = Optional.ofNullable(l);
            }
        }

        private final List<List<String>> sentences;
        private final List<List<Span>> spans;

        public Builder() {
            sentences = new ArrayList<>();
            spans = new ArrayList<>();
        }

        public Builder sentence(List<String> sentence, List<Span> ss) {
            sentences.add(ImmutableList.copyOf(sentence));
            spans.add(ss);
            return this;
        }

        public Document build() {
            return null;
        }
    }

}
