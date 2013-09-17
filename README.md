# Interactive Brokers REST Service

This is a RESTful service that interfaces with the Interactive Brokers (IB) API. Any client that can make HTTP requests can call this service to make trades, without a need to implement the full IB API on the client. A long (short) position is opened by the service using a limit order, in response to a simple REST call. The parameters indicate the asset to buy (sell), the duration for which to keep the position open and the percentage of the portfolio to allocate to the position. The service will first calculate the number of shares to buy (sell) and then checks whether or not sufficient cash is available in the account to fill the position. This service is particularly useful for algorithmic trading of assets in response to certain real-time events.
This is a work in progress. Future iterations will include support for stop-loss orders as well as additional exit conditions (maunually exiting a position, exiting a position if certain market conditions are met, etc).

The service is currently optimized for trading equities but other asset classes are be possible as well.

## Prerequisites
* An instance of Trader Workstation (TWS) or the IB gateway that is signed into your IB account and accepting API calls
* A MySQL instance
* Maven and JDK 7+ to build / run the project

## Installation and Running

To run the project in tomcat, check out the repo from Git and change to the project's root directory. Then, do the following:

````
mvn tomcat:run
````

This will start a tomcat container using the Maven tomcat plugin, running on local port 8080, and build and run the broker service within the tomcat container.

This is a Spring Roo project (https://github.com/spring-projects/spring-roo). To edit the source code, it is recommended to use the SpringSource Tool Suite IDE (STS) (http://spring.io/tools), which is based on Eclipse. However, STS is not needed to run the service.

## Configuration
The MySQL connection configuration needs to be adjusted to match your local environment before running the service. You can find the MySQL settings in src/main/resources/META-INF/spring/database.properties. You will need to create an empty MySQL database with the name that you configure in the database.properties file with the configured credentials.
Additionally, the host and port of TWS or the IB Gateway can be configured. By default, they are set to run on localhost, port 7496. If your TWS or IB Gateway is running somewhere else, change the TWS_HOST and TWS_PORT parameters in com.patricksalami.broker.IbBroker.

## API

#### /buy
opens a new long position for the specified symbol
Parameters:

* (required) symbol - the contract to buy
* (required) percentOfAccount - percentage of portfolio to allocate to this position
* (required) closeByDate - UNIX timestamp indicating when to exit this position
* type - the security type. Valid values are: STK, FUT, IND, FOP. *Default: STK*
* exchange - the exchange on which to place the order. *Default: SMART*
* currency - the currency of the contract. *Default: USD*

Example:
````
curl -X POST --data "{symbol:AAPL, closeByDate: 1379400147000, percentOfAccount: 3}" \
    -H "Content-Type:application/json" -H "Accept:application/json" http://localhost:8080/broker/buy
````

#### /sell
opens a new short position for the specified symbol
Parameters:

* (required) symbol - the contract to sell short
* (required) percentOfAccount - percentage of portfolio to allocate to this position
* (required) closeByDate - UNIX timestamp indicating when to exit this position
* type - the security type. Valid values are: STK, FUT, IND, FOP. *Default: STK*
* exchange - the exchange on which to place the order. *Default: SMART*
* currency - the currency of the contract. *Default: USD*

Example:
````
curl -X POST --data "{symbol:GOOG, closeByDate: 1379400147000, percentOfAccount: 3}" \
    -H "Content-Type:application/json" -H "Accept:application/json" http://localhost:8080/broker/sell
````

#### /admin/closePositions
Call this service periodically with no parameters to close positions whose closeByDate has passed.

Example:
````
curl -X GET -H "Content-Type:application/json" -H "Accept:application/json" http://localhost:8080/broker/admin/closePositions
````


_see https://www.interactivebrokers.com/en/software/api/api.htm for full IB API reference_