/**
 * Factory to return Singleton objects for network connections
 *
 * Christoph Bodenstein TU-Ilmenau, FG SSE
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

    /**
     * Returns the singleton DefaultHttpClient object for reuse with every
     * connection
     *
     * @return DefaultHttpClient to be reused with every connection
     */
    public static DefaultHttpClient getHttpClient() {
        if (client == null) {
            client = new DefaultHttpClient();
        }
        return client;
    }

    /**
     * Returns the singleton HttpPost object to be reused with every
     * Post-Request If given URI-String is wrong, returns a null
     *
     * @param s URI-String to build the HttpPost-request
     * @return singleton HttpPost object to be reused with every Post-Request
     */
    public static HttpPost getPostRequest(String s) {
        if (postRequest == null) {
            postRequest = new HttpPost();
        }
        try {
            postRequest.setURI(new URI(s));
        } catch (URISyntaxException ex) {
            support.log("Error creating new PostRequest.");
            postRequest = null;
        }
        return postRequest;
    }

    /**
     * Returns the singleton HttpGet object to be reused with every Http get
     * request If URI-String is wrong, returns a null
     *
     * @param s URI-String to build the Get request
     * @return singleton HttpGet object to be reused with every Http get request
     */
    public static HttpGet getGetRequest(String s) {
        if (httpGet == null) {
            httpGet = new HttpGet();
        }
        try {
            httpGet.setURI(new URI(s));
        } catch (URISyntaxException ex) {
            support.log("Error creating new PostRequest.");
            httpGet = null;
        }
        return httpGet;
    }

}