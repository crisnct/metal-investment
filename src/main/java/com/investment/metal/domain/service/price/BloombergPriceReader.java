package com.investment.metal.domain.service.price;

import com.investment.metal.domain.model.CurrencyType;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.infrastructure.util.Util;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;

/**
 * Price reader implementation for Bloomberg API.
 * Fetches precious metal prices from Bloomberg's intraday API endpoint.
 */
public class BloombergPriceReader implements ExternalMetalPriceReader {

    private static final String BLOOMBERG_API_BASE_URL = 
        "https://www.bloomberg.com/markets2/api/intraday/";
    private static final String BLOOMBERG_API_PARAMS = 
        "%3ACUR?days=1&interval=1&volumeInterval=15";

    /**
     * Fetch current price for the specified metal type from Bloomberg API.
     * 
     * @param metalType the type of metal to fetch price for
     * @return the current price of the metal per ounce
     * @throws RuntimeException if the API call fails or returns invalid data
     */
    @Override
    public double fetchPrice(MetalType metalType) {
        String metalSymbol = getSymbol(metalType);
        String apiUrl = BLOOMBERG_API_BASE_URL + metalSymbol + BLOOMBERG_API_PARAMS;

        HttpResponse<String> response = Unirest.get(apiUrl)
                .header("authority", "www.bloomberg.com")
                .header("user-agent", "Chrome" + System.currentTimeMillis())
                .header("dnt", "1")
                .header("accept", "*/*")
                .header("sec-fetch-site", "same-origin")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-dest", "empty")
                .header("referer", "https://www.bloomberg.com/quote/" + metalSymbol + ":CUR")
                .header("accept-language", "ro,en;q=0.9,en-GB;q=0.8")
                .header("if-none-match", "W/\"cafe-Gz5oVOGo7YERbAPvVFIFwfcYPL0\"")
                .asString();

        String jsonContent = response.getBody();
        JsonNode node = new JsonNode(jsonContent);
        JSONArray array = node.getArray()
                             .getJSONObject(0)
                             .getJSONArray("price");
        
        double price = array.getJSONObject(array.length() - 1)
                           .getDouble("value");

        // Convert to price per ounce
        return price / Util.OUNCE;
    }

    /**
     * Get the Bloomberg symbol for the specified metal type.
     * 
     * @param metalType the metal type to get symbol for
     * @return the Bloomberg symbol for the metal
     */
    private String getSymbol(MetalType metalType) {
        return switch (metalType) {
            case GOLD -> "XAU";
            case SILVER -> "XAG";
            case PLATINUM -> "XPT";
            default -> null;
        };
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
