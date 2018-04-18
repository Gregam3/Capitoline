package com.greg.controller.holding.currency.fiat;

import com.greg.entity.holding.fiat.Fiat;
import com.greg.service.currency.fiat.FiatService;
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
@RequestMapping("/fiat/")
public class FiatController {
    private FiatService fiatService;


    private static final Logger LOG = Logger.getLogger(FiatController.class);
    @Autowired
    public FiatController(FiatService fiatService) {
        this.fiatService = fiatService;
    }

    @GetMapping("{acronym}")
    public ResponseEntity<Fiat> getFiatDetails(@PathVariable("acronym") String acronym) {
        return new ResponseEntity<>(fiatService.get(acronym), HttpStatus.OK);
    }

    @GetMapping("list")
    public ResponseEntity<List<Fiat>> getFiatList() {
        try {
            return new ResponseEntity<>(fiatService.list(), HttpStatus.OK);
        } catch (Exception e) {
            LOG.error(e);
            System.err.println(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

//    @GetMapping("delete")
//    public ResponseEntity<String> delete() {
//        try {
//            return new ResponseEntity<>(fiatService.removeFiatNotOnApi(), HttpStatus.OK);
//        } catch (Exception e) {
//            LOG.error(e);
//            System.err.println(e);
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//    }
}
