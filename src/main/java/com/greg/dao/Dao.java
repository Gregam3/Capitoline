package com.greg.dao;

import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
public interface Dao<T> {
    T get(String id);
    void delete(String id);
    List list();
    void update(T t);
}
