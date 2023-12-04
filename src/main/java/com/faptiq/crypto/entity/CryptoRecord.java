package com.faptiq.crypto.entity;

import com.faptiq.crypto.pojo.CryptoSymbol;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class CryptoRecord {
    @Id
    @GeneratedValue
    private Long id;
    private Long timestamp;
    private CryptoSymbol symbol;
    private Double price;
    private LocalDate date;

    public CryptoRecord() {

    }

    public CryptoRecord(Long timestamp, CryptoSymbol cryptoSymbol, Double price, LocalDate date) {
        this.timestamp = timestamp;
        this.symbol = cryptoSymbol;
        this.price = price;
        this.date = date;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public CryptoSymbol getSymbol() {
        return symbol;
    }

    public void setSymbol(CryptoSymbol cryptoSymbol) {
        this.symbol = cryptoSymbol;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }


    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

}
