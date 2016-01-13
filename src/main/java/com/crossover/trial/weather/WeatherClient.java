package com.crossover.trial.weather;

import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * A reference implementation for the weather client. Consumers of the REST API can look at WeatherClient
 * to understand API semantics. This existing client populates the REST endpoint with dummy data useful for
 * testing.
 *
 * @author code test administrator
 */
public class WeatherClient {
	
	public final static Logger logger = Logger.getLogger(WeatherClient.class.getName());

	//CR: Avoiding hard coding urls so as to achieve transparency
	//CR: after deployment
	//CR: The symptoms and consequences are quite synonymous to
	//CR: Embedded Navigational Information anti-pattern
    private static final String BASE_URI = "http://localhost:8080";
    /** end point for read queries */
    private WebTarget query;

    /** end point to supply updates */
    private WebTarget collect;

    public WeatherClient() {
        Client client = ClientBuilder.newClient();
        query = client.target(BASE_URI + "/query");
        collect = client.target("http://localhost:8080/collect");
    }

    //CR: Use logging utilities as against System.out.println
    //CR: as it affects application performance as regards I/O
    public void pingCollect() {
        WebTarget path = collect.path("/ping");
        Response response = path.request().get();
        logger.info("collect.ping: " + response.readEntity(String.class) + "\n");
    }

    public void pingQuery() {
        WebTarget path = query.path("/ping");
        Response response = path.request().get();
        System.out.println("query.ping: " + response.readEntity(String.class));
    }

       
    public void populate() {
        WebTarget path = collect.path("/weather/BOS/wind");
        DataPoint dp = new DataPoint();
        dp.setFirst(0);
        dp.setLast(10);
        dp.setMean(4);
        dp.setMedian(4);
        dp.setCount(10);
        Response post = path.request().post(Entity.entity(dp, "application/json"));
    }

    public void query() {
        WebTarget path = query.path("/weather/BOS/0");
        Response response = path.request().get();
        System.out.println("query.get:" + response.readEntity(String.class));
    }

    //CR: All test methods should be gotten rid of
    //CR: prior deployment
    
    public static void main(String[] args) {
        WeatherClient wc = new WeatherClient();
        wc.pingCollect();
        wc.populate();
        wc.query();
        wc.pingQuery();
        System.out.print("complete");
        System.exit(0);
    }
}
