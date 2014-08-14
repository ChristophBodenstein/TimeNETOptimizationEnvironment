/*
 * Web based simulator of SCPNs
 * download SCPNs as xml files, start local simulation, upload the results


* To be modified by: Group studies 2014


 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import timenetexperimentgenerator.support;

/**
 *
 * @author sse
 */
public class SimulatorWebSlave implements Runnable{
private boolean shouldEnd=false;

    public void run() {
        while(true){
        //Request the server Api to get the Status Code and response body.
       // Getting the status code.
       // While starting the tool the function should not call due to which exception is created.
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://localhost:8080/timenetws-server/rest/api/downloads/ND");     
        HttpResponse response = null;
        String responseString = null;
            try {
                response = client.execute(httpGet);
                responseString = new BasicResponseHandler().handleResponse(response);
            } catch (IOException ex) {
                Logger.getLogger(SimulatorWebSlave.class.getName()).log(Level.SEVERE, null, ex);
            }
                        
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("Response String ==========="+responseString);     
        System.out.println("statusCode ==========="+statusCode);  
        
        List<String> lines = null;
         
     FileWriter fileWriter = null;
        try {
           
            File newTextFile = new File("C:/Downloads/veer.txt");
            fileWriter = new FileWriter(newTextFile);
            fileWriter.write(responseString);
            fileWriter.close();
        } catch (IOException ex) {
               } finally {
            try {
                fileWriter.close();
            } catch (IOException ex) {
               
            }
        }
     /*       String content = "Hello File!";
            String path = "file:///C://Downloads/veer.txt";
            try {
                Files.write( Paths.get(path), responseString.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException ex) {
                Logger.getLogger(SimulatorWebSlave.class.getName()).log(Level.SEVERE, null, ex);
            }   */
            try {
                Thread.sleep(2000);
                
                //get URL!
                //support.getReMoteAddress();
                
            } catch (InterruptedException ex) {
                support.log("Error while sleeping Thread of Slave Web simulator.");
            }
        support.log("Dummy Thread started to download and simulate SCPNs.");
        
            if(this.shouldEnd){
            support.log("Slave Thread will end now.");
            this.shouldEnd=false;
            return;
            }
        
        }
    }

    /**
     * @return the shouldEnd
     */
    public boolean isShouldEnd() {
        return shouldEnd;
    }

    /**
     * @param shouldEnd the shouldEnd to set
     */
    public void setShouldEnd(boolean shouldEnd) {
        this.shouldEnd = shouldEnd;
    }
    
}
