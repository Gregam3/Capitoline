package com.greg.controller;

import com.greg.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.Map;

@Controller
public class PageController {

    // inject via application.properties
    @Value("${portfolio-tracker.system-version}")
    private String systemVersion;

    private UserService userService;

    @Autowired
    public PageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String welcome(Map<String, Object> model) throws IOException {
        model.put("systemVersion", systemVersion);

//        if(userService.getCurrentUser() == null) {
//            return "login/login";
//        }

        userService.get("gregoryamitten@gmail.com");

        return "home/home";
    }
}