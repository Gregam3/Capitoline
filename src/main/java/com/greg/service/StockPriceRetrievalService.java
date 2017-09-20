package com.greg.service;

import com.greg.entity.holding.HoldingType;
import com.greg.exceptions.HoldingNotFoundException;
import org.springframework.stereotype.Service;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class StockPriceRetrievalService {
    public List<Stock> listAllStocks() {
        throw new AssertionError("Not yet implemented");
    }

    public Stock getStockById(String stockId) throws IOException, HoldingNotFoundException {
        Stock stock = YahooFinance.get(stockId);

        if (stock != null) return stock;
        else throw new HoldingNotFoundException(HoldingType.STOCK);
    }
}
