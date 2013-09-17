package com.patricksalami.model;

import java.util.Date;

public interface BrokerOrder {
	public Long getId();
	public String getSymbol();
	public Float getPercentOfAccount();
	public Long getCloseByDate();
	public Float getStopPercent();
	public String getExchange();
	public String getCurrency();
	public String getType();
	public void setOpeningPrice(Double openingPrice);
	public Double getOpeningPrice();
	public void setClosingPrice(Double closingPrice);
	public Double getClosingPrice();
	public void setRealizedPnl(Double realizedPnl);
	public Double getRealizedPnl();
	public int getActionIndex();
	public void setClosed(Date d);
	public void persist();
	
}
