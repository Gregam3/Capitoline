package com.greg.dao.user;

import com.greg.dao.AbstractDaoImpl;
import com.greg.entity.holding.HoldingType;
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
    public int indexOfHolding(User user, String acronym, HoldingType holdingType) throws IOException {
        List<UserHolding> userHoldings = user.getHoldings();
        for (int i = 0; i < userHoldings.size(); i++)
            if (userHoldings.get(i).getAcronym().equals(acronym) &&
                    userHoldings.get(i).getHoldingType().equals(holdingType))
                return i;
        return -1;
    }
}
