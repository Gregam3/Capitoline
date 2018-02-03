package com.greg.controller.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@RestController("/user/")
public class UserController {
    @GetMapping("get")
    public ResponseEntity getUser() {
        return new ResponseEntity(,HttpStatus.OK);
    }
}
