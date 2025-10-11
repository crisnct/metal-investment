package com.investment.metal.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RSSFeedParser {

  public Map<CurrencyType, Double> readFeed(String feedUrl) throws IOException {
    try {
      // Fetch and parse the XML document
      Document doc = fetchXmlDocument(feedUrl);
      return getExchangeRate(doc);
    } catch (Exception e) {
      throw new IOException("Can not read from RSS FEED " + feedUrl);
    }
  }

  private static Document fetchXmlDocument(String url) throws Exception {
    URL bnrUrl = new URL(url);
    URLConnection connection = bnrUrl.openConnection();
    try (InputStream inputStream = connection.getInputStream()) {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(inputStream);
    }
  }

  private static Map<CurrencyType, Double> getExchangeRate(Document doc) {
    // Normalize XML structure
    doc.getDocumentElement().normalize();
    final Map<CurrencyType, Double> result = new HashMap<>();

    // Retrieve the 'Cube' element containing exchange rates
    NodeList bodyList = doc.getElementsByTagName("Body");
    Element body0 = (Element) bodyList.item(0);

    NodeList cubeList = body0.getElementsByTagName("Cube");
    for (int i = 0; i < cubeList.getLength(); i++) {
      Element cubeElement = (Element) cubeList.item(i);
      // Retrieve all 'Rate' elements within the 'Cube' element
      NodeList rateList = cubeElement.getElementsByTagName("Rate");
      for (int j = 0; j < rateList.getLength(); j++) {
        Element rateElement = (Element) rateList.item(j);
        String currency = rateElement.getAttribute("currency");
        String rateValue = rateElement.getTextContent();
        try {
          CurrencyType currencyEnum = CurrencyType.valueOf(currency);
          result.put(currencyEnum, Double.parseDouble(rateValue));
        } catch (RuntimeException e) {
          //ignore unknown currency
        }
      }
    }
    return result;
  }


}
