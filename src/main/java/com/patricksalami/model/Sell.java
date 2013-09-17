package com.patricksalami.model;

import java.util.Date;
import javax.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJson
@RooEntity(finders = { "findSellsByClosedIsNullAndSentIsNotNullAndCloseByDateLessThan" })
public class Sell implements BrokerOrder {
	
	private final int actionIndex = 2;

    @NotNull
    private String symbol;

    @NotNull
    private Float percentOfAccount;

    private Float stopPercent;

    private Long closeByDate;

    private int numContracts;

    @DateTimeFormat(style = "M-")
    private Date sent;

    @DateTimeFormat(style = "M-")
    private Date closed;
    
    private Double openingPrice;
    private Double closingPrice;
    private Double realizedPnl;

    private String exchange = "SMART";
	private String type = "STK";
	private String currency = "USD";
	
	
    
    
}
