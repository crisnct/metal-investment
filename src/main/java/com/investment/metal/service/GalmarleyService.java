package com.investment.metal.service;

import com.investment.metal.MetalType;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.stereotype.Service;

import java.util.Currency;

@Service
public class GalmarleyService {

    //1ounce = 0.0283495231 kg
    private static final double ounce = 0.0283495231;

    //percentages
    private static final double REVOLUT_GOLD_PROFIT = 0.0785;

    //TODO save this in the database and make a scheduler to update it from https://www.bnr.ro/RSS_200004_USD.aspx
    private static final double usdRonRate = 4.094;

    private static double calculateCost(double ouncePurchased, double pricePerKg) {
        return ouncePurchased * ounce * pricePerKg;
    }

    public static void main(String g[]) {
        //XAU
        double amountPurchased = 0.202734;

        //price per kg
        double priceGoldNow = getPrice(MetalType.GOLD);

        //USD
        //double metalPricePerKg = 63500;
//        double origInvestment = calculateCost(amountPurchased, metalPricePerKg);
//        double costNow = calculateCost(amountPurchased, priceGoldNow);
//        double profit = costNow - origInvestment;

        double revolutOrigInvestment = 1600 / usdRonRate;
        double revolutGoldPriceKg = priceGoldNow * (REVOLUT_GOLD_PROFIT + 1);
        double revolutGoldPriceOunce = revolutGoldPriceKg * ounce;
        double priceRevolut = revolutGoldPriceOunce * amountPurchased;
        double profitRevolut = priceRevolut - revolutOrigInvestment;

        String message = String.format("Revolut profit: %.2f USD", profitRevolut);
        System.out.println(message);

        trainRates(priceGoldNow);
    }

    private static void trainRates(double priceGoldNow) {
        //7.8121381593
        //7.8728846156
        //7.8660882013
        //7.8594608899
        double revolutGoldPriceOunce = 7773;

        double amountPurchased = 0.202734;
        double priceRevolut = revolutGoldPriceOunce * amountPurchased;
        double diffCostKg = (revolutGoldPriceOunce / (ounce * usdRonRate) - priceGoldNow);
        double perc = 100 * (diffCostKg / priceGoldNow);
        String msgRev = String.format("The gold price per kg is more expensive at Revolut with %.2f USD (%.10f%%)", diffCostKg, perc);
        System.out.println(msgRev);
        System.out.println("Amount: " + priceRevolut + " RON");
    }

    public double getProfit() {
        return 0;
    }

    private static double getPrice(MetalType metalType) {
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
