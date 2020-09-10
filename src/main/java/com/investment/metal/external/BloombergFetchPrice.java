package com.investment.metal.external;

import com.investment.metal.common.CurrencyType;
import com.investment.metal.common.MetalType;
import com.investment.metal.common.Util;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;

public class BloombergFetchPrice implements MetalFetchPriceBean {

    @Override
    public double fetchPrice(MetalType metalType) {
        final String metalSymbol = getSymbol(metalType);

        HttpResponse<String> response = Unirest.get("https://www.bloomberg.com/markets2/api/intraday/" + metalSymbol + "%3ACUR?days=1&interval=1&volumeInterval=15")
                .header("authority", "www.bloomberg.com")
                .header("user-agent", "Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1")
                .header("dnt", "1")
                .header("accept", "*/*")
                .header("sec-fetch-site", "same-origin")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-dest", "empty")
                .header("referer", "https://www.bloomberg.com/quote/" + metalSymbol + ":CUR")
                .header("accept-language", "ro,en;q=0.9,en-GB;q=0.8")
                .header("cookie", "_pxhd=8d60a23da657222f91f396b65fc719cbfa2446d2a17087c66f263aa1eaa010c8:d65bb900-f375-11ea-a27e-a979413cf87d; optimizelyEndUserId=oeu1599749804746r0.6745751280125054; _reg-csrf=s%3Aoz7TZF_jfjiUQXqlNZ4NjXvO.EhMCvYbE%2FW5DBrpcAleq5a%2BI%2BECJ%2F%2FD2x5kHVdMb6Js; agent_id=80f9f861-3c96-4006-8ca0-153f2f7f1b5a; session_id=5bf11c39-b3c6-4da3-96f0-7ac8cc087d21; session_key=b4908dbc057fd039b35e8543bf374fa13b6584c4; _user-status=anonymous; _gcl_aw=GCL.1599749806.CjwKCAjwnef6BRAgEiwAgv8mQa6t2W0PMreGDrwQrUaw6f_s46cB2rrbQYVRN0jLP1um3wzynFqr3hoC8lAQAvD_BwE; _gcl_au=1.1.952723337.1599749806; bdfpc=004.4691446938.1599749806476; _ga=GA1.2.1319130082.1599749807; _gid=GA1.2.1125837996.1599749807; _rdt_uuid=1599749806974.f008becb-2095-48d0-83de-b6f155daeb01; _scid=24368186-bfa3-4729-9adb-b65908f46359; _pxvid=d65bb900-f375-11ea-a27e-a979413cf87d; notice_behavior=implied|eu; _fbp=fb.1.1599749807410.1254076603; _li_dcdm_c=.bloomberg.com; _lc2_fpi=b1166d620485--01ehw89qf380nv7jqcvyg3r9et; _gac_UA-11413116-1=1.1599749813.CjwKCAjwnef6BRAgEiwAgv8mQa6t2W0PMreGDrwQrUaw6f_s46cB2rrbQYVRN0jLP1um3wzynFqr3hoC8lAQAvD_BwE; opt-reg-modal-triggered=true; bb_geo_info={\"country\":\"RO\",\"region\":\"Europe\"}|1600354625080; _tb_t_ppg=https%3A//www.bloomberg.com/quote/" + metalSymbol + "%3ACUR; trc_cookie_storage=taboola%2520global%253Auser-id%3D35448899-f4ff-4ad8-8b78-19dd12521dcb-tuct3250767; notice_preferences=7:; notice_gdpr_prefs=0|1|2|3|4|5|6|7:; cmapi_gtm_bl=; cmapi_cookie_privacy=permit_1|2|3|4|5|6|7|8; __tbc=%7Bjzx%7Dt_3qvTkEkvt3AGEeiiNNgCDUkknCaDDwfuv7THktIPOdJTi9V5Uil4JHZKo0dYxRDLNuYNeYC_Q5SC9je9F9Tk9g4oaov4CtWUEvcIOoKZXQb1YUP8aPRXxe3VA4-YMyjylU0Z664w9lha1BgkmqDg; __pat=-14400000; DigiTrust.v1.identity=eyJpZCI6IkxPMktzMW50ZkpQWktBUytGcHEwNlc3RU4zcW1PanBrQ2tDN2p6YXZFY3Y4dGFtMXJvQ1c0QkpHQThzdldPMm1ObkQ0RU5mbXY3SkxZclhGSTZEdVZ0amVueERzbnNpUitvbWZuaGRiNUEwOHkwWjJvQzBmSkZrZXhac01iZVJvMWFXTmVwRmhGaGZtbzJXNlJIMnFZMTNCTC9YYjZTUW9WVVh6dlBTSTBuVlNFbWxPR2dodEZhT1VQcEhlMys5aDRaaUtJK2JtRkdXeXQ4U2VWckFhUThSWTNndlNmbG9MSlBja3MzMmJjR09TYXBhVlNHUmhOa013TW1NMUxNa3NnV1RocU82VGROVjR0WFdxMDk0WVZydVJwMisvd2Y3TzBLYmpwb3g4ekRicTBMQXIxMXoxRU0waXJmYVllb0lESS9YQS83aHhzYmtIcEUxcmtjL2Q2dz09IiwidmVyc2lvbiI6MiwicHJvZHVjZXIiOiIxQ3JzZFVOQW82IiwicHJpdmFjeSI6eyJvcHRvdXQiOmZhbHNlfSwia2V5diI6NH0%3D; com.bloomberg.player.volume.level=1; GED_PLAYLIST_ACTIVITY=W3sidSI6IjFjTzEiLCJ0c2wiOjE1OTk3NDk5MDcsIm52IjowLCJ1cHQiOjE1OTk3NDk4NzcsImx0IjoxNTk5NzQ5ODk0fV0.; _reg-csrf-token=juUTEveL-pRfqGpgED07Hbx7trgZvIRVTirs; _user_newsletters=[]; _uetsid=075f1f6246526ca227e113db1f13f394; _uetvid=a91c914616cbd916398dceebdf62c9de; __pvi=%7B%22id%22%3A%22v-2020-09-10-18-19-55-920-VQy6RTPAZ0ghtRmP-0d887597b3d7ca7c5f94ffd8645b1d08%22%2C%22domain%22%3A%22.bloomberg.com%22%2C%22time%22%3A1599751195920%7D; xbc=%7Bjzx%7DfUYbgjawoSPqMi0s8aubw6VGQ0XmtgdIZS0JdbNhsSUreoOcgchN8Rj7USn-YCw0b12JmUCvvCNbMXGbS_dYOUQdJfU6h8bfXfyOfJ7vGzB9b98CzCaB8mvO4I5DrDnYtGR_GyFxT3Ba28cVJMZF7MjRTMRUkihqRKCrVgZnWilYs90_NXhigQ-d6O2vAxujyRau6ochsO4avvts7yb6KdoV0CtqeCgHui9YIz53EeqHORYVpO993cAsvS5axBttRjUEvdCuyArSa2TM6xWc12hD3bEcmHiUBQm4fLvR2XaKi_s6HBeRcMeRV4Oh7oADM0gpF6JkeK_GZInN0hKb6pNeqyFGlfcFa5PlQ8i-OUM; __gads=ID=3bc3fec68de482c3:T=1599749829:RT=1599751434:S=ALNI_MbU_LnRo09toDpnSMO6XkroVkTqVA; _px2=eyJ1IjoiMTQ4MjA4ODAtZjM3OS0xMWVhLWI1ODktYTk3N2I0MmIyYzk5IiwidiI6ImQ2NWJiOTAwLWYzNzUtMTFlYS1hMjdlLWE5Nzk0MTNjZjg3ZCIsInQiOjE1OTk3NTI5NDAxODAsImgiOiJlNWI3NDI3MzMxODYwOTIzZDA0YzNlYTVhMWQxYzM5OTBkZDJlMTcyYTFjZDU1ODgwYzdjMmJlOWViZWQzMzkzIn0=; _px3=accc56dc6fff7d3caa00ca4d4527ef29f5e32dfd9cbd63258818f33e8733143b:Ke7ZOQSpnzDJZmTy29drXnwNKsswsrrn7seHO4pbqDUcP04ZwtU2zuqEP5GiI1ej2RJ+xJ9ejSARCW7IZMx0lg==:1000:avS3gHRz281bCv06TUAil6euzB/3hTumskIVabdQkvO8haTAc0T/LezZClt70kzqntSHyNEBbK3JIItEfamVqCS/Y7SjLNj4nEF0i6I3qGaZhLejjRiszOL1ybXbxGoxlAPlRgoF+c1IOV7EusQCYZUKOs5kQyN/WWoQIWx859E=; _pxde=4e9108dfb9356d94ed733750e088038d65cec2b985e04ee3f2301071fc585feb:eyJ0aW1lc3RhbXAiOjE1OTk3NTI2NDAxODEsImZfa2IiOjAsImlwY19pZCI6W119; _pxhd=8d60a23da657222f91f396b65fc719cbfa2446d2a17087c66f263aa1eaa010c8:d65bb900-f375-11ea-a27e-a979413cf87d")
                .header("if-none-match", "W/\"cafe-Gz5oVOGo7YERbAPvVFIFwfcYPL0\"")
                .asString();

        String jsonContent = response.getBody();
        final JsonNode node = new JsonNode(jsonContent);
        JSONArray array = node.getArray().getJSONObject(0).getJSONArray("price");
        double price = array.getJSONObject(array.length() - 1).getDouble("value");

        return price / Util.ounce;
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
