package com.greg.controller.crypto;

import com.greg.entity.holding.crypto.Crypto;
import com.greg.service.crypto.CryptoPriceRetrievalService;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@RestController
@RequestMapping("/crypto/")
public class CryptoController {

    private static final Logger LOG = Logger.getLogger(CryptoController.class);

    private final CryptoPriceRetrievalService cryptoPriceRetrievalService;

    @Autowired
    public CryptoController(CryptoPriceRetrievalService cryptoPriceRetrievalService) {
        this.cryptoPriceRetrievalService = cryptoPriceRetrievalService;
    }

    @GetMapping("/get/{cryptoId}/{userCurrency}")
    public ResponseEntity<Crypto> getStockById(@PathVariable("cryptoId") String cryptoId,
                                               @PathVariable("userCurrency") String userCurrency) throws UnirestException {
        return new ResponseEntity<>(cryptoPriceRetrievalService.retrieveCryptoPrice(cryptoId, userCurrency), HttpStatus.OK);
    }

    @GetMapping("/get/currencies-list")
    public ResponseEntity<List<String>> getCurrencyList() {
        return null;
    }
}