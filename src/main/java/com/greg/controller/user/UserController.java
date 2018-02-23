package com.greg.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.greg.entity.user.User;
import com.greg.service.user.UserService;
import com.greg.utils.JSONUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@RestController
@RequestMapping("/user/")
public class UserController {

    private static final Logger LOG = Logger.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {

        this.userService = userService;
    }

    @GetMapping(value = "get/{email:.+}")
    public ResponseEntity<User> getUser(@PathVariable("email") String email) throws JsonProcessingException {
        try {
            return new ResponseEntity<>(userService.get(email), HttpStatus.OK);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            System.err.println(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "update", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateUser(@RequestBody JsonNode userNode) {
        try {
            userService.update(JSONUtils.convertToUser(userNode));
            return new ResponseEntity<>("Updated successfully", HttpStatus.OK);
        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
            System.err.println(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}