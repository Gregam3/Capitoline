package com.greg.service.currency;

import com.greg.dao.fiat.FiatDao;
import com.greg.entity.holding.fiat.Fiat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */

@Service
public class FiatService {

    private final String tableName = "PT_CURRENCY";

    private final FiatDao fiatDao;

    @Autowired
    public FiatService(FiatDao fiatDao) {
        this.fiatDao = fiatDao;
    }

    public Fiat get(String fiatAcronym) {
        return fiatDao.get(fiatAcronym);
    }

    public List<Fiat> list() {
        return (List<Fiat>) fiatDao.list();
    }
}
