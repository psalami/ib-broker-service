package com.patricksalami.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	public static Logger LOG = LoggerFactory.getLogger(AdminController.class);
	
	@Autowired
	private IbBroker ibBroker;
	
	/**
	 * This call will cause all open positions to be examined to see if their
	 * closeByDate has been reached. All positions for which the closeByDate
	 * has been reached that are still open will be closed.
	 * 
	 * @return the number of positions that were closed
	 * @throws InterruptedException
	 */
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
