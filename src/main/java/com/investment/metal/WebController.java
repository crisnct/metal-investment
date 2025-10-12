package com.investment.metal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class WebController {

    // Serve React app for all routes except API endpoints
    @RequestMapping(value = {
        "/", 
        "/home", 
        "/features", 
        "/about", 
        "/contact",
        "/login",
        "/dashboard",
        "/profile"
    }, method = {RequestMethod.GET, RequestMethod.POST})
    public String index() {
        return "forward:/index.html";
    }
    
    // Handle React Router routes
    @RequestMapping(value = "/{path:[^\\.]*}", method = RequestMethod.GET)
    public String reactRoutes() {
        return "forward:/index.html";
    }
}
