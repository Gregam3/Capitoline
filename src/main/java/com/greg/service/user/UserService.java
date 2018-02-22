package com.greg.service.user;

import com.greg.dao.user.UserDao;
import com.greg.entity.user.User;
import com.greg.utils.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

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

    public User get(String email) throws IOException {
        User user = userDao.get(email);
        if (user.getHoldingsJson() != null)
            user.setHoldings(JSONUtils.convertToHoldingsList(user.getHoldingsJson()));

        return user;
    }

    public void update(User user) {
        userDao.update(user);
    }
}
