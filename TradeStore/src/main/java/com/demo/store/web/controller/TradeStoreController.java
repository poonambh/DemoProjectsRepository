package com.demo.store.web.controller;

import com.demo.store.web.CustomException;
import com.demo.store.web.beans.TradeItemDto;
import com.demo.store.web.service.TradeStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/traderecord")
@Configuration
@PropertySource(value = "application.properties", ignoreResourceNotFound = true)
public class TradeStoreController {

    @Autowired
    private TradeStoreService tradeStoreService;

    @PostMapping
    @RequestMapping("/create")
    public ResponseEntity<Object> createTradeRecord(@RequestBody TradeItemDto tradeItemDto) throws Exception{
        TradeItemDto tradeItem = tradeStoreService.getTradeRecordService(tradeItemDto.getId());
        if(tradeItem!=null) {
            validate(tradeItem.getTradeVersion(), tradeItem.getMaturityDate(), tradeItemDto);
        }

        tradeStoreService.insertRecordService(tradeItemDto);
        return ResponseEntity.ok().build();
    }

    private void validate(Long tradeVersion, Date maturityDate, TradeItemDto tradeItemDto) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date todayDate = sdf.parse(sdf.format(new Date() ));

        if(tradeItemDto.getTradeVersion() < tradeVersion){
            throw new CustomException("Trade rejected because version found lower than existing trade version");
        }
        if(tradeItemDto.getMaturityDate().compareTo(todayDate) < 0){
            throw new CustomException("Trade rejected as maturity date is passed already ");
        }
    }

}
