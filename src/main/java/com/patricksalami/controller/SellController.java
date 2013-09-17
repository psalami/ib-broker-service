package com.patricksalami.controller;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.patricksalami.background.PositionCloser;
import com.patricksalami.broker.IbBroker;
import com.patricksalami.model.Buy;
import com.patricksalami.model.Sell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/sell")
@Controller
@RooWebScaffold(path = "sells", formBackingObject = Sell.class)
@RooWebJson(jsonObject = Sell.class)
public class SellController {
	
	@Autowired
	private IbBroker ibBroker;
	
	private ScheduledExecutorService scheduler;

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) throws InterruptedException{
		HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text");
        Sell sell = Sell.fromJsonToSell(json);
        
        System.out.println("received sell order for " + sell.getSymbol());
        
        if(!ibBroker.isInitialized()){
        	System.out.println("broker not yet initialized. Initializing now...");
        	ibBroker.initialize();
        	Thread.sleep(5000);
        }
        
        
        
        ConcurrentHashMap<String,Integer> portfolio = ibBroker.getPortfolio();
		if(portfolio.containsKey(sell.getSymbol()) && portfolio.get(sell.getSymbol()) != 0){
			System.out.println("WARN: we already have a position for symbol: " + sell.getSymbol() + " with " + portfolio.get(sell.getSymbol()) + " shares");
			return new ResponseEntity<String>(headers, HttpStatus.METHOD_NOT_ALLOWED);
		}
		
		System.out.println("proceeding with sell order");
		sell.persist();
		
		System.out.println("persisted to DB");
		
        try{
        	ibBroker.sellOrder(sell);
        	System.out.println("submitted to system");
        }catch(Exception e){
        	System.out.println("exception: " + e + ": " + e.getLocalizedMessage());
        	e.printStackTrace();
        }
        
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }
	
	@RequestMapping(method = RequestMethod.GET, value="initialize")
	public ResponseEntity<String> initialize() throws InterruptedException{
        if(!ibBroker.isInitialized()){
        	System.out.println("broker not yet initialized. Initializing now...");
        	ibBroker.initialize();
        	Thread.sleep(5000);
        }
        return new ResponseEntity<String>("{result:\"ok\"}", HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "startPositionCloser")
	public ResponseEntity<String> startPositionCloser() throws InterruptedException{
		System.out.println("starting positions closer");
		PositionCloser pc = new PositionCloser(ibBroker);
		scheduler = Executors.newSingleThreadScheduledExecutor();
		ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(pc, 1, 30, TimeUnit.SECONDS);
		//scheduler.awaitTermination(20, TimeUnit.SECONDS);
		return new ResponseEntity<String>("{result:\"ok\"}", HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "stopPositionCloser")
	public ResponseEntity<String> stopPositionCloser(){
		if(scheduler == null){
			return new ResponseEntity<String>("{result:\"failed\", reason:\"PositionCloser not started; you must start the PositionCloser by calling /startPositionCloser before it can be stopped\"}", HttpStatus.CONFLICT);
		}
		scheduler.shutdown();
		return new ResponseEntity<String>("{result:\"ok\"}", HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "closePositions")
	public ResponseEntity<String> closePositions() throws InterruptedException{
		
		if(!ibBroker.isInitialized()){
        	ibBroker.initialize();
        }
		
		PositionCloser pc = new PositionCloser(ibBroker);
		int totalClosed = pc.closePositions();
		
		return new ResponseEntity<String>("{result:\"ok\", totalClosed:" + totalClosed + "}", HttpStatus.OK);
	}
	
	
	/*
	@RequestMapping(method = RequestMethod.GET, value = "testPositionCloser")
	public ResponseEntity<String> testPositionCloser(){
		  List<Buy> buys = Buy.findBuysByClosedIsNullAndSentIsNotNullAndCloseByDateLessThan(new Date().getTime()).getResultList();
		for(Buy s : buys){
			System.out.println("closing sell " + s.getSymbol());
			//close(s);
			Date date = new Date();
			System.out.println("setting closed date for " + s.getSymbol() + " to " + date);
			s.setClosed(date);
			s.persist();
			s.flush();
			
		}
		return new ResponseEntity<String>("{result:\"ok\"}", HttpStatus.OK);
	}*/
	
}
