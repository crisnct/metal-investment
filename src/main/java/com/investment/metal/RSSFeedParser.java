package com.investment.metal;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class RSSFeedParser {

    private static final String TITLE = "title";

    private static final String ITEM = "item";

    public double readFeed(String feedUrl) throws IOException {
        try {
            URL url = new URL(feedUrl);
            boolean isFeedHeader = true;
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream in = url.openStream();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    String localPart = event.asStartElement().getName().getLocalPart();
                    switch (localPart) {
                        case ITEM:
                            isFeedHeader = false;
                            break;
                        case TITLE:
                            if (!isFeedHeader) {
                                String title = getCharacterData(event, eventReader);
                                return parseTitle(title);
                            }
                            break;
                    }
                }
            }
        } catch (IOException | XMLStreamException e) {
            //
        }
        throw new IOException("Can not read from RSS FEED " + feedUrl);
    }

    //1 USD = 4.1136 RON 08-09-2020 Curs de schimb BNR
    private double parseTitle(String title) {
        int posEqual = title.indexOf("=");
        int postRON = title.indexOf("RON", posEqual);
        String value = title.substring(posEqual + 1, postRON).trim();
        return Double.parseDouble(value);
    }

    private String getCharacterData(XMLEvent event, XMLEventReader eventReader)
            throws XMLStreamException {
        String result = "";
        event = eventReader.nextEvent();
        if (event instanceof Characters) {
            result = event.asCharacters().getData();
        }
        return result;
    }

}