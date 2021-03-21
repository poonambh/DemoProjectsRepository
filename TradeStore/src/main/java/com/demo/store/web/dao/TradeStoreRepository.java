package com.demo.store.web.dao;

import com.demo.store.web.beans.TradeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradeStoreRepository extends JpaRepository<TradeItem,String> {

public TradeItem findByTradeId(Long tardeId);

}
