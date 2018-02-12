package com.greg.controller.user;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.greg.entity.user.User;
import com.greg.service.user.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<User> getUser(@PathVariable("email") String email) {
        return new ResponseEntity<>(userService.get(email), HttpStatus.OK);
    }

    @PutMapping("update")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
//        if (user.get("email").isNull())
//            return new ResponseEntity<>("No email provided", HttpStatus.BAD_REQUEST);
        userService.update(user);
        return new ResponseEntity<>("Updated successfully", HttpStatus.OK);
    }
}