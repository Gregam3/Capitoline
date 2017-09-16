package com.greg.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;

@Controller
public class PageController {
    @RequestMapping(value = "/home", method = RequestMethod.GET, produces = "text/html;charset=UTF-8")
    public String index(Model model) {
        return "home.html";
    }
}
