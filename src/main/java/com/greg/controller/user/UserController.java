package com.greg.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.greg.entity.holding.crypto.Crypto;
import com.greg.entity.user.User;
import com.greg.entity.user.UserHoldings;
import com.greg.service.user.UserService;
import com.greg.utils.JSONUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    @GetMapping("get/{email:.+}")
    public ResponseEntity<User> getUser(@PathVariable("email") String email) throws JsonProcessingException {
        return new ResponseEntity<>(userService.get(email), HttpStatus.OK);
    }

    @PutMapping(value = "update", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateUser(@RequestBody JsonNode userNode) {
        userService.update(JSONUtils.convertToUser(userNode));
        return new ResponseEntity<>("Updated successfully", HttpStatus.OK);
    }
}