package com.greg.dao.user;

import com.greg.dao.AbstractDaoImpl;
import com.greg.entity.user.User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
}
