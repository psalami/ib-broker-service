// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.patricksalami.model;

import java.lang.Double;
import java.lang.Float;
import java.lang.Long;
import java.lang.String;
import java.util.Date;

privileged aspect Sell_Roo_JavaBean {
    
    public int Sell.getActionIndex() {
        return this.actionIndex;
    }
    
    public String Sell.getSymbol() {
        return this.symbol;
    }
    
    public void Sell.setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public Float Sell.getPercentOfAccount() {
        return this.percentOfAccount;
    }
    
    public void Sell.setPercentOfAccount(Float percentOfAccount) {
        this.percentOfAccount = percentOfAccount;
    }
    
    public Float Sell.getStopPercent() {
        return this.stopPercent;
    }
    
    public void Sell.setStopPercent(Float stopPercent) {
        this.stopPercent = stopPercent;
    }
    
    public Long Sell.getCloseByDate() {
        return this.closeByDate;
    }
    
    public void Sell.setCloseByDate(Long closeByDate) {
        this.closeByDate = closeByDate;
    }
    
    public int Sell.getNumContracts() {
        return this.numContracts;
    }
    
    public void Sell.setNumContracts(int numContracts) {
        this.numContracts = numContracts;
    }
    
    public Date Sell.getSent() {
        return this.sent;
    }
    
    public void Sell.setSent(Date sent) {
        this.sent = sent;
    }
    
    public Date Sell.getClosed() {
        return this.closed;
    }
    
    public void Sell.setClosed(Date closed) {
        this.closed = closed;
    }
    
    public Double Sell.getOpeningPrice() {
        return this.openingPrice;
    }
    
    public void Sell.setOpeningPrice(Double openingPrice) {
        this.openingPrice = openingPrice;
    }
    
    public Double Sell.getClosingPrice() {
        return this.closingPrice;
    }
    
    public void Sell.setClosingPrice(Double closingPrice) {
        this.closingPrice = closingPrice;
    }
    
    public Double Sell.getRealizedPnl() {
        return this.realizedPnl;
    }
    
    public void Sell.setRealizedPnl(Double realizedPnl) {
        this.realizedPnl = realizedPnl;
    }
    
    public String Sell.getExchange() {
        return this.exchange;
    }
    
    public void Sell.setExchange(String exchange) {
        this.exchange = exchange;
    }
    
    public String Sell.getType() {
        return this.type;
    }
    
    public void Sell.setType(String type) {
        this.type = type;
    }
    
    public String Sell.getCurrency() {
        return this.currency;
    }
    
    public void Sell.setCurrency(String currency) {
        this.currency = currency;
    }
    
}
