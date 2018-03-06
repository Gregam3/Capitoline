package com.greg.dao;

import com.greg.controller.holding.stock.StockController;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Transactional
@Repository
public abstract class AbstractDaoImpl<T> implements Dao<T> {

    private static final Logger LOG = Logger.getLogger(StockController.class);

    private Class currentClass;

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    protected EntityManager entityManager;

    protected void setThisClass(Class currentClass) {
        this.currentClass = currentClass;
    }

    @SuppressWarnings("unchecked")
    public T get(String id) {
        return (T) entityManager.find(currentClass, id);
    }

    public void delete(String id) {
        entityManager.remove(get(id));
    }

    public void update(T t) {
        entityManager.merge(t);
        entityManager.flush();
    }

    @SuppressWarnings("unchecked")
    public List list(String tableName) {
        return entityManager.createQuery("from " + tableName, currentClass).getResultList();
    }
}
