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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sw206 on 15/05/2018.
 */
public class XML2Column {



    public static void main(String[] args) throws Exception {
        obNer();
    }

    public static void crimeDate() throws Exception {

        Key<Spans<String, String>> sessions = Key.of("sessionsPaper", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> trials = Key.of("trialAccount", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> statements = Key.of("statement", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> entities = Key.of("entities", RuntimeType.stringSpans(String.class));
//        Key<Spans<String, String>> crimeDate = Key.of("crimeDate", RuntimeType.stringSpans(String.class));

        Map<Key<Spans<String, String>>, List<XML2Datum.Element>> interestingElements = new HashMap<>();

        interestingElements.put(sessions, ImmutableList.of(
                new XML2Datum.Element("div0", ImmutableMap.of("type", "sessionsPaper"), "sessionsPaper").isContainer(true)
        ));
//        interestingElements.add(new XML2Datum.Element("div1", ImmutableMap.of("type", "frontMatter"), "frontMatter"));

        interestingElements.put(trials, ImmutableList.of(
                new XML2Datum.Element("div1", ImmutableMap.of("type", "trialAccount"), "trialAccount").valueAttribute("id")
        ));

        interestingElements.put(statements, ImmutableList.of(
                new XML2Datum.Element("p", ImmutableMap.of(), "statement")
        ));

        interestingElements.put(entities, ImmutableList.of(
//                new XML2Datum.Element("placeName", ImmutableMap.of(), "placeName"),
                new XML2Datum.Element("rs", ImmutableMap.of("type", "crimeDate"), "crimeDate"))
        );
        Path start = Paths.get("data", "sessionsPapers");
        SAXParserFactory factory = SAXParserFactory.newInstance();


        try {
            Files.createDirectory(Paths.get("data","crimeDate"));
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

                        //Optional<Spans<String,String>> places = statement.maybeGet(crimeDate);

                        Datum2Column columns = new Datum2Column(statement, textKey, ImmutableList.of(entities));

                        String s = columns.columnise();
//                        System.out.println(s);
                        sb.append(s);
                    }

                    if(sb.length() > 0) {
                        String trialId = trial.get(trials).get(0).get();

                        Files.write(Paths.get("data", "crimeDate", trialId+".col"), ImmutableList.of(sb));
                    }
                }

            }


        } catch (Throwable err) {
            err.printStackTrace ();
        }
    }


    public static void obNer() throws Exception {

        Path outDir = Paths.get("data","obPlaceNer");

        Key<Spans<String, String>> sessions = Key.of("sessionsPaper", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> trials = Key.of("trialAccount", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> statements = Key.of("statement", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> entities = Key.of("entities", RuntimeType.stringSpans(String.class));
//        Key<Spans<String, String>> crimeDate = Key.of("crimeDate", RuntimeType.stringSpans(String.class));

        Map<Key<Spans<String, String>>, List<XML2Datum.Element>> interestingElements = new HashMap<>();

        interestingElements.put(sessions, ImmutableList.of(
            new XML2Datum.Element("div0", ImmutableMap.of("type", "sessionsPaper"), "sessionsPaper").isContainer(true)
        ));
//        interestingElements.add(new XML2Datum.Element("div1", ImmutableMap.of("type", "frontMatter"), "frontMatter"));

        interestingElements.put(trials, ImmutableList.of(
            new XML2Datum.Element("div1", ImmutableMap.of("type", "trialAccount"), "trialAccount").valueAttribute("id")
        ));

        interestingElements.put(statements, ImmutableList.of(
            new XML2Datum.Element("p", ImmutableMap.of(), "statement")
        ));

        interestingElements.put(entities, ImmutableList.of(
            new XML2Datum.Element("placeName", ImmutableMap.of(), "placeName")
//            ,new XML2Datum.Element("rs", ImmutableMap.of("type", "crimeDate"), "crimeDate")
        ));


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

        try {
            Files.createDirectory(outDir);
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

                        Datum2Column columns = new Datum2Column(statement, textKey, ImmutableList.of(entities));

                        String s = columns.columnise();
//                        System.out.println(s);
                        sb.append(s);
                    }

                    if(sb.length() > 0) {
                        String trialId = trial.get(trials).get(0).get();

                        Files.write(outDir.resolve(trialId+".col"), ImmutableList.of(sb));
                    }
                }

            }


        } catch (Throwable err) {
            err.printStackTrace ();
        }
    }
}
