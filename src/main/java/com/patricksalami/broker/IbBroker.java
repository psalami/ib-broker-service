package com.patricksalami.broker;

import java.util.Date;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.springframework.stereotype.Component;

import com.ib.client.*;
import com.patricksalami.model.BrokerOrder;
import com.patricksalami.model.Buy;
import com.patricksalami.model.Sell;

@Component
public class IbBroker implements Broker {
	
	public static final String TWS_HOST = "localhost";
	
	public static final int TWS_PORT = 7496;
	
	public static EClientSocket clientSocket;

	private static Semaphore apiAccessSemaphore;
	 
	private static Double lastBalance = null;
	
	private Random rand = new Random();
	
	private ConcurrentHashMap<String,String> accountValues = new ConcurrentHashMap<String,String>();
	private ConcurrentHashMap<String,Integer> portfolio = new ConcurrentHashMap<String,Integer>();
	private String accountUpdateTime;
	private ConcurrentHashMap<Integer,Double> lastPrices = new ConcurrentHashMap<Integer,Double>();
	public ConcurrentHashMap<String,Double> portfolioPnl = new ConcurrentHashMap<String,Double>();
	private IbBrokerWrapper wrapper;
	

	private int clientId;
	
	private boolean initialized = false;
	
	private static final int MAX_SLEEP_MILLIS = 30000;
	
	public IbBroker(){

		apiAccessSemaphore = new Semaphore(1, true);

        wrapper = new IbBrokerWrapper(accountValues, 
        		portfolio, 
        		accountUpdateTime, 
        		lastPrices,
        		portfolioPnl);
        clientSocket = new EClientSocket(wrapper);
        clientSocket.eDisconnect();
        clientId = rand.nextInt();
        clientSocket.eConnect(TWS_HOST, TWS_PORT, clientId);
	}
	
	public void initialize(){
		clientSocket.reqAccountUpdates(true,"1");
		initialized = true;
	}
	
	public boolean isInitialized(){
		return this.initialized;
	}
	
	public void wrapUp(){
		clientSocket.reqAccountUpdates(false,"1");
		clientSocket.eDisconnect();
	}
	
	public Float getAvailableFunds() throws InterruptedException{
		while(accountValues.get("AvailableFunds") == null){
			//System.out.println("value is " + accountValues.get("AvailableFunds") + "; sleeping...");
			Thread.sleep(1000);
		}
		return Float.valueOf(accountValues.get("AvailableFunds"));
	}
	
	public Float getNetLiquidationValue() throws InterruptedException{
		while(accountValues.get("NetLiquidation") == null){
			Thread.sleep(1000);
		}
		return Float.valueOf(accountValues.get("NetLiquidation"));
	}
	
	public Float getCashBalance() throws InterruptedException{
		while(accountValues.get("TotalCashValue") == null){
			Thread.sleep(1000);
		}
		return Float.valueOf(accountValues.get("TotalCashValue"));
	}
	
	public Double getContractPrice(BrokerOrder order, int tickType) throws InterruptedException{
		
		
        Contract c = getContract(order);
        Integer id = rand.nextInt();
        lastPrices.put(id,-1.0);
        wrapper.setTickType(tickType);
        clientSocket.reqMktData(id, c, "", true);
        long sleepTime = 0;
		while(lastPrices.get(id) == -1.0){
			System.out.println("waiting for price for request id " + id);
			Thread.sleep(1000);
			sleepTime += 1000;
			if(sleepTime > MAX_SLEEP_MILLIS){
				System.out.println("timed out while waiting for price from request id " + id);
				break;
			}
		}
		Double price = lastPrices.get(id);
		lastPrices.remove(id);
		return price;
	}
	
	/*
	public Contract getContract(String symbol){
		Contract c = new Contract();
        c.m_exchange = "SMART";
        //c.m_primaryExch = "NASDAQ";
        c.m_localSymbol = symbol;
        c.m_currency = "USD";
        c.m_secType = "STK";
        return c;
	}*/
	/*
	public Contract getContract(String symbol){
		Contract c = new Contract();
        
        //hong kong
        c.m_exchange = "SEHK";
        c.m_primaryExch = "SEHK";
        
        //singapore
		c.m_exchange = "SGX";
        c.m_primaryExch = "SGX";
        
        c.m_localSymbol = symbol;
        c.m_currency = "SGD";
        c.m_secType = "STK";
        return c;
	}
	*/
	

	public Contract getContract(BrokerOrder order){
		Contract c = new Contract();
		c.m_exchange = order.getExchange();
		if(!order.getExchange().equals("SMART")){
			c.m_primaryExch = order.getExchange();
		}
        //c.m_primaryExch = "SGX";
        c.m_localSymbol = order.getSymbol();
        c.m_currency = order.getCurrency();
        c.m_secType = order.getType();
        System.out.println("symbol: " + c.m_localSymbol);
        System.out.println("exchange: " + c.m_exchange);
        System.out.println("primary exchange: " + c.m_primaryExch);
        System.out.println("currency: " + c.m_currency);
        
        return c;
	}
	
