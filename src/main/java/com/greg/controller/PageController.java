package com.greg.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class PageController {

    // inject via application.properties
    @Value("${portfolio-tracker.system-version}")
    private String systemVersion;

    @GetMapping("/")
    public String welcome(Map<String, Object> model) {
        model.put("systemVersion", systemVersion);
        return "home/home";
    }
}