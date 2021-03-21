package com.demo.store.web.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeItem {
    private Long tradId;
    private Long tradeVersion;
    private String counterPartyId;
    private String bookId;
    private Date maturityDate;
    private Date createDate;
    private String expiredFlag;//this field will have length as 1 in database

    public TradeItem() {
        super();
    }

    public TradeItem(Long tradId,Long tradeVersion,String counterPartyId,String bookId,Date maturityDate,Date createDate,String expiredFlag){
        this.tradId = tradId;
        this.tradeVersion = tradeVersion;
        this.counterPartyId = counterPartyId;
        this.bookId = bookId;
        this.maturityDate = maturityDate;
        this.createDate = createDate;
        this.expiredFlag = expiredFlag;
    }

    public void setTradId(Long tradId){
        this.tradId = tradId;
    }
    public Long getTradId(){
        return tradId;
    }
    public void setTradeVersion(Long tradeVersion){
        this.tradeVersion = tradeVersion;
    }

    public Long getTradeVersion(){
        return tradeVersion;
    }
    public void setCounterPartyId(String counterPartyId){
        this.counterPartyId = counterPartyId;
    }
    public String getCounterPartyId(){
        return  counterPartyId;
    }
    public void setBookId(String bookId){
        this.bookId = bookId;
    }
    public String getBookId(){
        return bookId;
    }
    public void setMaturityDate(Date maturityDate){
        this.maturityDate = maturityDate;
    }
    public Date getMaturityDate(){
        return maturityDate;
    }
    public void setCreateDate(Date createDate){
        this.createDate = createDate;
    }
    public Date getCreateDate(){
        return createDate;
    }
    public void setExpiredFlag(String expiredFlag){
        this.expiredFlag = expiredFlag;
    }
    public String getExpiredFlag(){
        return expiredFlag;
    }


}
