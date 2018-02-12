package com.greg.service.stock;

import com.greg.dao.fiat.FiatDao;
import com.greg.dao.stock.StockDao;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.holding.stock.Stock;
import com.greg.exceptions.HoldingNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class StockService {

    private final StockDao stockDao;

    @Autowired
    public StockService(StockDao stockDao) {
        this.stockDao = stockDao;
    }

    public List<Stock> list() {
        return stockDao.list();
    }
}
