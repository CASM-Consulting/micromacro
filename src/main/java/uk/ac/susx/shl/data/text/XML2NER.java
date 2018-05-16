package uk.ac.susx.shl.data.text;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.span.Spans;

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
        SAXParserFactory factory = SAXParserFactory.newInstance();


        try {
            for(Path path : Files.walk(start).filter(path->path.toString().endsWith("xml")).collect(Collectors.toList()) ) {

                System.out.println(path.toString());

                InputStream xmlInput = Files.newInputStream(path);

                SAXParser saxParser = factory.newSAXParser();

                XML2Datum handler = new XML2Datum(interestingElements);

                saxParser.parse(xmlInput, handler);

                Datum datum = handler.getDatum();
                KeySet keys = handler.getKeys();
                Key<String> textKey = handler.getTextKey();

                Key<Spans<String, String>> trials = keys.get("trialAccount");
                Key<Spans<String, String>> statements = keys.get("statement");

                for(Datum trial : datum.getSpannedData(trials, keys)) {

                    for(Datum statement : trial.getSpannedData(statements, keys)) {

                        Datum tokenized = Tokenizer.tokenize(statement, textKey, KeySet.of());

                        Key<List<String>> tokenKey = tokenized.getKeys().get(textKey+Tokenizer.SUFFIX);

                        String text = String.join(" ", tokenized.get(tokenKey));

                        String ner = NERSocket.get(text);

                        System.out.println(ner);
                    }


                }

            }


        } catch (Throwable err) {
            err.printStackTrace ();
        }
    }
}
