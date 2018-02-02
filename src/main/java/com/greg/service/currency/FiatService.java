package com.greg.service.currency;

import com.greg.entity.currency.Currency;
import com.greg.service.AbstractService;

import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */

public class CurrencyService extends AbstractService{
    
    private final String tableName;

    @Override
    public List<Currency> list() {
        entityManager.createNamedQuery()
    }
}
