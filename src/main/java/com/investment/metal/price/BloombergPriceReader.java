package com.investment.metal.price;

import com.investment.metal.common.CurrencyType;
import com.investment.metal.common.MetalType;
import com.investment.metal.common.Util;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;

public class BloombergPriceReader implements ExternalMetalPriceReader {

    @Override
    public double fetchPrice(MetalType metalType) {
        final String metalSymbol = getSymbol(metalType);

        HttpResponse<String> response = Unirest.get("https://www.bloomberg.com/markets2/api/intraday/" + metalSymbol + "%3ACUR?days=1&interval=1&volumeInterval=15")
                .header("authority", "www.bloomberg.com")
                .header("user-agent", "Chrome"+System.currentTimeMillis())
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
        final JsonNode node = new JsonNode(jsonContent);
        JSONArray array = node.getArray().getJSONObject(0).getJSONArray("price");
        double price = array.getJSONObject(array.length() - 1).getDouble("value");

        return price / Util.OUNCE;
    }

    private String getSymbol(MetalType metalType) {
        switch (metalType) {
            case GOLD:
                return "XAU";
            case SILVER:
                return "XAG";
            case PLATINUM:
                return "XPT";
            default:
                return null;
        }
    }

    @Override
    public CurrencyType getCurrencyType() {
        return CurrencyType.USD;
    }
}
