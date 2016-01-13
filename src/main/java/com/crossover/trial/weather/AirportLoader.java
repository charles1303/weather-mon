package com.crossover.trial.weather;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import java.io.*;

/**
 * A simple airport loader which reads a file from disk and sends entries to the webservice
 *
 * TODO: Implement the Airport Loader
 * 
 * @author code test administrator
 */
public class AirportLoader {

    /** end point for read queries */
    private WebTarget query;

    /** end point to supply updates */
    private WebTarget collect;

    public AirportLoader() {
        Client client = ClientBuilder.newClient();
        //CR: Externalize the hard-coded end point values below
        //CR: and reference them via variables as this will
        //CR: surely change when moved to another environment
        //CR: say production
        query = client.target(WeatherServer.BASE_URL+"query");
        collect = client.target(WeatherServer.BASE_URL+"collect");
    }

    public void upload(InputStream airportDataStream) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(airportDataStream));
        String l = null;
        while ((l = reader.readLine()) != null) {
            break;
        }
    }
    //save a new Airport
    private void saveAirport(){
    	Airport airport = new Airport();
    	airport.setCity("Ajah");
    	airport.setAltitude(20);
    	airport.setIcao_code("XD");
    	
    	airport.setCountry("Nigeria");
    	airport.setTimezone(5.5);
    	airport.setLongitude(7.8);
    	airport.setDaylight_saving_time(Airport.DST.valueOf("E"));
    	Airport newAirport = collect.path("/airport/post").request().post(Entity.entity(airport,MediaType.APPLICATION_JSON),Airport.class);
    	
    }
    
    //delete an existing airport
    private void removeAirport(){
    	String airportId = "10";
    	collect.path("/airport/{airportId}").resolveTemplate("airportId",airportId).request().delete();
    }

    public static void main(String args[]) throws IOException{
    	AirportLoader al = new AirportLoader();
    	al.saveAirport();
    }

	private static void initTest() throws IOException, FileNotFoundException {
		//File airportDataFile = new File(args[0]);
    	File airportDataFile = new File("airports.txt");
        if (!airportDataFile.exists() || airportDataFile.length() == 0) {
            System.err.println(airportDataFile + " is not a valid input");
            System.exit(1);
        }

        AirportLoader al = new AirportLoader();
        al.upload(new FileInputStream(airportDataFile));
        System.exit(0);
	}
}
