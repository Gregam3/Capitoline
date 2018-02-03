package com.greg.controller.fiat;

import com.greg.entity.currency.Fiat;
import com.greg.service.currency.FiatService;
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
@RequestMapping("/fiat/")
public class FiatController {
    private FiatService fiatService;

    @Autowired
    public FiatController(FiatService fiatService) {
        this.fiatService = fiatService;
    }

    @GetMapping("{acronym}")
    public ResponseEntity<Fiat> getFiatDetails(@PathVariable("acronym") String acronym) {
        return new ResponseEntity<>(fiatService.get(acronym), HttpStatus.OK);
    }

}
