package com.demo.store.web.service;

import com.demo.store.web.beans.TradeItem;

import java.util.List;

public interface TradeStoreService {

    public void insertRecordService(TradeItem tradeItemDto);
    public TradeItem getTradeRecordService(Long tradeId);

}
