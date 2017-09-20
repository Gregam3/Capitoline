package com.greg.controller.stock;

import com.greg.exceptions.HoldingNotFoundException;
import com.greg.service.StockPriceRetrievalService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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
        try {
            return new ResponseEntity<>(stockPriceRetrievalService.getStockById(id), HttpStatus.OK);
        } catch (IOException e) {
            LOG.error(e);
            return new ResponseEntity<>("Holding could not be found: " +e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (HoldingNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}