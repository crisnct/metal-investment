package com.investment.metal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class WebController {

    // Serve React app for root route
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "forward:/index.html";
    }
}
