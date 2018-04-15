package com.greg.service.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.greg.exceptions.InvalidAccessAttemptException;
import com.greg.exceptions.InvalidRegisterCredentialsException;
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

        return userService.getUserSecure(email.asText(), password.asText()) != null;
    }

    public void register(JsonNode registerNode) throws InvalidRegisterCredentialsException, IOException, InvalidAccessAttemptException {
        JsonNode email = registerNode.get("email");
        JsonNode name = registerNode.get("name");
        JsonNode password = registerNode.get("password");

        if (email == null || password == null)
            throw new InvalidRegisterCredentialsException("Email and password must be provided");

        if(name != null && name.asText().length() > 50)
            throw new InvalidRegisterCredentialsException("Name cannot exceed 50 characters, consider inputting a nickname.");

        if(email.asText().length() < 6 || email.asText().length() > 254)
            throw new InvalidRegisterCredentialsException("Email length must between 6-254 characters");

        if(password.asText().length() < 1 || password.asText().length() > 50)
            throw new InvalidRegisterCredentialsException("Password length must between 1-50 characters");

        userService.addUser(email.asText(),
                (name == null) ? null : name.asText(),
                password.asText()
        );
    }
}
