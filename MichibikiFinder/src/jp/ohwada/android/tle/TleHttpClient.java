package jp.ohwada.android.tle;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/**
 * TLE Http Client
 */
public class TleHttpClient {

	// debug
	private static final boolean D = TleConstant.DEBUG;
	private static final String TAG = TleConstant.TAG;
	private static final String TAG_SUB = "TleHttpCilent";

	// constant
	private final static String ENCODING = "UTF-8";	

    // constructor   	
	private DefaultHttpClient mClient = null;
	
	/**
	 * === constructor ===
	 */			 
    public TleHttpClient() {
        mClient = new DefaultHttpClient();
    }
		
	/**
	 * execute get metod
	 * @param String url
	 * @return String : result
	 */  		
    public String get( String url ) {
		// get http response
		HttpGet request = new HttpGet( url );
		String result = null;
		try {
    		result = mClient.execute( request, new ResponseHandler<String>() {
        		public String handleResponse( HttpResponse response ) {
                	return parseResponse( response );
            	}
    		});    		
		} catch ( ClientProtocolException e ) {
		    if (D) e.printStackTrace();
		} catch ( IOException e ) {
		    if (D) e.printStackTrace();
		}
    	return result;	
	}

	/**
	 * parse Response
	 */ 
    private String parseResponse( HttpResponse response ) {
		String result = null;
		int code = response.getStatusLine().getStatusCode();
		switch ( code ) {
       		case HttpStatus.SC_OK:
       			try {
       				result = EntityUtils.toString( response.getEntity(), ENCODING );
       			} catch (ParseException e) {
       				if (D) e.printStackTrace();
       			} catch (IOException e) {
       				if (D) e.printStackTrace();
       			}
       			break;      
            default:
                log_d( "error code: " + code );
				break;
		}		
        return result;    
	} 

	/**
	 * shutdown 
	 */ 
    public void shutdown() {
    	if ( mClient != null ) {
    		mClient.getConnectionManager().shutdown();
    	}
    	mClient = null;
    }
		   		
	/**
	 * write log
	 * @param String msg
	 */ 
	private void log_d( String msg ) {
	    if (D) Log.d( TAG, TAG_SUB + " " + msg );
	}			    		   
}