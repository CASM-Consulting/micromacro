package uk.ac.susx.shl.text.sequence;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Parse and extract IOB chunks from a column file, return them as Candidates.
 * Created by sw206 on 16/04/2018.
 */
public class IOBColumn2Document {

    private final String iSuffix;
    private final String oSuffix;
    private final String bSuffix;
    private final String split;

    private enum IOB {
        I,
        O,
        B
    }

    private class NotIOB extends RuntimeException {
        public NotIOB(String line) {
            super(line);
        }
    }

    private final Path path;

    public IOBColumn2Document(Path path, String i, String o, String b, String split) {
        this.iSuffix = i;
        this.oSuffix = o;
        this.bSuffix = b;
        this.split = split;
        this.path = path;
    }

    public IOBColumn2Document(Path path) {
        this(path,"-i", "_", "-b", "\\s" );
    }


    private List<String> getLines(BufferedReader reader) throws IOException {
        List<String> sentence = new LinkedList<>();
        String line = null;
        while((line = reader.readLine())!=null) {
            if(line.equals("")) {
                break;
            }
            sentence.add(line);
        }

        if(line == null) {
            return null;
        } else {
            return sentence;
        }
    }

    private List<String> getTokens(List<String> lines) {
        try {
            return lines.stream().map(this::getToken).collect(Collectors.toList());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println(lines);
            throw e;
        }
    }


    private List<Document.Builder.Span> getSpans(List<String> lines) {
        List<Document.Builder.Span> spans = new ArrayList<>();
        int from = 0;
        int to = 0;
        int i = 0;
        String wasTag = null;
        for(String line : lines) {
            String token = getToken(line);
            String tag = getToken(line);
            switch (getIOB(tag)) {
                case B: {
                    from = i;
                    wasTag = tag;
                    break;
                }
                case I: {
                    break;
                }
                case O: {
                    to = i;
                    spans.add(new Document.Builder.Span(from, to, wasTag));
                    break;
                }
                default: {
                    throw new NotIOB(line);
                    break;
                }

            }
            ++i;
        }

        return spans;
    }


    private IOB getIOB(String line) {
        if(line.indexOf(oSuffix) == line.length()-oSuffix.length()) {
            return IOB.O;
        } else if (line.indexOf(bSuffix) == line.length()-bSuffix.length()) {
            return IOB.B;
        } else if (line.indexOf(iSuffix) == line.length()-iSuffix.length()) {
            return IOB.I;
        } else {
            throw new NotIOB(line);
        }
    }

    private Optional<Document> getDocument(BufferedReader reader) throws IOException {

        Document.Builder builder = new Document.Builder();

        List<String> lines = null;
        int i = 0;
        try {
            while( (lines = getLines(reader)) != null) {
                ++i;
                builder.sentence(getTokens(lines));
                builder.spans(getSpans(lines));

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println(line);
            throw e;
        }

        if(line == null || line.equals("")) {
            return Optional.empty();
        }
//        else if (!isB(line)) {
//            throw new RuntimeException("Not IOB? " + line + " " + n);
//        }


        sb.append(getToken(line));

        try {
            while(isI(line = reader.readLine())) {
                sb.append(" ").append(getToken(line));
                ++i;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println(line);
            throw e;
        }

        return Optional.of(Candidate.of(sb.toString()));
    }

    private String getToken(String line) {
        return line.split(split)[0];
    }

    private String getTag(String line, int i) {
        return line.split(split)[i];
    }

    private String getTag(String line) {
        return line.split(split)[1];
    }

    private boolean isO(String line) {
        if(line == null) {
            return false;
        }
        String[] bits = line.split(split);
        if(bits.length != 2) {
            return true;
        } else {
            return bits[1].equals(o);
        }
    }

    private boolean isB(String line) {
        if(line == null) {
            return false;
        }
        return line.split(split)[1].equals(b);
    }

    private boolean isI(String line) {
        if(line == null) {
            return false;
        }
        String[] bits = line.split(split);
        if(bits.length != 2) {
            return false;
        } else {
            return bits[1].equals(i);
        }
    }

    public Iterator<Document> iterator() throws IOException {

        BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"));

        return new Iterator<Document>() {

            private volatile Document document;

            @Override
            public synchronized boolean hasNext() {

                if(document == null) {
                    try {
                        Optional<Document> optional = getDocument(reader);
                        if(optional.isPresent()) {
                            document = optional.get();
                            return true;
                        } else {
                            try {
                                reader.close();
                            } catch (IOException e) { }
                            return false;
                        }

                    } catch (IOException e) {
                        try {
                            reader.close();
                        } catch (IOException ee) { }
                        return false;
                    }

                } else {
                    return true;
                }
            }

            @Override
            public synchronized Document next() {

                Document d = document;

                document = null;

                return d;
            }
        };
    }


}
