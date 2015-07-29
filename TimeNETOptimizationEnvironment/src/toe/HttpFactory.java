/**
 * Factory to return Singleton objects for network connections
 *
 * Christoph Bodenstein TU-Ilmenau, FG SSE
 */
package toe;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import toe.typedef.*;

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
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 2000);
            client = new DefaultHttpClient(httpParams);
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
            support.log("Error creating new PostRequest.", typeOfLogLevel.ERROR);
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
            support.log("Error creating new PostRequest.", typeOfLogLevel.ERROR);
            httpGet = null;
        }
        return httpGet;
    }

    /**
     * Reset all connection objects to be created newly
     */
    public static void resetConnections() {
        httpGet = null;
        postRequest = null;
        client = null;
    }

}
