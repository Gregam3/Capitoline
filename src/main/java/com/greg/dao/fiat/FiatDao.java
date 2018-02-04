package com.greg.dao.fiat;

import com.greg.dao.Dao;
import com.greg.entity.currency.Fiat;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Transactional
@Repository
public interface FiatDao extends Dao<Fiat> {

}
