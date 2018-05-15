package uk.ac.susx.shl.data.text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.span.Spans;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by sw206 on 09/05/2018.
 */
public class XML2Datum extends DefaultHandler {

    public static class Element {

        private final String name;

        private final ImmutableMap<String, String> attributes;

        private final boolean isContainer;

        private final boolean selfClosing;

        private final String label;

        private final String valueAttribute;

        private Element(String name, Attributes attributes) {
            this.name = name;
            Map<String, String> attrs = new HashMap<>();
            for(int i = 0; i < attributes.getLength(); ++i) {
                String k = attributes.getQName(i);
                String v = attributes.getValue(i);
                attrs.put(k,v);
            }
            this.attributes = ImmutableMap.copyOf(attrs);
            label = null;
            valueAttribute = null;
            isContainer = false;
            selfClosing = false;
        }

        public Element(String name, Map<String, String> attributes, String label) {
            this(name, attributes, label, null, false, false);
        }



        private Element(String name, Map<String, String> attributes, String label, String valueAttribute, boolean selfClosing, boolean isContainer) {
            this.name = name;
            this.attributes = ImmutableMap.copyOf(attributes);
            this.label = label;
            this.valueAttribute = valueAttribute;
            this.selfClosing = selfClosing;
            this.isContainer = isContainer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Element element = (Element) o;
            return com.google.common.base.Objects.equal(name, element.name) &&
                    com.google.common.base.Objects.equal(attributes, element.attributes);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(name, attributes);
        }

        private boolean is(Element other) {
            return other.name.equals(name) && other.attributes.entrySet().containsAll(attributes.entrySet());
        }

        private boolean selfClosing() {
            return selfClosing;
        }

        public Element attributes(Map<String, String> attributes) {
            return new Element(name, attributes, label, valueAttribute, selfClosing, isContainer);
        }

        public Element valueAttribute(String valueAttribute) {
            return new Element(name, attributes, label, valueAttribute, selfClosing, isContainer);
        }

        public Element selfClosing(boolean selfClosing) {
            return new Element(name, attributes, label, valueAttribute, selfClosing, isContainer);
        }

        public Element isContainer(boolean isContainer) {
            return new Element(name, attributes, label, valueAttribute, selfClosing, isContainer);
        }
    }

    private ArrayDeque<AbstractMap.SimpleImmutableEntry<Integer,Optional<Element>>> stack;

    private Datum datum;

    private StringBuilder text;

    private List<Element> interestingElements;

    private int i;

    private boolean enabled;

    private final Key<String> textKey;

    private final KeySet keys;

    public XML2Datum(List<Element> interestingElements) {
        i = 0;
        enabled = false;
        stack = new ArrayDeque<>();
        datum = new Datum();
        text = new StringBuilder();

        textKey = Key.of("text", RuntimeType.STRING);

        this.interestingElements = ImmutableList.copyOf(interestingElements);


        KeySet ks = KeySet.of();

        for(Element e : interestingElements) {
            ks = ks.with(Key.of(e.label, RuntimeType.stringSpans(String.class)));
        }

        keys = ks;
    }

    public KeySet getKeys() {
        return keys;
    }

    public Key<String> getTextKey() {
        return textKey;
    }

    private void startCheck(Element element) {
        if(element.isContainer) {
            enabled = true;
        }
    }

    private void endCheck(Element element) {
        if(element.isContainer) {
            datum = datum.with(textKey, text.toString());
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        Element element = new Element(qName, attributes);

        Optional<Element> possiblyInteresting = Optional.empty();

        for(Element interesting : interestingElements) {
            if(interesting.is(element)) {
                if(possiblyInteresting.isPresent()) {
                    throw new RuntimeException("Element already matched.");
                }
                possiblyInteresting = Optional.of(interesting.attributes(element.attributes));
                startCheck(possiblyInteresting.get());
            }
        }

        stack.push(new AbstractMap.SimpleImmutableEntry<>(i, possiblyInteresting));

//        System.out.println("push " + qName);
    }


    private int getStart() {
        Iterator<AbstractMap.SimpleImmutableEntry<Integer, Optional<Element>>> itr = stack.iterator();
        while(itr.hasNext() ) {
            AbstractMap.SimpleImmutableEntry<Integer, Optional<Element>> entry = itr.next();
            if(entry.getValue().isPresent() && !entry.getValue().get().selfClosing()) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("No non-self closing parent.");
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        AbstractMap.SimpleImmutableEntry<Integer, Optional<Element>> entry = stack.pop();

        if(entry.getValue().isPresent()) {

            Element element = entry.getValue().get();
            int start;
            if(element.selfClosing()) {
                start = getStart();
            } else {
                start = entry.getKey();
            }
            int end = i;
            String label = element.label;
            Key<Spans<String, String>> key = Key.of(label, RuntimeType.stringSpans(String.class));

            Spans<String, String> spans;

            if(datum.get().containsKey(key)) {
                spans = datum.get(key);
            } else {
                spans = Spans.annotate(textKey, String.class);
            }

            String value = label;
            if(element.valueAttribute !=null) {
                value = element.attributes.get(element.valueAttribute);
            }

//            String span = text.toString().substring(start, end);

            datum = datum.with(key, spans.with(start, end, value));

            endCheck(element);
        }

//        System.out.println("pop " + qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(enabled) {
            String dirty = new String(ch, start, length);
            String clean = clean(dirty);
            text.append(clean);
            i += clean.length();
        }
    }

    private String clean(String dirty) {
        dirty = " " + dirty + " ";
        String cleaner = dirty.replaceAll("\\s+", " ");
        return cleaner.equals(" ") ? "" : cleaner;
    }

    public Datum getDatum() {
        return datum;
    }

    public static void main (String argv []) {

    }

}
