package com.greg.controller.holding.currency.crypto;

import com.greg.entity.holding.crypto.Crypto;
import com.greg.service.currency.CurrencyService;
import com.greg.service.currency.crypto.CryptoService;
import com.greg.service.user.UserService;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@RestController
@RequestMapping("/crypto/")
public class CryptoController {

    private static final Logger LOG = Logger.getLogger(CryptoController.class);

    private final CryptoService cryptoService;
    private final CurrencyService currencyService;
    private final UserService userService;

    @Autowired
    public CryptoController(CryptoService cryptoService,
                            CurrencyService currencyService,
                            UserService userService) {
        this.cryptoService = cryptoService;
        this.currencyService = currencyService;
        this.userService = userService;
    }

//    @GetMapping("/get/{cryptoId}/{userCurrency}")
//    public ResponseEntity<Crypto> getStockById(@PathVariable("cryptoId") String cryptoId,
//                                               @PathVariable("userCurrency") String userCurrency) throws UnirestException {
//        return new ResponseEntity<>(cryptoService.retrieveCryptoPrice(cryptoId, userCurrency), HttpStatus.OK);
//    }

    @GetMapping("/list")
    public ResponseEntity<List<Crypto>> getCurrencyList() throws UnirestException {
        return new ResponseEntity<>(cryptoService.list(), HttpStatus.OK);
    }

    @GetMapping("/get/BTC-benchmark")
    public ResponseEntity<?> getBTCBenchMark() throws UnirestException {
        try {
            return new ResponseEntity<>(currencyService.getValueAtDate("BTC",
                    new Date().getTime() - DateUtils.MILLIS_PER_DAY * 7,
                    userService.getCurrentUser().getSettings().getUserCurrency().getAcronym()), HttpStatus.OK);
        } catch (Exception e) {
            LOG.error(e);
            e.printStackTrace();
            return new ResponseEntity<>("Could not retrieve BTC value", HttpStatus.BAD_REQUEST);
        }
    }
}