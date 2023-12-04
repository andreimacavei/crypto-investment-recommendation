package com.faptiq.crypto.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CryptoDetails {
    CryptoSymbol cryptoSymbol;
    Double minValue;
    Double maxValue;

    Long oldestValue;
    Long newestValue;

    @JsonIgnore()
    Double normalizedRange;

    public CryptoDetails() {

    }

    public CryptoDetails(CryptoSymbol cryptoSymbol, Double minValue, Double maxValue, Long oldestValue, Long newestValue) {
        this.cryptoSymbol = cryptoSymbol;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.oldestValue = oldestValue;
        this.newestValue = newestValue;
        this.normalizedRange = (maxValue - minValue) / minValue;
    }

    public CryptoSymbol getCryptoSymbol() {
        return cryptoSymbol;
    }

    public void setCryptoSymbol(CryptoSymbol cryptoSymbol) {
        this.cryptoSymbol = cryptoSymbol;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Long getOldestValue() {
        return oldestValue;
    }

    public void setOldestValue(Long oldestValue) {
        this.oldestValue = oldestValue;
    }

    public Long getNewestValue() {
        return newestValue;
    }

    public void setNewestValue(Long newestValue) {
        this.newestValue = newestValue;
    }

    public Double getNormalizedRange() {
        return normalizedRange;
    }

    public void setNormalizedRange(Double normalizedRange) {
        this.normalizedRange = normalizedRange;
    }


}
