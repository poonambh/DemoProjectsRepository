package com.demo.store.web.dao;

import com.demo.store.web.beans.TradeItemDto;
import com.demo.store.web.entity.TradeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeStoreRepository extends JpaRepository<TradeItem,String> {

public TradeItem findById(Long tardeId);

}
