package com.patricksalami.broker;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.TickType;
import com.ib.client.UnderComp;

public class IbBrokerWrapper implements EWrapper {
	
	ConcurrentHashMap<String,String> accountValues = new ConcurrentHashMap<String,String>();
	ConcurrentHashMap<String,Integer> portfolio = new ConcurrentHashMap<String,Integer>();
	String accountUpdateTime;
	public ConcurrentHashMap<Integer,Double> lastPrices = new ConcurrentHashMap<Integer,Double>();
	public ConcurrentHashMap<String,Double> portfolioPnl = new ConcurrentHashMap<String,Double>();
	private int tickType;
	
	private int nextOrderId;
	


	public IbBrokerWrapper(ConcurrentHashMap<String,String> accountValues, 
			ConcurrentHashMap<String,Integer> portfolio, 
			String accountUpdateTime,
			ConcurrentHashMap<Integer,Double> lastPrices,
			ConcurrentHashMap<String,Double> portfolioPnl){
		this.accountValues = accountValues;
		this.portfolio = portfolio;
		this.accountUpdateTime = accountUpdateTime;
		this.lastPrices = lastPrices;
		this.portfolioPnl = portfolioPnl;
	}
	

	@Override
	public void error(Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(String str) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionClosed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickPrice(int tickerId, int field, double price,
			int canAutoExecute) {
		// TODO Auto-generated method stub
		System.out.println("tickerId: " + tickerId + "; field: " + field + "; price: " + price);
		if(field == tickType){
			System.out.println("tickerId: " + tickerId + "; field: " + field + "; price: " + price);
			lastPrices.put(tickerId, price);
		}
		
		
	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
		// TODO Auto-generated method stub
		//System.out.println("tick size: tickerId: " + tickerId + "; field: " + field + "; size: " + size);
		
	}

	@Override
	public void tickOptionComputation(int tickerId, int field,
			double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta,
			double undPrice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureExpiry, double dividendImpact, double dividendsToExpiry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void orderStatus(int orderId, String status, int filled,
			int remaining, double avgFillPrice, int permId, int parentId,
			double lastFillPrice, int clientId, String whyHeld) {
		// TODO Auto-generated method stub
		System.out.println("order status: id: " + orderId +
				" status: " + status + " filled: " + filled + " remaining: " +
				remaining + " avgFillPrice: " + avgFillPrice + " permId: " + permId + 
				" parentId: " + parentId + " lastFillPrice: " + lastFillPrice + 
				" clientId: " + clientId + " whyHeld: " + whyHeld);
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order,
			OrderState orderState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openOrderEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAccountValue(String key, String value, String currency,
			String accountName) {
		// TODO Auto-generated method stub
		//System.out.println("key: " + key + "; value: " + value);
		accountValues.put(key,value);
		
	}

	@Override
	public void updatePortfolio(Contract contract, int position,
			double marketPrice, double marketValue, double averageCost,
			double unrealizedPNL, double realizedPNL, String accountName) {
		// TODO Auto-generated method stub
		this.portfolio.put(contract.m_localSymbol, position);
		this.portfolioPnl.put(contract.m_localSymbol, realizedPNL);
		
	}

	@Override
	public void updateAccountTime(String timeStamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accountDownloadEnd(String accountName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nextValidId(int orderId) {
		nextOrderId = orderId;
		
	}

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contractDetailsEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execDetailsEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation,
			int side, double price, int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMktDepthL2(int tickerId, int position,
			String marketMaker, int operation, int side, double price, int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message,
			String origExchange) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void managedAccounts(String accountsList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveFA(int faDataType, String xml) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalData(int reqId, String date, double open,
			double high, double low, double close, int volume, int count,
			double WAP, boolean hasGaps) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scannerParameters(String xml) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scannerData(int reqId, int rank,
			ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scannerDataEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void realtimeBar(int reqId, long time, double open, double high,
			double low, double close, long volume, double wap, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void currentTime(long time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fundamentalData(int reqId, String data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deltaNeutralValidation(int reqId, UnderComp underComp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickSnapshotEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
		// TODO Auto-generated method stub
		
	}


	public int getNextOrderId() {
		return nextOrderId;
	}


	public void setNextOrderId(int nextOrderId) {
		this.nextOrderId = nextOrderId;
	}
	

	public ConcurrentHashMap<String, Double> getPortfolioPnl() {
		return portfolioPnl;
	}


	public void setTickType(int tickType) {
		this.tickType = tickType;
	}



	
	

}
