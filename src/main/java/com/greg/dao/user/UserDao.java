package com.greg.dao.user;

import com.greg.dao.Dao;
import com.greg.entity.holding.UserHolding;
import com.greg.entity.holding.Transaction;
import com.greg.entity.user.User;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.io.IOException;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Transactional
@Repository
public interface UserDao extends Dao<User> {
    public void addTransaction(String email, String holdingName, Transaction transaction);

    int indexOfHolding(String email, String acronym) throws IOException;

    void appendTransaction(String email, int holdingIndex, Transaction transaction) throws IOException;

    void addHolding(String email, UserHolding userHolding) throws IOException;
}
