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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	public static Logger LOG = LoggerFactory.getLogger(SellController.class);
	
	@Autowired
	private IbBroker ibBroker;
	
	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) throws InterruptedException{
		HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text");
        Sell sell = Sell.fromJsonToSell(json);
        
        LOG.info("received sell order for " + sell.getSymbol());
        
        if(!ibBroker.isInitialized()){
        	LOG.info("broker not yet initialized. Initializing now...");
        	ibBroker.initialize();
        	Thread.sleep(5000);
        }
        
        
        
        ConcurrentHashMap<String,Integer> portfolio = ibBroker.getPortfolio();
		if(portfolio.containsKey(sell.getSymbol()) && portfolio.get(sell.getSymbol()) != 0){
			LOG.warn("we already have a position for symbol: " + sell.getSymbol() + " with " + portfolio.get(sell.getSymbol()) + " shares");
			return new ResponseEntity<String>(headers, HttpStatus.METHOD_NOT_ALLOWED);
		}
		
		sell.persist();
		
		LOG.info("persisted to DB");
		
        try{
        	ibBroker.sellOrder(sell);
        	LOG.info("submitted to system");
        }catch(Exception e){
        	LOG.error("exception: " + e + ": " + e.getLocalizedMessage());
        	e.printStackTrace();
        }
        
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }
	
	@RequestMapping(method = RequestMethod.GET, value="initialize")
	public ResponseEntity<String> initialize() throws InterruptedException{
        if(!ibBroker.isInitialized()){
        	LOG.info("broker not yet initialized. Initializing now...");
        	ibBroker.initialize();
        	Thread.sleep(5000);
        }
        return new ResponseEntity<String>("{result:\"ok\"}", HttpStatus.OK);
	}
	
}
