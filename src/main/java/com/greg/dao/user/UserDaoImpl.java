package com.greg.dao.user;

import com.greg.dao.AbstractDaoImpl;
import com.greg.entity.user.User;
import com.greg.service.AbstractService;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Transactional
@Repository
public class UserDaoImpl extends AbstractDaoImpl<User> implements UserDao {
    private String tableName = "User";

    @Override
    public List list() {
        return list(tableName);
    }
}
