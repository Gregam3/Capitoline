package com.greg.service.user;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.greg.dao.user.UserDao;
import com.greg.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class UserService {

    private UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User get(String email) {
        return userDao.get(email);
    }

    public void update(User user) {
        userDao.update(user);
    }
}
