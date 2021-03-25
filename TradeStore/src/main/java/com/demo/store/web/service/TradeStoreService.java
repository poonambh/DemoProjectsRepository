package com.demo.store.web.service;

import com.demo.store.web.beans.TradeItemDto;
import com.demo.store.web.entity.TradeItem;

public interface TradeStoreService {

    public void insertRecordService(TradeItemDto tradeItem);
    public TradeItemDto getTradeRecordService(Long tradeId);

}
