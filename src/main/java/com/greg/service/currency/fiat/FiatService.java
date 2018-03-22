package com.greg.service.currency.fiat;

import com.greg.dao.fiat.FiatDao;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.holding.fiat.Fiat;
import com.greg.service.currency.CurrencyService;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */

@Service
public class FiatService {

    private final FiatDao fiatDao;
    private final CurrencyService currencyService;

    @Autowired
    public FiatService(FiatDao fiatDao, CurrencyService currencyService) {
        this.currencyService = currencyService;
        this.fiatDao = fiatDao;
    }

    public Fiat get(String fiatAcronym) {
        return fiatDao.get(fiatAcronym);
    }

    public List<Fiat> list() {
        List<Fiat> fiatList = fiatDao.list();
        fiatList.forEach(item -> item.setHoldingType(HoldingType.FIAT));

        return fiatList;
    }

    public Map<Date, Double> getFiatHistory(String acronym, double quantity) throws UnirestException {
        return currencyService.getCurrencyHistory(acronym, quantity);
    }
}
