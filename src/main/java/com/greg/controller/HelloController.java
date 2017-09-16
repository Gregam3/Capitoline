package com.greg.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController {
    
    @RequestMapping("/")
    public ResponseEntity<String> index() {
        return new ResponseEntity<>("Finally", HttpStatus.OK);
    }
}
