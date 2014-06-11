package jp.ohwada.android.tle;

import android.content.AsyncTaskLoader;
import android.content.Context;

/*
 * TleLoader
 */ 
public class TleLoader extends AsyncTaskLoader<String> {

	private TleHttpClient mClient;
        
	/*
	 * Constractor
	 * @param Context context
	 */ 
    public TleLoader( Context context ) {
        super( context );
        mClient = new TleHttpClient();	
	}

	/*
	 * === loadInBackground ===
	 */ 	 
    @Override
    public String loadInBackground() {
		return mClient.get( TleConstant.URL_SBAS );
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override 
    protected void onStopLoading() {
        mClient.shutdown();
        cancelLoad();
    }
     	
}