package com.patricksalami.background;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.Order;
import com.ib.client.TickType;
import com.patricksalami.broker.IbBroker;
import com.patricksalami.model.BrokerOrder;
import com.patricksalami.model.Buy;
import com.patricksalami.model.Sell;

/**
 * This class is intended to go through open positions that are ready to be closed
 * based on their expiration time and closes the positions.
 * 
 * Background processes don't seem to work as expected in Spring Roo,
 * therefore it is necessary for now to have an external process that
 * periodically calls the closePositions() method of this class.  
 * 
 * @author psalami
 *
 */
public class PositionCloser implements Runnable{
	
	private IbBroker broker;
	
	public PositionCloser(IbBroker broker){
		this.broker = broker;
	}
	

	@Override
	public void run() {
		try{
			closePositions();	
		}catch(InterruptedException e){
			System.out.println("Exception while trying to close positions:" + e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	public int closePositions() throws InterruptedException{
		//System.out.println("checking for positions to close");
		List<Buy> buys = Buy.findBuysByClosedIsNullAndSentIsNotNullAndCloseByDateLessThan(new Date().getTime()).getResultList();
		List<Sell> sells = Sell.findSellsByClosedIsNullAndSentIsNotNullAndCloseByDateLessThan(new Date().getTime()).getResultList();
		int totalClosed = 0;
		HashSet<String> closedSymbols = new HashSet<String>();
		for(Sell s : sells){
			if(s.getNumContracts() < 1 || closedSymbols.contains(s.getSymbol())){
				continue;
			}
			//System.out.println("closing sell " + s.getSymbol());
			int numClosed = close(s);
			if(numClosed > 0){
				closedSymbols.add(s.getSymbol());
			}
			
			totalClosed++;
		}
		
		for(Buy b : buys){
			if(b.getNumContracts() < 1 || closedSymbols.contains(b.getSymbol())){
				continue;
			}
			//System.out.println("closing buy " + b.getSymbol());
			int numClosed = close(b);
			if(numClosed > 0){
				closedSymbols.add(b.getSymbol());
			}
			
			totalClosed++;
		}
		
		return totalClosed;
	
	} 
	
	
	public void setClosedBuy(Buy order){
		Date date = new Date();
		System.out.println("setting closed date for " + order.getSymbol() + " to " + date);
		order.setClosed(date);
		order.persist();
		order.flush();
	}
	
	public void setClosedSell(Sell order){
		Date date = new Date();
		System.out.println("setting closed date for " + order.getSymbol() + " to " + date);
		order.setClosed(date);
		order.persist();
		order.flush();
	}
	
	public int close(BrokerOrder order) throws InterruptedException{
        if(!broker.isInitialized()){
        	System.out.println("broker not yet initialized. Initializing now...");
        	broker.initialize();
        	Thread.sleep(5000);
        }
        
		int actionIndex = order.getActionIndex();
		int tickType;
		if(actionIndex == 1){
			actionIndex = 2;
			tickType = TickType.BID;
		}else if(actionIndex == 2){
			actionIndex = 1;
			tickType = TickType.ASK;
		}else{
			System.out.println("invalid actionIndex: " + actionIndex);
			return 0;
		}
		
		ConcurrentHashMap<String,Integer> portfolio = broker.getPortfolio();

		if(!portfolio.containsKey(order.getSymbol())){
			//System.out.println("WARN: we do not have a position for symbol: " + order.getSymbol());
			return 0;
		}
		
		int absNumShares = Math.abs(portfolio.get(order.getSymbol()));
		int numShares = portfolio.get(order.getSymbol());
		if( (actionIndex == 1 && numShares < 0) || (actionIndex == 2 && numShares > 0) || numShares == 0){
			if(absNumShares > 0){
				EClientSocket clientSocket = IbBroker.getClientSocket();
				
				Double price = broker.getContractPrice(order, tickType);
				Order ibo = broker.getOrder(actionIndex, absNumShares, price);
				Contract contract = broker.getContract(order);
				ConcurrentHashMap<String,Double> portfolioPnl = broker.getPortfolioPnl();
				portfolioPnl.put(order.getSymbol(), -1.0);
				clientSocket.placeOrder(ibo.m_orderId, contract, ibo);
				Date date = new Date();
				System.out.println("setting closed date for " + order.getSymbol() + " to " + date);
				order.setClosed(date);
				order.setClosingPrice(price);
				order.setRealizedPnl(broker.getPnl(order.getSymbol()));
				order.persist();
				System.out.println("closed position for " + absNumShares + " shares of " + order.getSymbol() + " (actionIndex = " + actionIndex + ")");
				return absNumShares;
			}else{
				//System.out.println("we have 0 shares for " + order.getSymbol() + "; skipping...");
			}
		} else{
			if(actionIndex == 1){
				System.out.println("order id: " + order.getId() + ": we are closing a sell position (buying to cover) but we have a positive number of shares (" + 
						numShares + ") for " + order.getSymbol() + "; skipping this position...");
			}else if(actionIndex == 2){
				System.out.println("order id: " + order.getId() + ": we are closing a buy position (selling) but we have a negative number of shares (" + 
						numShares + ") for " + order.getSymbol() + "; skipping this position...");
			}
		}
		return 0;
	}
	

	
}
