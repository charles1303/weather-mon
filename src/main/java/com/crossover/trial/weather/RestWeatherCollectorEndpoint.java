package com.crossover.trial.weather;

import com.google.gson.Gson;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static com.crossover.trial.weather.RestWeatherQueryEndpoint.*;

/**
 * A REST implementation of the WeatherCollector API. Accessible only to airport weather collection
 * sites via secure VPN.
 *
 * @author code test administrator
 */

@Path("/collect")
public class RestWeatherCollectorEndpoint implements WeatherCollector {
	
	public final static Logger logger = Logger.getLogger(RestWeatherCollectorEndpoint.class.getName());

    /** shared gson json to object factory */
    public final static Gson gson = new Gson();

    static {
        init();
    }

    @GET
    @Path("/ping")
    @Override
    public Response ping() {
        return Response.status(Response.Status.OK).entity("ready").build();
    }

    @POST
    @Path("/weather/{iata}/{pointType}")
    @Override
    
    public Response updateWeather(@PathParam("iata") String iataCode,
                                  @PathParam("pointType") String pointType,
                                  String datapointJson) {
        try {
            addDataPoint(iataCode, pointType, gson.fromJson(datapointJson, DataPoint.class));
        } catch (WeatherException e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/airports")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response getAirports() {
        Set<String> retval = new HashSet<>();
        for (AirportData ad : airportData) {
            retval.add(ad.getIata());
        }
        return Response.status(Response.Status.OK).entity(retval).build();
    }

    @GET
    @Path("/airport/{iata}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response getAirport(@PathParam("iata") String iata) {
        AirportData ad = findAirportData(iata);
        return Response.status(Response.Status.OK).entity(ad).build();
    }

    @POST
    @Path("/airport23/{iata}/{lat}/{long}")
    @Override
    public Response addAirport(@PathParam("iata") String iata,
                               @PathParam("lat") String latString,
                               @PathParam("long") String longString) {
        addAirport(iata, Double.valueOf(latString), Double.valueOf(longString));
        return Response.status(Response.Status.OK).build();
    }
    
    /*
     * (non-Javadoc)
     * @see com.crossover.trial.weather.WeatherCollector#createAirport(com.crossover.trial.weather.Airport)
     * Sample Json payload to send to endpoint that creates new airport
     * {
			"city":"Ajah",
			"altitude":"20",
			"icao_code":"XD",
			"country":"Nigeria",
			"timezone":"5.5",
			"longitude":"7.8",
			"daylight_saving_time":"E"
		}
     */
    @POST
    @Path("/airport/post")
    @Consumes("application/json")
    @Override
    public Response createAirport(Airport airport) {
    	Airport newAirport =new Airport();
    	try {
			newAirport = doCreateAirport(airport);
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
        return Response.ok(newAirport).build();
    }

    @DELETE
    @Path("/airport23/{iata}")
    @Override
    public Response deleteAirport(@PathParam("iata") String iata) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }
    
    @DELETE
    @Path("/airport/{id}")
    @Override
    public Response removeAirport(@PathParam("id") String id) {
    	try {
			doRemoveAirport(id);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    //
    // Internal support methods
    //

    /**
     * Update the airports weather data with the collected data.
     *
     * @param iataCode the 3 letter IATA code
     * @param pointType the point type {@link DataPointType}
     * @param dp a datapoint object holding pointType data
     *
     * @throws WeatherException if the update can not be completed
     */
    public void addDataPoint(String iataCode, String pointType, DataPoint dp) throws WeatherException {
        int airportDataIdx = getAirportDataIdx(iataCode);
        AtmosphericInformation ai = atmosphericInformation.get(airportDataIdx);
        updateAtmosphericInformation(ai, pointType, dp);
    }

    /**
     * update atmospheric information with the given data point for the given point type
     *
     * @param ai the atmospheric information object to update
     * @param pointType the data point type as a string
     * @param dp the actual data point
     */
    public void updateAtmosphericInformation(AtmosphericInformation ai, String pointType, DataPoint dp) throws WeatherException {
       
        if (pointType.equalsIgnoreCase(DataPointType.WIND.name())) {
            if (dp.getMean() >= 0) {
                ai.setWind(dp);
                ai.setLastUpdateTime(System.currentTimeMillis());
                return;
            }
        }

        if (pointType.equalsIgnoreCase(DataPointType.TEMPERATURE.name())) {
            if (dp.getMean() >= -50 && dp.getMean() < 100) {
                ai.setTemperature(dp);
                ai.setLastUpdateTime(System.currentTimeMillis());
                return;
            }
        }

        if (pointType.equalsIgnoreCase(DataPointType.HUMIDTY.name())) {
            if (dp.getMean() >= 0 && dp.getMean() < 100) {
                ai.setHumidity(dp);
                ai.setLastUpdateTime(System.currentTimeMillis());
                return;
            }
        }

        if (pointType.equalsIgnoreCase(DataPointType.PRESSURE.name())) {
            if (dp.getMean() >= 650 && dp.getMean() < 800) {
                ai.setPressure(dp);
                ai.setLastUpdateTime(System.currentTimeMillis());
                return;
            }
        }

        if (pointType.equalsIgnoreCase(DataPointType.CLOUDCOVER.name())) {
            if (dp.getMean() >= 0 && dp.getMean() < 100) {
                ai.setCloudCover(dp);
                ai.setLastUpdateTime(System.currentTimeMillis());
                return;
            }
        }

        if (pointType.equalsIgnoreCase(DataPointType.PRECIPITATION.name())) {
            if (dp.getMean() >=0 && dp.getMean() < 100) {
                ai.setPrecipitation(dp);
                ai.setLastUpdateTime(System.currentTimeMillis());
                return;
            }
        }

        throw new IllegalStateException("couldn't update atmospheric data");
    }

    /**
     * Add a new known airport to our list.
     *
     * @param iataCode 3 letter code
     * @param latitude in degrees
     * @param longitude in degrees
     *
     * @return the added airport
     */
    public static AirportData addAirport(String iataCode, double latitude, double longitude) {
        AirportData ad = new AirportData();
        airportData.add(ad);

        AtmosphericInformation ai = new AtmosphericInformation();
        atmosphericInformation.add(ai);
        ad.setIata(iataCode);
        ad.setLatitude(latitude);
        ad.setLatitude(longitude);
        return ad;
    }
    
    //Write to file but this can be replaced by a more
    //robust data store system
    public Airport doCreateAirport(Airport airport) throws IOException, URISyntaxException {
           	try {
    		
    		File aFile = new File("airports.txt");
    		int cnt = countLines(aFile.getCanonicalPath());
        	FileWriter fileWritter = new FileWriter(aFile.getName(),true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            StringBuffer strBuffer = new StringBuffer();
            strBuffer.append(cnt + 1);
            strBuffer.append(",");
            strBuffer.append(airport.getCity());
            strBuffer.append(",");
            strBuffer.append(airport.getCountry());
            strBuffer.append(",");
            strBuffer.append(airport.getIcao_code());
            strBuffer.append(",");
            strBuffer.append(airport.getLongitude());
            strBuffer.append(",");
            strBuffer.append(airport.getAltitude());
            strBuffer.append(",");
            strBuffer.append(airport.getTimezone());
            strBuffer.append(",");
            strBuffer.append(airport.getDaylight_saving_time().name());
            bufferWritter.write(strBuffer.toString());
            bufferWritter.write("\n");
            bufferWritter.close();
            airport.setId(Long.valueOf(String.valueOf(cnt+1)));
		} catch (IOException ioe) {
			throw ioe;
		}
        return airport;
    }
    
    private boolean doRemoveAirport(String id) throws URISyntaxException{
    	File aFile = new File("airports.txt");
    	return removeLineFromFile(aFile, id);
    }

    /**
     * A dummy init method that loads hard coded data
     */
   
    protected static void init() {
        airportData.clear(); atmosphericInformation.clear(); requestFrequency.clear();
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("airports.dat");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String l = null;

        try {
            while ( (l = br.readLine()) != null) {
                String[] split = l.split(",");
                addAirport(split[0],
                        Double.valueOf(split[1]),
                        Double.valueOf(split[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }
    
    
    private boolean removeLineFromFile(File inFile, String lineIdToRemove) {
    		boolean delete = false;
        try {
          if (!inFile.isFile()) {
        	  logger.info("Parameter is not an existing file");
            return delete;
          }
          File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
          BufferedReader br = new BufferedReader(new FileReader(inFile.getAbsolutePath()));
          PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

          String line = null;
          while ((line = br.readLine()) != null) {
        	  if(!line.startsWith(lineIdToRemove)){
              pw.println(line);
              pw.flush();
            }
          }
          pw.close();
          br.close();
          if (!inFile.delete()) {
        	  logger.info("Could not delete file");
            return delete;
          }
          if (!tempFile.renameTo(inFile)){
        	  logger.info("Could not rename file");
              return delete;
          }
            
          delete = true;

        }
        catch (FileNotFoundException ex) {
          ex.printStackTrace();
        }
        catch (IOException ex) {
          ex.printStackTrace();
        }
        return delete;
      }
    
    
    public static void main(String[] args) throws IOException, URISyntaxException{
    	RestWeatherCollectorEndpoint r = new RestWeatherCollectorEndpoint();
    	/*Airport airport = new Airport();
    	airport.setCity("Ajah");
    	airport.setAltitude(20);
    	airport.setIcao_code("XD");
    	
    	airport.setCountry("Nigeria");
    	airport.setTimezone(5.5);
    	airport.setLongitude(7.8);
    	airport.setDaylight_saving_time(Airport.DST.valueOf("E"));
    	r.createAirport(airport);*/
    	r.doRemoveAirport("11");
    }

}
