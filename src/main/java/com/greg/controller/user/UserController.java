package com.greg.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.user.User;
import com.greg.service.user.UserService;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ConcurrentModificationException;

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

    @GetMapping(value = "get")
    public ResponseEntity<User> getCurrentUser(
    ) throws JsonProcessingException {
        try {
            return new ResponseEntity<>(userService.getCurrentUser(), HttpStatus.OK);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            System.err.println(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

//    @GetMapping(value = "post")
//    public ResponseEntity<User> getCurrentUser() throws JsonProcessingException {
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
                    holdingNode.get("quantity").asDouble() > 10000000000L)
                return new ResponseEntity<>("A value must be provided for Quantity and between than 0 and 10BN.", HttpStatus.BAD_REQUEST);

            if (holdingNode.get("dateBought") == null)
                return new ResponseEntity<>("A date must be selected for between than 1/1/2000 and today", HttpStatus.BAD_REQUEST);

            userService.addTransaction(holdingNode);
            return new ResponseEntity<>(HttpStatus.OK);

        }catch (JSONException e) {
            LOG.error(e.getMessage());
            System.err.println(e);
            return new ResponseEntity<>("Could not retrieve any data from AlphaVantage for that Stock Right now",
                    HttpStatus.BAD_REQUEST);
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

    @GetMapping(value = "get/holding-graph-data")
    public ResponseEntity<?> getGraphHoldingData() {
        try {
            return new ResponseEntity<>(userService.getGraphHoldingData(), HttpStatus.OK);
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
            return new ResponseEntity<>(
                    "You have modified your portfolio whilst it is loading as such history may not be correct.",
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "delete/holding/{acronym}/{holdingType}/{amountToRemove:.+}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> removeFromHolding(@PathVariable("acronym") String acronym,
                                                    @PathVariable("holdingType") String holdingType,
                                                    @PathVariable("amountToRemove") Double amountToRemove) {
        try {
            if (amountToRemove == null || amountToRemove <= 0)
                return new ResponseEntity<>("Amount to remove must be greater than 0.", HttpStatus.BAD_REQUEST);

            userService.reduceHoldingQuantity(acronym, HoldingType.valueOf(holdingType), amountToRemove);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "update/settings", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateSettings(@RequestBody JsonNode settingsNode) {
        try {
            userService.updateSettings(settingsNode);
            return new ResponseEntity<>("Settings updated", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("No currency selected", HttpStatus.BAD_REQUEST);
        }
    }
}