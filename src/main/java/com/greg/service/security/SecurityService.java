package com.greg.service.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.greg.exceptions.InvalidAccessAttemptException;
import com.greg.exceptions.InvalidRegisterCredentialsException;
import com.greg.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class SecurityService {

    private UserService userService;
    private final Pattern emailPattern = Pattern.compile("([A-Za-z]+@[A-Za-z]+\\.[A-Za-z]{2,})");
    private final Pattern passwordPattern = Pattern.compile("([^/^\\s])+");

    @Autowired
    public SecurityService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Validates the log in attempt
     * @param loginAttemptNode node containing "email" and "password" data for log in attempt
     * @return was login successful
     * @throws IOException
     * @throws InvalidAccessAttemptException
     */
    public boolean validateLogin(JsonNode loginAttemptNode) throws IOException, InvalidAccessAttemptException {
        JsonNode email = loginAttemptNode.get("email");
        JsonNode password = loginAttemptNode.get("password");

        if (email == null || password == null)
            throw new InvalidAccessAttemptException("Email and password must be provided");

        return userService.getUserSecure(email.asText(), password.asText()) != null;
    }

    /**
     * Validates register attempt
     * @param registerNode node containing "email", "password" and maybe "name". Data needed for register attempt
     * @throws InvalidRegisterCredentialsException
     * @throws IOException
     * @throws InvalidAccessAttemptException
     */
    public void register(JsonNode registerNode) throws InvalidRegisterCredentialsException, IOException, InvalidAccessAttemptException {
        JsonNode emailNode = registerNode.get("email");
        JsonNode nameNode = registerNode.get("name");
        JsonNode passwordNode = registerNode.get("password");

        if (emailNode == null || passwordNode == null || passwordNode.isNull())
            throw new InvalidRegisterCredentialsException("Email and password must be provided");

        String email = emailNode.asText();
        String password = passwordNode.asText();

        if(nameNode.asText().length() > 50)
            throw new InvalidRegisterCredentialsException("Name cannot exceed 50 characters, consider inputting a nickname.");

        if(email.length() < 6 || email.length() > 254)
            throw new InvalidRegisterCredentialsException("Email length must between 6-254 characters");

        if(password.length() < 1 || password.length() > 50)
            throw new InvalidRegisterCredentialsException("Password length must between 1-50 characters");

        if(!emailPattern.matcher(email).find())
            throw new InvalidRegisterCredentialsException("Not a Valid Email.");

        if(!passwordPattern.matcher(password).find())
            throw new InvalidRegisterCredentialsException("Password cannot be empty or contain spaces or '/'.");

        userService.addUser(emailNode.asText(),
                (nameNode.asText().equals("null")) ? null : nameNode.asText(),
                password
        );
    }
}
