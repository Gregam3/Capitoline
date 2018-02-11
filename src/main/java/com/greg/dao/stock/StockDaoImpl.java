package com.greg.dao.stock;

import com.greg.dao.AbstractDaoImpl;
import com.greg.dao.fiat.StockDao;
import com.greg.entity.currency.Fiat;
import com.greg.entity.holding.stock.Stock;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Repository
@Transactional
public class StockDaoImpl extends AbstractDaoImpl<Stock> implements StockDao {
    private String tableName = "Stock";

    public StockDaoImpl() {
        setThisClass(Stock.class);
    }

    @Override
    public List list() {
        return list(tableName);
    }
}