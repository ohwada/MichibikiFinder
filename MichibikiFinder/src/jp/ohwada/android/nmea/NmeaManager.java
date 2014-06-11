package jp.ohwada.android.nmea;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Nmea Manager
 */ 
public class NmeaManager {

	private static final long MIN_TIME = 1000;	// 1 sec
	private static final float MIN_DISTANCE = 0;

 	private Context mContext;  			
	private LocationManager mLocationManager;
	private LocationListener mLocationListener = null;
	private GpsStatus.NmeaListener mNmeaListener = null;

    /** 
	 * === constructor ===
	 */	     
 	public NmeaManager( Context context ) {
 		mContext = context;                          
		mLocationManager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
	}

    /** 
	 * checkGpsEnable
	 */	
	public boolean checkGpsEnable() {
		return mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
    }
           	
    /** 
	 * addNmeaListener
	 */	
 	public void addNmeaListener( GpsStatus.NmeaListener listener ) {	
 		mNmeaListener = listener;
		mLocationManager.addNmeaListener( mNmeaListener );
	}

    /** 
	 * removeNmeaListener
	 */	
 	public void removeNmeaListener() {	
 		if ( mNmeaListener != null ) {
			mLocationManager.removeNmeaListener( mNmeaListener );
		}
	}

    /** 
	 * requestLocationUpdates
	 */		
 	public void requestLocationUpdates() {	
 		requestLocationUpdates( new DummyLocationListener() );
	}

    /** 
	 * requestLocationUpdates
	 */		
 	public void requestLocationUpdates( LocationListener listener ) {	
 		mLocationListener = listener;
		mLocationManager.requestLocationUpdates( 
			LocationManager.GPS_PROVIDER, 
			MIN_TIME, 
			MIN_DISTANCE, 
			listener );
	}

    /** 
	 * removeUpdates
	 */
 	public void removeUpdates() {		
 		if ( mLocationListener != null ) {
   			mLocationManager.removeUpdates( mLocationListener );
		}
	}	

    /** 
	 * checkGpsEnable
	 */	
	public void showGpsEnableDialog( int id_message, int id_enable, int id_cancel ) {
  		AlertDialog.Builder builder = new AlertDialog.Builder( mContext );
		builder.setMessage( id_message )
			.setCancelable( false );
		builder.setPositiveButton( id_enable,
			new DialogInterface.OnClickListener(){
				public void onClick( DialogInterface dialog, int id ){
					Intent intent = new Intent( android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS );
					mContext.startActivity( intent );
				}
			});
		builder.setNegativeButton( id_cancel,
			new DialogInterface.OnClickListener(){
				public void onClick( DialogInterface dialog, int id ){
					dialog.cancel();
				}
		});
        AlertDialog alert = builder.create();
        alert.show();
    }
   	
	/**
	 * class DummyLocationListener
	 */    
	private class DummyLocationListener implements LocationListener {
		@Override
		public void onLocationChanged( Location location ) {
			// dummy
		}
		@Override
		public void onProviderDisabled( String provider ) {
			// dummy
		}
		@Override
		public void onProviderEnabled( String provider ) { 
			// dummy
		}
		@Override
		public void onStatusChanged( String provider, int status, Bundle extras ) { 
			// dummy
		}
	};
    	        
}
