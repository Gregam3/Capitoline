package com.greg.service.stock;

import com.greg.dao.stock.StockDao;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.holding.stock.Stock;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class StockService {

    private static final String ALPHA_VANTAGE_API_KEY = "QVJRID55FX6HALQH";
    private static final String ALPHA_VANTAGE_BATCH_QUOTE_URL = "https://www.alphavantage.co/query?function=BATCH_STOCK_QUOTES&symbols=";

    private final StockDao stockDao;

    @Autowired
    public StockService(StockDao stockDao) {
        this.stockDao = stockDao;
    }

    public List<Stock> list() {
        List<Stock> stockList = stockDao.list();
        stockList.forEach(item -> item.setHoldingType(HoldingType.STOCK));

        return stockList;
    }

    public double getStockPrice(String acronym) throws UnirestException {
        JSONObject object = Unirest.get(ALPHA_VANTAGE_BATCH_QUOTE_URL + acronym + "&apikey=" + ALPHA_VANTAGE_API_KEY).asJson().getBody().getObject();

        return ((JSONObject)(((JSONArray) object.get("Stock Quotes")).get(0))).getDouble("2. price");
    }
}
