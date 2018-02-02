package com.greg.dao;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
public interface Dao<T> {
    boolean insert(T t);
    T get(String id);
    void delete(String id);
}