	public void buyOrder(Buy buyOrder) throws InterruptedException{
		System.out.println("starting buyOrder");
		Double price = getContractPrice(buyOrder, TickType.ASK);
		int numShares = getNumShares(buyOrder, price);
		if(numShares > 0){
			System.out.println("preparing to buy " + numShares + " shares");
			Order order = getOrder(1, numShares, price);
			Contract contract = getContract(buyOrder);
			clientSocket.placeOrder(order.m_orderId, contract, order);
			buyOrder.setNumContracts(numShares);
			buyOrder.setSent(new Date());
			buyOrder.setOpeningPrice(price);
			buyOrder.persist();
			System.out.println("placed buy order for " + numShares + " of " + buyOrder.getSymbol());
		}else{
			System.out.println("buy order not placed (0 shares requested)");
		}
		
	}
	
	public Order getOrder(int actionIndex, int numShares) throws InterruptedException{
		Order order = new Order();
		order.m_clientId = clientId;
		order.m_orderType = "MKT";
		if(actionIndex == 1){
			order.m_action = "BUY";
		}else if(actionIndex == 2){
			order.m_action = "SELL";
		}
		
		//int orderId = Math.abs(rand.nextInt());
		int orderId = getNextOrderId();
		System.out.println("order id: " + orderId);
		order.m_orderId = orderId;
		int permId = Math.abs(rand.nextInt());
		System.out.println("new order perm id: "+ permId);
		order.m_permId = permId;
		
		order.m_totalQuantity = numShares;
		order.m_outsideRth = true;
		order.m_tif = "GTC";
		order.m_lmtPrice = 0.0;
		order.m_auxPrice = 0.0;
		
		return order;
	}
	
	public Order getOrder(int actionIndex, int numShares, double limitPrice) throws InterruptedException{
		Order order = new Order();
		order.m_clientId = clientId;
		order.m_orderType = "LMT";
		//limitPrice = (double) ((int)(limitPrice*100)) / 100;
		System.out.println("using calculated limit price: " + limitPrice);
		order.m_lmtPrice = limitPrice;
		order.m_overridePercentageConstraints = true;
		if(actionIndex == 1){
			order.m_action = "BUY";
		}else if(actionIndex == 2){
			order.m_action = "SELL";
		}
		
		//int orderId = Math.abs(rand.nextInt());
		int orderId = getNextOrderId();
		System.out.println("order id: " + orderId);
		order.m_orderId = orderId;
		int permId = Math.abs(rand.nextInt());
		System.out.println("new order perm id: "+ permId);
		order.m_permId = permId;
		
		order.m_totalQuantity = numShares;
		order.m_outsideRth = true;
		order.m_tif = "GTC";
		
		return order;
	}
	
	private int getNextOrderId() throws InterruptedException{
		
		wrapper.setNextOrderId(0);
		do{
			clientSocket.reqIds(1);
			Thread.sleep(500);
		}while(wrapper.getNextOrderId() == 0);
		
		return wrapper.getNextOrderId();
		
	}
	
	public int getNumShares(BrokerOrder order, Double price) throws InterruptedException{
		Float liquidation = getNetLiquidationValue();
		System.out.println("liquidation value: " + liquidation);
		Float cash = getCashBalance();
		System.out.println("cash value: " + cash);
		Float investment = (liquidation / 100f) * order.getPercentOfAccount();
		System.out.println("investment: " + investment);
		
		if(price == -1.0){
			System.out.println("unable to obtain share price");
			return 0;
		}
		System.out.println("price: " + price);
		if(investment > cash - BrokerSettings.MIN_CASH_BALANCE){
			System.out.println("not enough cash on hand for investment");
			return 0;
		}
		int numShares = (int) (investment / price);
		System.out.println("num shares: " + numShares);
		return numShares;
	}
	
	public void sellOrder(Sell sellOrder) throws InterruptedException{
		System.out.println("starting sellOrder");
		Double price = getContractPrice(sellOrder, TickType.BID);
		int numShares = getNumShares(sellOrder, price);
		if(numShares > 0){
			System.out.println("preparing to sell " + numShares + " shares");
			Order order = getOrder(2, numShares, price);
			Contract contract = getContract(sellOrder);
			clientSocket.placeOrder(order.m_orderId, contract, order);
			sellOrder.setNumContracts(numShares);
			sellOrder.setSent(new Date());
			sellOrder.setOpeningPrice(price);
			sellOrder.persist();
			System.out.println("placed sell order for " + numShares + " of " + sellOrder.getSymbol());
		}else{
			System.out.println("sell order not placed (0 shares requested)");
		}
		
		
	}
	
	public Double getPnl(String symbol) throws InterruptedException{
		long sleepTime = 0;
		System.out.println("waiting for P&L for " + symbol);
		while(portfolioPnl.get(symbol) == -1.0){
			sleepTime += 1000;
			if(sleepTime > MAX_SLEEP_MILLIS){
				System.out.println("timed out while waiting for P&L for symbol " + symbol);
				return null;
			}
			Thread.sleep(1000);
		}
		Double pnl = portfolioPnl.get(symbol);
		System.out.println("got P&L for symbol " + symbol + ": " + pnl);
		return pnl;
		
	}
	
	public void setLastBalance(double lastBalance){
		this.lastBalance = lastBalance;
	}
	
	public ConcurrentHashMap<String,Integer> getPortfolio(){
		return this.portfolio;
	}
	
	public ConcurrentHashMap<String, Double> getPortfolioPnl() {
		return portfolioPnl;
	}

	public static EClientSocket getClientSocket() {
		return clientSocket;
	}
	
	
	
}
