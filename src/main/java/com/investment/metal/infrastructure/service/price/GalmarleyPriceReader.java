package com.investment.metal.infrastructure.service.price;

import com.investment.metal.domain.model.CurrencyType;
import com.investment.metal.domain.model.MetalType;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

/**
 * Price reader implementation for Galmarley API.
 * Fetches precious metal prices from Galmarley's JSONP API endpoint.
 */
public class GalmarleyPriceReader implements ExternalMetalPriceReader {

    private static final String GALMARLEY_API_URL = 
        "https://www.galmarley.com/prices/prices.json?callback=jQuery341049419930235311216_1598937200774" +
        "&noCache=1598968803664&version=v2&chartType=CHART_POINTS&valuationSecurityId=USD" +
        "&interval=5&batch=Update&_=1598937201386";

    /**
     * Fetch current price for the specified metal type from Galmarley API.
     * 
     * @param metalType the type of metal to fetch price for
     * @return the current price of the metal
     * @throws RuntimeException if the API call fails or returns invalid data
     */
    @Override
    public double fetchPrice(MetalType metalType) {
        HttpResponse<String> response = Unirest.get(GALMARLEY_API_URL)
                .queryString("securityId", metalType.getSymbol())
                .header("Connection", "keep-alive")
                .header("User-Agent", "Mozilla/" + System.currentTimeMillis())
                .header("DNT", "1")
                .header("Accept", "*/*")
                .header("Sec-Fetch-Site", "cross-site")
                .header("Sec-Fetch-Mode", "no-cors")
                .header("Sec-Fetch-Dest", "script")
                .header("Referer", "https://www.bullionvault.com/silver-price-chart.do")
                .header("Accept-Language", "ro,en;q=0.9,en-GB;q=0.8")
                .header("Content-Type", "application/json")
                .asString();
        
        String content = response.getBody();
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException(
                "Empty response from Galmarley API for " + metalType.getSymbol()
            );
        }
        
        // Parse JSONP response format: callback(data)
        int p1 = content.indexOf("(");
        int p2 = content.lastIndexOf(")");
        
        if (p1 >= 0 && p2 > p1) {
            String jsonContent = content.substring(p1 + 1, p2);
            JsonNode node = new JsonNode(jsonContent);
            return node.getObject()
                      .getJSONObject("latestPrice")
                      .getDouble("price");
        } else {
            String errorMessage = String.format(
                "Invalid response format from Galmarley API for %s. Response: %s",
                metalType.getSymbol(),
                content.substring(0, Math.min(200, content.length()))
            );
            throw new RuntimeException(errorMessage);
        }
    }

    /**
     * Get the currency type for this price reader.
     * 
     * @return USD currency type
     */
    @Override
    public CurrencyType getCurrencyType() {
        return CurrencyType.USD;
    }
}
