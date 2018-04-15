package com.greg.service;

import com.greg.dao.Dao;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */

//Annotations do not extend to child classes
public abstract class AbstractService<T> {
    private static final Logger LOG = Logger.getLogger(AbstractService.class);
    private Dao<T> dao;

    public T get(String id) {
        return dao.get(id);
    }

    public void update(T t) {
        dao.update(t);
    }

    public void delete(String id) {
        dao.delete(id);
    }

    protected void setDao(Dao dao) {
        this.dao = dao;
    }

    public List<T> list() {
        return dao.list();
    }

}
