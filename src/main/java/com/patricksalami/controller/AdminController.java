package com.patricksalami.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.patricksalami.background.PositionCloser;
import com.patricksalami.broker.IbBroker;

@RequestMapping("/admin")
@Controller
public class AdminController {
	
	@Autowired
	private IbBroker ibBroker;
	
	@RequestMapping(method = RequestMethod.GET, value = "closePositions")
	public ResponseEntity<String> closePositions() throws InterruptedException{
		
		if(!ibBroker.isInitialized()){
        	ibBroker.initialize();
        }
		
		PositionCloser pc = new PositionCloser(ibBroker);
		int totalClosed = pc.closePositions();
		
		return new ResponseEntity<String>("{result:\"ok\", totalClosed:" + totalClosed + "}", HttpStatus.OK);
	}

}
