package com.greg.dao.fiat;

import com.greg.dao.AbstractDaoImpl;
import com.greg.entity.holding.fiat.Fiat;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Repository
@Transactional
public class FiatDaoImpl extends AbstractDaoImpl<Fiat> implements FiatDao {
    private String tableName = "Fiat";

    public FiatDaoImpl() {
        setThisClass(Fiat.class);
    }

    @Override
    public List list() {
        return list(tableName);
    }
}