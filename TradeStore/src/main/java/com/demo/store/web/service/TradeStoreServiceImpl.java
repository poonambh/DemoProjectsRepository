package com.demo.store.web.service;

import com.demo.store.web.beans.TradeItemDto;
import com.demo.store.web.dao.TradeStoreRepository;
import com.demo.store.web.entity.TradeItem;
import com.demo.store.web.utils.ObjectMapperUtil;
import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;


@Service
public class TradeStoreServiceImpl implements  TradeStoreService{

    @Autowired
    private TradeStoreRepository tradeStoreRepository;

    @Override
    @Transactional
    public void insertRecordService(TradeItemDto tradeItemdto) {
        TradeItem tardeItem = (TradeItem) ObjectMapperUtil.map(tradeItemdto, TradeItem.class);
        tradeStoreRepository.save(tardeItem);
    }

    @Override
    public TradeItemDto getTradeRecordService(Long tradeId) {

        TradeItem tradeItem = tradeStoreRepository.findById(tradeId);
        TradeItemDto tradeItemDto = (TradeItemDto) ObjectMapperUtil.map(tradeItem, TradeItemDto.class);
        return tradeItemDto;
    }
}
