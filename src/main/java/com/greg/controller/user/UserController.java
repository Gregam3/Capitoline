package com.greg.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.greg.entity.GraphHoldingData;
import com.greg.entity.user.User;
import com.greg.service.user.UserService;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

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
        } catch (Exception e) {
            LOG.error(e.getMessage());
            System.err.println(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "add-holding", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateUser(@RequestBody JsonNode holdingNode) {
        try {
            userService.addTransaction(holdingNode);
            return new ResponseEntity<>("Updated successfully", HttpStatus.OK);
        } catch (UnirestException | IOException e) {
            LOG.error(e.getMessage());
            System.err.println(e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "get/holding-graph-data/{email:.+}")
    public ResponseEntity<Map<String, List<GraphHoldingData>>> getGraphHoldingData(@PathVariable("email") String email) {
        try {
            return new ResponseEntity<>(userService.getGraphHoldingData(email), HttpStatus.OK);
        } catch (UnirestException | IOException | ParseException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}