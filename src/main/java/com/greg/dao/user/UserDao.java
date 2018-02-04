package com.greg.dao.user;

import com.greg.dao.Dao;
import com.greg.entity.user.User;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Repository
@Transactional
public interface UserDao extends Dao<User> {
}
