package com.demo.store.web.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
@Entity
@Table(name="TradeItem")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeItem  implements Serializable {
    private static final long serialVersionUID = -7632746691749562076L;
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "tardeVersion")
    private Long tradeVersion;

    @Column(name= "counterPartyId")
    private String counterPartyId;

    @Column(name="bookId")
    private String bookId;

    @Column(name="maturityDate")
    private Date maturityDate;

    @Column(name="createDate")
    private Date createDate;

    @Column(name="expiredFlag")
    private String expiredFlag;//this field will have length as 1 in database

    public TradeItem() {
        super();
    }

    public TradeItem(Long tradId, Long tradeVersion, String counterPartyId, String bookId, Date maturityDate, Date createDate, String expiredFlag){
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
