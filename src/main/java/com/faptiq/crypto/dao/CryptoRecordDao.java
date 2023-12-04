package com.faptiq.crypto.dao;

import com.faptiq.crypto.entity.CryptoRecord;
import com.faptiq.crypto.pojo.CryptoSymbol;

import java.util.List;

public interface CryptoRecordDao {

    List<CryptoRecord> findByCryptoSymbol(CryptoSymbol cryptoSymbol);

    CryptoRecord save(CryptoRecord record);

    void saveAll(List<CryptoRecord> records);
}
