package com.investment.metal.service.impl;

import com.investment.metal.MetalType;
import com.investment.metal.service.AbstractService;
import com.investment.metal.service.ExternalMetalPriceService;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.stereotype.Service;

@Service
public class GalmarleyService extends AbstractService implements ExternalMetalPriceService {

    @Override
    public double fetchPrice(MetalType metalType) {
        HttpResponse<String> response = Unirest.get("https://www.galmarley.com/prices/prices.json?callback=jQuery341049419930235311216_1598937200774&noCache=1598968803664&version=v2&chartType=CHART_POINTS&valuationSecurityId=USD&interval=5&batch=Update&_=1598937201386")
                .queryString("securityId", metalType.getSymbol())
                .header("Connection", "keep-alive")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.135 Mobile Safari/537.36")
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
        int p1 = content.indexOf("(");
        int p2 = content.lastIndexOf(")");
        String jsonContent = content.substring(p1 + 1, p2);
        final JsonNode node = new JsonNode(jsonContent);
        return node.getObject().getJSONObject("latestPrice").getDouble("price");
    }
}
