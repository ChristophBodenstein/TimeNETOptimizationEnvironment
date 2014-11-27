/**
 * Factory to return Singleton objects for network connections
 * 
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package timenetexperimentgenerator;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author Christoph Bodenstein
 */
public class HttpFactory {
private static DefaultHttpClient client = null;
private static HttpPost postRequest = null;
private static HttpGet httpGet = null;

    public static DefaultHttpClient getHttpClient(){
        if(client==null){
        client = new DefaultHttpClient();
        }
        return client;
    }
    
    public static HttpPost getPostRequest(String s){
        if(postRequest==null){
            postRequest=new HttpPost();
        }
    try {
        postRequest.setURI(new URI(s));
    } catch (URISyntaxException ex) {
        support.log("Error creating new PostRequest.");
        postRequest=null;
    }
        return postRequest;
    }
    
    public static HttpGet getGetRequest(String s){
        if(httpGet==null){
            httpGet=new HttpGet();
        }
    try {
        httpGet.setURI(new URI(s));
    } catch (URISyntaxException ex) {
        support.log("Error creating new PostRequest.");
        httpGet=null;
    }
        return httpGet;
    }

}
