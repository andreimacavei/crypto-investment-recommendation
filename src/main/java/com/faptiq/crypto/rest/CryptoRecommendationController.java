package com.faptiq.crypto.rest;

import com.faptiq.crypto.pojo.CryptoDetails;
import com.faptiq.crypto.pojo.CryptoSymbol;
import com.faptiq.crypto.service.CryptoRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CryptoRecommendationController {

    private final CryptoRecommendationService cryptoService;

    @Autowired
    public CryptoRecommendationController(CryptoRecommendationService cryptoRecommendationService) {
        cryptoService = cryptoRecommendationService;
    }


    @GetMapping(path="/sorted", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<String> getCryptoSortedByNormalizedRange() {
        // get a list of all cryptos sorted using a normalized range (max-min)/min
        List<CryptoSymbol> cryptoSymbols = cryptoService.getCryptoSortedByNormalizedRange();
        return cryptoSymbols.stream().map(CryptoSymbol::toString).toList();
    }

    @GetMapping(path="/details", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getCryptoDetails(@RequestParam String symbol) {
        CryptoDetails cryptoDetails = null;
        try {
            cryptoDetails = cryptoService.getCryptoDetails(CryptoSymbol.valueOf(symbol));
            if (cryptoDetails == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(cryptoDetails, HttpStatus.OK);
    }


    @GetMapping(path="/highest", produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getHighestNormalizedRange(@RequestParam String date) {
        CryptoSymbol cryptoSymbol = null;
        try {
            LocalDate localDate = LocalDate.parse(date);
            if (!cryptoService.validate(localDate)) {
                return new ResponseEntity<>("No records found for date " + date, HttpStatus.NOT_FOUND);
            }
            cryptoSymbol = cryptoService.getHighestNormalizedRange(localDate);

        } catch (DateTimeParseException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(cryptoSymbol.toString(), HttpStatus.OK);
    }
}
