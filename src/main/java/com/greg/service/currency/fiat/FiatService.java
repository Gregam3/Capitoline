package com.greg.service.currency.fiat;

import com.greg.dao.fiat.FiatDao;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.holding.fiat.Fiat;
import com.greg.service.currency.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

//    public String removeFiatNotOnApi() throws UnirestException {
//        List<Fiat> list = list();
//        StringBuilder stringBuilder = new StringBuilder();
//
//        for (Fiat fiat : list) {
//            if(currencyService.getCurrentPrice(fiat.getAcronym(), "USD") == -1) {
//                stringBuilder.append(fiat.getAcronym());
//                fiatDao.delete(fiat.getAcronym());
//            }
//        }
////
////        String[] deleteThis = {
////                "SVC USD",
////                "UYU UYI",
////                "ZAR LSL",
////                "ZAR NAD"
////        };
////
////        for (String deleteThi : deleteThis) {
////            fiatDao.delete(deleteThi);
////        }
////
//        return stringBuilder.toString();
//    }
}
