package com.greg.dao.user;

import com.greg.dao.AbstractDaoImpl;
import com.greg.entity.user.UserHolding;
import com.greg.entity.user.Transaction;
import com.greg.entity.user.User;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Transactional
@Repository
public class UserDaoImpl extends AbstractDaoImpl<User> implements UserDao {
    private String tableName = "User";

    public UserDaoImpl() {
        setThisClass(User.class);
    }

    @Override
    public List list() {
        return list(tableName);
    }

    @Override
    public void addTransaction(String email, String holdingName, Transaction transaction) {

    }

    @Override
    public int indexOfHolding(String email, String acronym) throws IOException {
        List<UserHolding> userHoldings = get(email).getHoldings();
        for (int i = 0; i < userHoldings.size(); i++)
            if (userHoldings.get(i).getAcronym().equals(acronym))
                return i;
        return -1;
    }

    @Override
    public void appendTransaction(String email,
                                  int holdingIndex,
                                  Transaction transaction
    ) throws IOException {
        User user = get(email);
        user.getHoldings().get(holdingIndex).addTransaction(transaction);
        this.entityManager.getTransaction().begin();
        update(user);
        this.entityManager.getTransaction().commit();
    }

    public void addHolding(String email, UserHolding userHolding) throws IOException {
        User user = get(email);
        List<UserHolding> userHoldings = user.getHoldings();
        userHoldings.add(userHolding);
        user.setHoldings(userHoldings);
        user.setName("test");
        update(user);
    }
}
