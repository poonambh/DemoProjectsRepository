package com.demo.store.web.service;

import com.demo.store.web.beans.TradeItem;
import com.demo.store.web.dao.TradeStoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;


@Service
public class TradeStoreServiceImpl implements  TradeStoreService{

    @Autowired
    private TradeStoreRepository tradeStoreRepository;

    @Override
    @Transactional
    public void insertRecordService(TradeItem tradeItemDto) {
        tradeStoreRepository.save(tradeItemDto);
    }

    @Override
    public TradeItem getTradeRecordService(Long tradeId) {
       return  tradeStoreRepository.findByTradeId(tradeId);
    }
}
