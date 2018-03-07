package com.greg.dao;

import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Repository
@Transactional
public interface Dao<T> {
    T get(String id);
    void delete(String id);
    List list();
    void update(T t);
}