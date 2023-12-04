package com.faptiq.crypto.pojo;

import java.util.Comparator;

public class CryptoDetailsComparator implements Comparator<CryptoDetails> {
    @Override
    public int compare(CryptoDetails crypto1, CryptoDetails crypto2) {
        return -Double.compare(crypto1.getNormalizedRange(), crypto2.getNormalizedRange());
    }
}
