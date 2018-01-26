package com.greg.controller.stock;

import com.greg.service.stock.StockPriceRetrievalService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@RestController
@RequestMapping("/stock/")
public class StockController {

    private static final Logger LOG = Logger.getLogger(StockController.class);

    private final StockPriceRetrievalService stockPriceRetrievalService;

    @Autowired
    public StockController(StockPriceRetrievalService stockPriceRetrievalService) {
        this.stockPriceRetrievalService = stockPriceRetrievalService;
    }

    @GetMapping("{id}")
    public ResponseEntity getStockById(@PathVariable("id") String id) {
            return new ResponseEntity(HttpStatus.OK);
    }
}