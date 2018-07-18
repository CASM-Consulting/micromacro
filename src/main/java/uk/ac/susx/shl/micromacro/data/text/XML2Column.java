package uk.ac.susx.shl.micromacro.core.data.text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
public class XML2Column {



    public static void main(String[] args) throws Exception {

        List<XML2Datum.Element> interestingElements = new ArrayList<>();

        interestingElements.add(new XML2Datum.Element("div0", ImmutableMap.of("type", "sessionsPaper"), "sessionsPaper").isContainer(true));
//        interestingElements.add(new XML2Datum.Element("div1", ImmutableMap.of("type", "frontMatter"), "frontMatter"));

        interestingElements.add(new XML2Datum.Element("div1", ImmutableMap.of("type", "trialAccount"), "trialAccount").valueAttribute("id"));

        interestingElements.add(new XML2Datum.Element("p", ImmutableMap.of(), "statement"));

        interestingElements.add(new XML2Datum.Element("placeName", ImmutableMap.of(), "placeName"));

//        interestingElements.add(new XML2Datum.Element("persName", ImmutableMap.of(), "persName"));
//        interestingElements.add(new XML2Datum.Element("interp", ImmutableMap.of("type", "gender"), "gender").valueAttribute("value").selfClosing(true));
//
//        interestingElements.add(new XML2Datum.Element("rs", ImmutableMap.of("type", "verdictDescription"), "verdictDescription"));
//        interestingElements.add(new XML2Datum.Element("interp", ImmutableMap.of("type", "verdictCategory"), "verdict").valueAttribute("value").selfClosing(true));
//
//        interestingElements.add(new XML2Datum.Element("rs", ImmutableMap.of("type", "offenceDescription"), "offenceDescription"));
//        interestingElements.add(new XML2Datum.Element("interp", ImmutableMap.of("type", "offenceCategory"), "offenceCategory").valueAttribute("value").selfClosing(true));
//        interestingElements.add(new XML2Datum.Element("interp", ImmutableMap.of("type", "offenceSubcategory"), "offenceSubcategory").valueAttribute("value").selfClosing(true));


        Path start = Paths.get("data", "sessionsPapers");
        SAXParserFactory factory = SAXParserFactory.newInstance();

        Key<Spans<String, String>> trials = Key.of("trialAccount", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> statements = Key.of("statement", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> placeNames = Key.of("placeName", RuntimeType.stringSpans(String.class));

        try {
            Files.createDirectory(Paths.get("data","placeName"));
        } catch (FileAlreadyExistsException e) {
            //pass
        }

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

                for(Datum trial : datum.getSpannedData(trials, keys)) {

                    StringBuilder sb = new StringBuilder();

                    for(Datum statement : trial.getSpannedData(statements, keys)) {

                        //Optional<Spans<String,String>> places = statement.maybeGet(placeNames);

                        Datum2Column<String> columns = new Datum2Column<>(statement, textKey, placeNames);

                        String s = columns.columnise();
//                        System.out.println(s);
                        sb.append(s);
                    }

                    if(sb.length() > 0) {
                        String trialId = trial.get(trials).get(0).get();

                        Files.write(Paths.get("data", "placeName", trialId+".col"), ImmutableList.of(sb));
                    }
                }

            }


        } catch (Throwable err) {
            err.printStackTrace ();
        }
    }
}
