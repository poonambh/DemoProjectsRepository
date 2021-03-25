package com.demo.store.web.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeItemDto {
    private Long id;
    private Long tradeVersion;
    private String counterPartyId;
    private String bookId;
    private Date maturityDate;
    private Date createDate;
    private String expiredFlag;//this field will have length as 1 in database

    public TradeItemDto() {
        super();
    }

    public TradeItemDto(Long id, Long tradeVersion, String counterPartyId, String bookId, Date maturityDate, Date createDate, String expiredFlag){
        this.id = id;
        this.tradeVersion = tradeVersion;
        this.counterPartyId = counterPartyId;
        this.bookId = bookId;
        this.maturityDate = maturityDate;
        this.createDate = createDate;
        this.expiredFlag = expiredFlag;
    }

    public void setId(Long tradId){
        this.id = tradId;
    }
    public Long getId(){
        return id;
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
