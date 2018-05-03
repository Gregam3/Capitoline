package com.greg.dao.fiat;

import com.greg.dao.Dao;
import com.greg.entity.holding.fiat.Fiat;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Transactional
@Repository
public interface FiatDao extends Dao<Fiat> {

}
