package com.greg.dao.fiat;

import com.greg.dao.AbstractDaoImpl;
import com.greg.entity.currency.Fiat;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Repository
@Transactional
public class FiatDaoImpl extends AbstractDaoImpl<Fiat> implements FiatDao  {
    public FiatDaoImpl() {
        setThisClass(Fiat.class);
    }
}