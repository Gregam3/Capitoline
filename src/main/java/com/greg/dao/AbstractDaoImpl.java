package com.greg.dao;

import com.greg.controller.stock.StockController;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Repository
@Transactional
public abstract class AbstractDao<T> {

    private static final Logger LOG = Logger.getLogger(StockController.class);

    @PersistenceContext
    protected EntityManager entityManager;

    public boolean insert(T t) {
        try {
            entityManager.persist(t);
            return true;
        } catch (Exception e) {
            LOG.error("Could not add " +t +" to database");
            return false;
        }
    }

    public T get(String id) {
        return (T) entityManager.find(toString().getClass(), id);
    }

    public void delete(String id) {
        entityManager.remove(get(id));
    }

    public List<T> list() {
        throw new AssertionError("Not Implemented, must override method in inheriting classes.");
    }

}
