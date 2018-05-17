package uk.ac.susx.shl.data.text;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.span.Spans;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sw206 on 15/05/2018.
 */
public class XML2NER {



    public static void main(String[] args) throws Exception {

        List<XML2Datum.Element> interestingElements = new ArrayList<>();

        interestingElements.add(new XML2Datum.Element("div0", ImmutableMap.of("type", "sessionsPaper"), "sessionsPaper").isContainer(true));

        interestingElements.add(new XML2Datum.Element("div1", ImmutableMap.of("type", "trialAccount"), "trialAccount").valueAttribute("id"));

        interestingElements.add(new XML2Datum.Element("p", ImmutableMap.of(), "statement"));

        Path start = Paths.get("data", "sessionsPapers");

        try {

            for(Datum trial : XML2Datum.getData(start, interestingElements, "trialAccount") ) {

                KeySet keys = trial.getKeys();
                Key<Spans<String, String>> sentenceKey = keys.get("statement");
                Key<String> textKey = keys.get("text");

                for(Datum statement : trial.getSpannedData(sentenceKey, keys)) {

                    Datum tokenized = Tokenizer.tokenize(statement, textKey, KeySet.of());

                    KeySet tokenizedKeys = tokenized.getKeys();

                    Key<List<String>> tokenKey = tokenizedKeys.get(textKey+Tokenizer.SUFFIX);
                    Key<Spans<List<String>, String>> spansKey = Key.of("placeName", RuntimeType.listSpans(String.class));


                    NER2Datum ner2Datum = new NER2Datum (
                        tokenKey,
                        ImmutableSet.of("placeName"),
                        spansKey,
                        true
                    );

                    String text = String.join(" ", tokenized.get(tokenKey));

                    String ner = NERSocket.get(text);

                    Datum nerd = ner2Datum.toDatum(ner);

                    if(tokenized.get(tokenKey).size() != nerd.get(tokenKey).size()) {
                        System.err.println("tokenised mismatch!");
                    } else {

                        tokenized = tokenized.with(nerd.getKeys().get("placeName"), nerd.get(spansKey));

                        Spans<List<String>, String> t = trial.get("trialAccount");

                        System.out.println(t.get(0).get());
                    }

                }



            }




        } catch (Throwable err) {
            err.printStackTrace ();
        }
    }
}
