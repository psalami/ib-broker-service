package com.patricksalami.controller;

import java.util.concurrent.ConcurrentHashMap;

import com.patricksalami.broker.IbBroker;
import com.patricksalami.model.Buy;

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

@RequestMapping("/buy")
@Controller
@RooWebScaffold(path = "buys", formBackingObject = Buy.class)
@RooWebJson(jsonObject = Buy.class)
public class BuyController {
	
	public static Logger LOG = LoggerFactory.getLogger(BuyController.class);
	
	@Autowired
	private IbBroker ibBroker;

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) throws InterruptedException{
		
		HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text");
        Buy buy = Buy.fromJsonToBuy(json);
        
        LOG.info("received buy order for " + buy.getSymbol());

        if(!ibBroker.isInitialized()){
        	LOG.info("broker not yet initialized. Initializing now...");
        	ibBroker.initialize();
        	Thread.sleep(5000);
        }
        
		ConcurrentHashMap<String,Integer> portfolio = ibBroker.getPortfolio();
		if(portfolio.containsKey(buy.getSymbol()) && portfolio.get(buy.getSymbol()) != 0){
			LOG.warn("we already have a position for symbol: " + buy.getSymbol() + " with " + portfolio.get(buy.getSymbol()) + " shares");
			return new ResponseEntity<String>(headers, HttpStatus.METHOD_NOT_ALLOWED);
		}
		buy.persist();
        try{
        	ibBroker.buyOrder(buy);
        }catch(Exception e){
        	LOG.error("exception: " + e + ": " + e.getLocalizedMessage());
        	e.printStackTrace();
        }
        
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }
}
