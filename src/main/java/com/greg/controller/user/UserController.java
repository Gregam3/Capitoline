package com.greg.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.greg.entity.holding.HoldingType;
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

//    @GetMapping(value = "post")
//    public ResponseEntity<User> getUser() throws JsonProcessingException {
//        try {
//            userService.addSettings();
//            return new ResponseEntity<>(HttpStatus.OK);
//        } catch (Exception e) {
//            LOG.error(e.getMessage());
//            System.err.println(e);
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//    }

    @PutMapping(value = "add-holding", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateUser(@RequestBody JsonNode holdingNode) {
        try {
            if (holdingNode.get("acronym") == null)
                return new ResponseEntity<>("You must select a holding", HttpStatus.BAD_REQUEST);

            if (holdingNode.get("quantity") == null ||
                    holdingNode.get("quantity").asDouble() < 0 ||
                    holdingNode.get("quantity").asDouble() >= 10000000000L)
                return new ResponseEntity<>("A value must be provided for Quantity and between than 0 and 10BN.", HttpStatus.BAD_REQUEST);

            if (holdingNode.get("dateBought") == null)
                return new ResponseEntity<>("A date must be selected for between than 1/1/2000 and today", HttpStatus.BAD_REQUEST);

            userService.addTransaction(holdingNode);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            System.err.println(e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "delete/{email:.+}")
    public ResponseEntity deleteUser(@PathVariable("email") String email) {
            userService.delete(email);
            return new ResponseEntity(HttpStatus.OK);
    }


    @GetMapping(value = "get/holding-graph-data/{email:.+}")
    public ResponseEntity<?> getGraphHoldingData(@PathVariable("email") String email) {
        try {
            return new ResponseEntity<>(userService.getGraphHoldingData(email), HttpStatus.OK);
        } catch (UnirestException | IOException | ParseException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "delete/holding/{acronym}/{holdingType}/{amountToRemove:.+}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> deleteHolding(@PathVariable("acronym") String acronym,
                                                @PathVariable("holdingType") String holdingType,
                                                @PathVariable("amountToRemove") Double amountToRemove) {
        try {
            if (amountToRemove == null || amountToRemove <= 0)
                return new ResponseEntity<>("Amount to remove must be greater than 0.", HttpStatus.BAD_REQUEST);

            userService.deleteHolding(acronym, HoldingType.valueOf(holdingType), amountToRemove);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "update/settings",  produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateSettings(@RequestBody JsonNode settingsNode) {
        try {
            userService.updateSettings(settingsNode);
            return new ResponseEntity<>("test", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("No currency selected", HttpStatus.BAD_REQUEST);
        }
    }
}