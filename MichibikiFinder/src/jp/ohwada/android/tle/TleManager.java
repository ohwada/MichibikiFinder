package jp.ohwada.android.tle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

/**
 * TleManager
 */
public class TleManager 
	implements LoaderCallbacks<String> {

	// debug
	private static final boolean D = TleConstant.DEBUG;
	private static final String TAG = TleConstant.TAG;
	private static final String TAG_SUB = "TleManager";

	private final static int LOADER_ID = 1;
	private final static Bundle LOADER_BUNDLE = null;

    private OnReadListener mListener;  
 
	private LoaderManager mLoaderManager;
	private Context mContext;
	private TleFile mTleFile;

    /**
     * The callback interface 
     */
    public interface OnReadListener {
        public void onRead( List<String> list );
    }

	/**
	 * OnReadListener
	 * @param OnReadListener listener
	 */
    public void setOnReadListener( OnReadListener listener ) {
        mListener = listener;
    }

	/**
	 * TleManager
	 */
	public TleManager( Activity activity ) {
		mLoaderManager = activity.getLoaderManager();
		mContext = activity;
		mTleFile = new TleFile();
		mTleFile.init();
	}

	/**
	 * initLoader
	 */
	private void initLoader() {
		mLoaderManager.initLoader( LOADER_ID, LOADER_BUNDLE, this );
	}

	/**
	 * destroyLoader
	 */
	public void destroyLoader() {
		mLoaderManager.destroyLoader( LOADER_ID );
	}

	/**
	 * read
	 */
	public void requestTle() {
		File file = mTleFile.getFile(TleConstant.FILE_SBAS );
		if ( mTleFile.isExpired( file, TleConstant.TIME_EXPIRE ) ) {
			initLoader();
			return;
		}
		notifyRead( readTle( file ));
	}
	
	/**
	 * read
	 */
	private List<String> readTle( File file ) {
		List<String> list1 = mTleFile.read( file );
		List<String> list2 = new ArrayList<String>();
        int size = list1.size() / 3;
		for ( int i = 0; i < size; ++i ) {
			int ii = 3 * i;
			String s0 = list1.get( ii );
			if ( s0.startsWith( TleConstant.NAME_QZS1 )) {
				list2.add( s0 );
				list2.add( list1.get( ii + 1 ) );
				list2.add( list1.get( ii + 2 ) );
				break;
			}
		}
		return list2;
	}

	/*
	 * === onCreateLoader ===
	 */
    @Override
	public Loader<String> onCreateLoader( int id, Bundle bundle ) {
        TleLoader loader = new TleLoader( mContext );
        loader.forceLoad();
        return loader;
    }

	/*
	 * === onLoadFinished ===
	 */
    @Override
    public void onLoadFinished( Loader<String> loader, String data ) {
    	if ( data == null ) {
    		log_d( "result is null" );
    		return;
    	}
    	File file = mTleFile.getFile( TleConstant.FILE_SBAS );
		mTleFile.write( file, data );
		notifyRead( readTle( file ));
    }

	/*
	 * === onLoaderReset ===
	 */	
	@Override
	public void onLoaderReset( Loader<String> loader ) {
		// dummy		
	}

	/**
	 * notifyRead
	 */ 
    private void notifyRead( List<String> list ) {
        if ( mListener != null ) {
            mListener.onRead( list );
        }
    }

	/**
	 * log_d
	 */
	private void log_d( String msg ) {
	    if (D) Log.d( TAG, TAG_SUB + " " + msg );	
	}
}
