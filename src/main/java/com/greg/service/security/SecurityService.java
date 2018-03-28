package com.greg.service.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.greg.entity.user.User;
import com.greg.exceptions.InvalidAccessAttemptException;
import com.greg.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class SecurityService {

    private UserService userService;

    @Autowired
    public SecurityService(UserService userService) {
        this.userService = userService;
    }

    public boolean validateLogin(JsonNode loginAttemptNode) throws IOException, InvalidAccessAttemptException {
        JsonNode email = loginAttemptNode.get("email");
        JsonNode password = loginAttemptNode.get("password");

        if (email == null || password == null)
            throw new InvalidAccessAttemptException("Email and password must be provided");

        User user = userService.get(email.asText());
        return (user != null && user.getPassword().equals(password.asText()));
    }

    public void register(JsonNode registerNode) throws InvalidAccessAttemptException, IOException {
        JsonNode email = registerNode.get("email");
        JsonNode name = registerNode.get("name");
        JsonNode password = registerNode.get("password");

        if (email == null || password == null)
            throw new InvalidAccessAttemptException("Email and password must be provided");

        userService.addUser(email.asText(),
                (name == null) ? null : name.asText(),
                password.asText()
        );
    }
}
