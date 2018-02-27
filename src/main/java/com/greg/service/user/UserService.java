package com.greg.service.user;

import com.greg.dao.user.UserDao;
import com.greg.entity.user.User;
import com.greg.service.stock.StockService;
import com.greg.utils.JSONUtils;
import com.mashape.unirest.http.Unirest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class UserService {

    private UserDao userDao;
    private StockService stockService;

    @Autowired
    public UserService(UserDao userDao, StockService stockService) {
        this.userDao = userDao;
        this.stockService = stockService;
    }

    public User get(String email) throws IOException {

        User user = userDao.get(email);
        if (user.getHoldingsJson() != null) {
            user.setHoldings(JSONUtils.convertToHoldingsList(user.getHoldingsJson()));
        } else  user.setHoldings(new ArrayList<>());
        return user;
    }

    public void update(User user) {
        userDao.update(user);
    }
}
