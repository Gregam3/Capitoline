package com.greg.dao.user;

import com.greg.dao.AbstractDaoImpl;
import com.greg.entity.user.Transaction;
import com.greg.entity.user.User;
import com.greg.entity.user.UserHolding;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Repository
@Transactional
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
    public int indexOfHolding(String email, String acronym) throws IOException {
        List<UserHolding> userHoldings = get(email).getHoldings();
        for (int i = 0; i < userHoldings.size(); i++)
            if (userHoldings.get(i).getAcronym().equals(acronym))
                return i;
        return -1;
    }
}
