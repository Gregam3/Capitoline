package com.greg.dao.stock;

import com.greg.dao.Dao;
import com.greg.entity.holding.stock.Stock;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Transactional
@Repository
public interface StockDao extends Dao<Stock> {
}
