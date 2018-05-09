package uk.ac.susx.shl.text.sequence;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by sw206 on 09/05/2018.
 */
public class OBTEI2Column extends DefaultHandler {

    private Deque<String> elementStack;

    private List<Document> trials;

    private Document currentTrial;

    private StringBuilder data;

    private int i;

    public OBTEI2Column() {
        i = 0;
        elementStack = new ArrayDeque<>();
        trials = new ArrayList<>();
        data = new StringBuilder();
    }


    private boolean isTrialAccount(String qName, Attributes attributes) {
        String type = attributes.getValue("type");
        return qName.equals("div0") && type != null && type.equals("trialAccount");
    }

    private boolean isTrialAccount(String qName, Attributes attributes) {
        return
    }



    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        elementStack.push(qName);

        if(isTrialAccount(qName, attributes)) {
            currentTrial = new Document("ob-trial", "placeName");
        }


        System.out.println("push " + qName);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        elementStack.pop();
        System.out.println("pop " + qName);
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {

    }


    public static void main (String argv []) {
        Path start = Paths.get("sessionsPapers");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            for(Path path : Files.walk(start).filter(path->path.toString().endsWith("xml")).collect(Collectors.toList()) ) {

                System.out.println(path.toString());
                InputStream xmlInput = Files.newInputStream(path);
                SAXParser saxParser = factory.newSAXParser();
                OBTEI2Column handler = new OBTEI2Column();
                saxParser.parse(xmlInput, handler);
            }


        } catch (Throwable err) {
            err.printStackTrace ();
        }
    }

}
