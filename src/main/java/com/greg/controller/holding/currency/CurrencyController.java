package com.greg.controller.holding.currency;

import com.greg.service.currency.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@RequestMapping("/currency/")
@RestController
public class CurrencyController {
    private CurrencyService currencyService;

    @Autowired
    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

//    @GetMapping("get/batch-prices")
//    public ResponseEntity<?> getBatchPrices()  {
//        try {
//            return new ResponseEntity<>(currencyService.getBatchPricesForUser(), HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }
}