package com.greg.controller.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.greg.service.security.SecurityService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@RestController
@RequestMapping("/security/")
public class UserSecurityController {
    private static final Logger LOG = Logger.getLogger(UserSecurityController.class);

    private SecurityService securityService;

    @Autowired
    public UserSecurityController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @PostMapping(value = "login", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> login(@RequestBody JsonNode loginAttemptNode) throws JsonProcessingException {
        try {
            if (securityService.validateLogin(loginAttemptNode))
                return new ResponseEntity<>("Logged in as " + loginAttemptNode.get("email").asText(), HttpStatus.OK);
            else
                return new ResponseEntity<>("Those details were not recognised", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            System.err.println(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "register", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> register(@RequestBody JsonNode registerNode) throws JsonProcessingException {
        try {
            securityService.register(registerNode);
            return new ResponseEntity<>("Logged in as " + registerNode.get("email").asText(), HttpStatus.OK);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            System.err.println(e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
