package com.faptiq.crypto.dao;

import com.faptiq.crypto.entity.CryptoRecord;
import com.faptiq.crypto.pojo.CryptoSymbol;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class CryptoRecordDaoImpl implements CryptoRecordDao {

    private EntityManager entityManager;

    @Autowired
    public CryptoRecordDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<CryptoRecord> findByCryptoSymbol(CryptoSymbol cryptoSymbol) {
        TypedQuery<CryptoRecord> query = entityManager.createQuery("from CryptoRecord where symbol=:cryptoSymbol", CryptoRecord.class);
        query.setParameter("cryptoSymbol", cryptoSymbol);

        return query.getResultList();
    }

    @Override
    @Transactional
    public CryptoRecord save(CryptoRecord record) {
        return entityManager.merge(record);
    }

    @Override
    @Transactional
    public void saveAll(List<CryptoRecord> records) {
        for (CryptoRecord record: records) {
            entityManager.merge(record);
        }
    }
}
