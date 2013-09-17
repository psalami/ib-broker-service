package com.patricksalami.broker;

public class BrokerDriver {
	
	public static void main(String[] args) throws Exception{
		IbBroker broker = new IbBroker();
		broker.initialize();
		/*Float liquidation = broker.getNetLiquidationValue();
		System.out.println("liquidation value: " + liquidation);*/
		//System.out.println("price: " + broker.getContractPrice("GOOG"));
		broker.wrapUp();
	}

}
