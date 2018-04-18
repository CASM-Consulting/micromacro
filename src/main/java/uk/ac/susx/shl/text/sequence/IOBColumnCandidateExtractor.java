package uk.ac.susx.shl.text.sequence;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;

/**
 * Created by sw206 on 16/04/2018.
 */
public class IOBColumnCandidateExtractor {

    private final String i;
    private final String o;
    private final String b;
    private final String split;

    private final Path path;

    public IOBColumnCandidateExtractor(Path path, String i, String o, String b, String split) {
        this.i = i;
        this.o = o;
        this.b = b;
        this.split = split;
        this.path = path;
    }

    public IOBColumnCandidateExtractor(Path path) {
        this(path,"placeName-i", "_", "placeName-b", "\\s" );
    }


    private Optional<Candidate> getCandidate(BufferedReader reader) throws IOException {


        StringBuilder sb = new StringBuilder();

        String line = "";
        int n = 0;
        try {

            while( isO(line = reader.readLine())) {
                ++n;
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
                ++n;
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

    public Iterator<Candidate> iterator() throws IOException {

        BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"));

        return new Iterator<Candidate>() {

            private volatile Candidate candidate;

            @Override
            public synchronized boolean hasNext() {

                if(candidate == null) {
                    try {
                        Optional<Candidate> optional = getCandidate(reader);
                        if(optional.isPresent()) {
                            candidate = optional.get();
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
            public synchronized Candidate next() {

                Candidate c = candidate;

                candidate = null;

                return c;
            }
        };
    }


}
