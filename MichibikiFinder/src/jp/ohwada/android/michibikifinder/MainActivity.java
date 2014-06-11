package jp.ohwada.android.michibikifinder;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import jp.ohwada.android.nmea.NmeaFactory;
import jp.ohwada.android.nmea.NmeaManager;
import jp.ohwada.android.satellite.AziEle;
import jp.ohwada.android.satellite.AziEleController;
import jp.ohwada.android.satellite.LatLng;
import jp.ohwada.android.satellite.LatLngController;
import jp.ohwada.android.satellite.Satellite;
import jp.ohwada.android.satellite.SatelliteController;
import jp.ohwada.android.satellite.SiderealTime;
import jp.ohwada.android.tle.TleManager;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

/**
 * MainActivity
 */
public class MainActivity extends MapActivity {

	private final static boolean D = true;
	private String TAG = "SatelliteTracking";

    private final static String CHAR_SPACE = " "; 

    private final static int MINUTE_DELTA = 10;    
    private final static int POINT_NUM = 24 * 60 / MINUTE_DELTA ;
	private final static double RAD_DIV = 2 * Math.PI / POINT_NUM;
	private final static long TIME_DIV = 24*60*60*1000 /  POINT_NUM;

    private final static String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss z";
    private final static String TIME_FORMAT = "kk:mm:ss";

	private final static double GEO_LAT = 35.695;
	private final static double GEO_LNG = 139.740;
	
    private final static int WHAT_TIMER = 1;
    private final static int TIMER_INTERVAL = 100;

	private DecimalFormat mLocFormat = new DecimalFormat("##0.000000");
	private DecimalFormat mInfoFormat = new DecimalFormat("##0.0");

	private String strDate; 
	private String strSatLocation;
	private String strMyLocation;
	private String strAzimuth;
	private String strElevation;
	private String strDefault;
	private String strPreNorth;
	private String strPreSouth;
	private String strPreWest;
	private String strPreEast;
	private String strPostNorth;
	private String strPostSouth;
	private String strPostWest;
	private String strPostEast;
				
	private AziEleView mAziEleView;
	private TrackingMap mTrackingMap;
	private SeekBar mSeekBarTrace;
	private TextView mTextViewDate;
	private TextView mTextViewSat;
	private TextView 	mTextViewLocation;
	private TextView mTextViewInfo;
			
	private NmeaManager mNmeaManager;
	private NmeaFactory mNmeaFactory;	
	private TleManager mTleManager;
	private SatelliteData mSatelliteData;
	private SatelliteController mSatelliteController;
	private LatLngController mLatLngController;
	private AziEleController mAziEleController;

	private List<Satellite> mSatelliteLlist = null;	
	private List<LatLng> mLatLngList = null;
	private List<AziEle> mAziEleList = null;

	private boolean isLocation = false;
	private double	mLatitude = GEO_LAT;
	private double	mLongitude = GEO_LNG;	
				
	private int mPointer = 0;
	private int mCurrentNum = 0;
	private long mStartTime = 0;
	private boolean isAuto = true;
	   				
	private boolean isTimerStarted = false;
	private boolean isTimerRunning = false;

    /** 
	 * === constructor ===
	 */	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		mTextViewDate = (TextView) findViewById( R.id.TextView_date );
		mTextViewSat = (TextView) findViewById( R.id.TextView_sat );
		mTextViewLocation = (TextView) findViewById( R.id.TextView_location );	
		mTextViewInfo = (TextView) findViewById( R.id.TextView_info );	
		
		mAziEleView = (AziEleView) findViewById( R.id.AziEleView );
		mAziEleView.setTrace( true );
				
        // map
        MapView mapView = (MapView) findViewById( R.id.MapView );	
		mTrackingMap = new TrackingMap( mapView);
		mTrackingMap.init();
		mTrackingMap.setTrace( true );
		
        CheckBox cbTrace = (CheckBox) findViewById( R.id.CheckBox_trace );
        cbTrace.setChecked( true );
        cbTrace.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
            	execCheckboxTrace( view );
            }
        });

		mSeekBarTrace = (SeekBar) findViewById( R.id.SeekBar_trace );
        mSeekBarTrace.setMax( POINT_NUM - 1 );
        mSeekBarTrace.setProgress( 0 );
        mSeekBarTrace.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch( SeekBar seekBar ) {
				// dummy
            }
            @Override
            public void onProgressChanged( SeekBar seekBar, int progress, boolean fromTouch ) {
				execSeekbarTrace( progress );
            }
            @Override
            public void onStopTrackingTouch( SeekBar seekBar ) {
				// dummy
            }
        });
		
		mTleManager = new TleManager( this );
		mTleManager.setOnReadListener( new TleManager.OnReadListener() {
			@Override
			public void onRead( List<String> list ) {
				showSatellite( list );
			}
		});

		strDate = getString( R.string.date ) + " : ";
		strSatLocation = getString( R.string.sat_location ) + " : ";
		strMyLocation = getString( R.string.my_location ) + " : ";
		strAzimuth = getString( R.string.azimuth ) + CHAR_SPACE;
		strElevation = getString( R.string.elevation ) + CHAR_SPACE;
		strDefault = "(" + getString( R.string.loc_default ) + ")";
		strPreNorth = getString( R.string.pre_north ) + CHAR_SPACE;
		strPreSouth = getString( R.string.pre_south ) + CHAR_SPACE;
		strPreWest = getString( R.string.pre_west ) + CHAR_SPACE;
		strPreEast = getString( R.string.pre_east ) + CHAR_SPACE;
		strPostNorth = getString( R.string.post_north ) + CHAR_SPACE;
		strPostSouth = getString( R.string.post_south ) + CHAR_SPACE;
		strPostWest = getString( R.string.post_west ) + CHAR_SPACE;
		strPostEast = getString( R.string.post_east ) + CHAR_SPACE;
												
		mSatelliteController = new SatelliteController();
		mLatLngController = new LatLngController();
		mAziEleController = new AziEleController();				
		mSatelliteData = new SatelliteData(); 

		mNmeaManager = new NmeaManager( this );
		mNmeaFactory = new NmeaFactory();

	}
	                        
	/**
	 * === onResume ===
	 */
	@Override
	protected void onResume() {
		super.onResume();

		LocationListener loc_listener = new LocationListener() {
			@Override
			public void onLocationChanged( Location location ) {
				execLocation( location );
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

		GpsStatus.NmeaListener nmea_listener = new GpsStatus.NmeaListener() {
			@Override
			public void onNmeaReceived( long timestamp, String nmea ) {
				execNmea( timestamp, nmea );
			}
    	};
  
    	mNmeaManager.addNmeaListener( nmea_listener );
    	mNmeaManager.requestLocationUpdates( loc_listener );

		// strat new thead
		new Handler().postDelayed( mRunnable, 100 ); 
	}

	/**
	 * === onPause ===
	 */
	@Override
	protected void onPause() {
		super.onPause();
    	mNmeaManager.removeNmeaListener();
    	mNmeaManager.removeUpdates();
		stopTimer();
	}

	/*
	 * === onDestroy ===
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTleManager.destroyLoader();
	}

	/*
	 * === isRouteDisplayed ===
	 */
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * Runnable
	 */
	private final Runnable mRunnable = new Runnable() {
    	@Override
    	public void run() {
        	execDelay();
    	}
	};

	/**
	 * execDelay
	 */
	private void execDelay() {
     	mTleManager.requestTle();
    	if ( !mNmeaManager.checkGpsEnable() ) {
    		mNmeaManager.showGpsEnableDialog( 
    			R.string.dialog_message, 
    			R.string.btn_enable, 
    			R.string.btn_cancel ); 
    	}
    }
    	
	/**
	 * showSatellite
	 */
	private void showSatellite( List<String> tle ) {
		Calendar cal = mSatelliteData.getCalendarNow( MINUTE_DELTA );
		long now = cal.getTimeInMillis();  
		cal.set( Calendar.HOUR_OF_DAY, 0 );
		cal.set( Calendar.MINUTE, 0 );
		mStartTime = cal.getTimeInMillis();  
		mCurrentNum = (int)(( now - mStartTime ) / TIME_DIV );	
		double offset = 2.0 * Math.PI * SiderealTime.getSiderealTime( cal );
		// SatelliteData
		mSatelliteData.setTle( tle );
		List<Satellite> list_data = mSatelliteData.getSatelliteList( POINT_NUM, cal );
		mSatelliteLlist = mSatelliteController.rotationList( list_data, RAD_DIV, offset );
		// TrrackingMap
		 mLatLngList = mLatLngController.convList( mSatelliteLlist );
		mTrackingMap.setList( mLatLngList );
		mTrackingMap.setCurrentNum( mCurrentNum );
		// AziEleView
		showLocationAndTrace();
		// text
		String msg1 = strDate + getFormatedDate( DATE_FORMAT, mCurrentNum );
		mTextViewDate.setText( msg1 );
		String msg2 = strSatLocation + getInfo( mCurrentNum );
		mTextViewSat.setText( msg2 );
		// timer
		procTrace();
	}

	/**
	 * showLocationAndTrace
	 */
	private void showLocationAndTrace() {
		mTrackingMap.setLocation( mLatitude, mLongitude );

		// AziEle Trace
		if ( mSatelliteLlist != null ) {
			mAziEleController.setOrigin( mLatitude, mLongitude );
			mAziEleList = mAziEleController.convList( mSatelliteLlist );
			mAziEleView.setTraceList( mAziEleList );
			mAziEleView.setCurrentNum( mCurrentNum );
		}
		
		// location
		String msg = strMyLocation ;
		msg += getLocLat( mLatitude ) + CHAR_SPACE;
		msg += getLocLng( mLongitude ) + CHAR_SPACE;
		msg += isLocation ? "" : strDefault;
		mTextViewLocation.setText( msg );	
	}
				
	/**
	 * execLocation
	 */		
	private void execLocation( Location location ) {
		isLocation = true;
		mLatitude = location.getLatitude();
		mLongitude = location.getLongitude();	
		showLocationAndTrace();
	}
	
	/**
	 * execNmea
	 */     
	private void execNmea( long timestamp, String nmea ) {
		mNmeaFactory.setTimestamp( timestamp );
		mNmeaFactory.parse( nmea );	
		mAziEleView.setSatelliteList( mNmeaFactory.getAllSatellites() );
	}

	/**
	 * execSeekbarTrace
	 */ 
	private void execSeekbarTrace( int progress ) {
		if ( isAuto ) return;
		updatePointer( progress );
	}

	/**
	 * execCheckboxTrace
	 */  
     private void execCheckboxTrace( View view ) {
     	CheckBox cb = (CheckBox) view;
		isAuto = cb.isChecked();
		procTrace();
	}

	/**
	 * procTrace
	 */  
     private void procTrace() {
		if ( isAuto ) {
			mPointer = mSeekBarTrace.getProgress();
			startTimer();
		} else {
			mSeekBarTrace.setProgress( mPointer );
			stopTimer();
		}
	}

	/**
	 * startTimer
	 */
	protected void startTimer() {
		isTimerStarted = true;
		updateRunning();
	}

	/**
	 * stopTimer
	 */
	public void stopTimer() {
		isTimerStarted = false;
		updateRunning();
	}

	/**
	 * updateRunning
	 */
    private void updateRunning() {
        boolean running = isTimerStarted;
        if ( running != isTimerRunning ) {
            if ( running ) {
            	updateTimer();
                mTimerHandler.sendMessageDelayed(
                	Message.obtain( mTimerHandler, WHAT_TIMER ), TIMER_INTERVAL );                
             } else {
                mTimerHandler.removeMessages( WHAT_TIMER );
            }
            isTimerRunning = running;
        }
    }

	/**
	 * TimerHandler
	 */
    private Handler mTimerHandler = new Handler() {
        public void handleMessage( Message m ) {
            if ( isTimerRunning ) {
                updateTimer();
                sendMessageDelayed(
                	Message.obtain( this, WHAT_TIMER ), TIMER_INTERVAL );
            }
        }
    };

	/**
	 * updateTimer
	 */  
	private synchronized void updateTimer() {
		if ( !isTimerStarted ) return;
		mPointer ++;
		if ( mPointer >= POINT_NUM ) {
			mPointer = 0;
		}
		updatePointer( mPointer );
	}

	/**
	 * updatePointer
	 */  	
	private void updatePointer( int pointer ) {
		mTrackingMap.updatePointer( pointer );
		mAziEleView.updatePointer( pointer );
		String msg = getFormatedDate( TIME_FORMAT, pointer ) + CHAR_SPACE;
		msg += getInfo( pointer );
		mTextViewInfo.setText( msg );
	}

	/**
	 * getFormatedDate
	 */  
	private String getFormatedDate( String format, int num ) {
		long time = mStartTime + num * TIME_DIV;
		CharSequence date = DateFormat.format( format, time );
		return (String)date;
	}

	/**
	 * getInfo
	 */ 
	private String getInfo( int num ) {
		String str = "";
		if ( mLatLngList != null ) {
			LatLng lat = mLatLngList.get( num );
			str += getInfoLat( lat.getLatitudeDeg() ) + CHAR_SPACE;
			str += getInfoLng( lat.getLongitudeDeg() ) + CHAR_SPACE;	
		}		
		if ( mAziEleList != null ) {
			AziEle azi = mAziEleList.get( num );
			str += strAzimuth + mInfoFormat.format( azi.getAzimuthDeg() ) + CHAR_SPACE;
			str += strElevation + mInfoFormat.format( azi.getElevationDeg() ) + CHAR_SPACE;
		}
		return str;
	}

	/**
	 * getLocLat
	 */  
	private String getLocLat( double val ) {
		String[] ret = getNS( val );
		String str = ret[0] + mLocFormat.format( Math.abs( val ) ) + CHAR_SPACE + ret[1];
		return str;
	}

	/**
	 * getLocLng
	 */  
	private String getLocLng( double val ) {
		String[] ret = getEW( val );
		String str = ret[0] + mLocFormat.format( Math.abs( val ) ) + CHAR_SPACE + ret[1];
		return str;
	}
		
	/**
	 * getInfoLat
	 */  
	private String getInfoLat( double val ) {
		String[] ret = getNS( val );
		String str = ret[0] + getInfoLatLng( val ) + CHAR_SPACE + ret[1];
		return str;
	}

	/**
	 * getInfoLng
	 */  
	private String getInfoLng( double val ) {
		String[] ret = getEW( val );
		String str = ret[0] + getInfoLatLng( val ) + CHAR_SPACE + ret[1];
		return str;
	}

	/**
	 * getNS
	 */ 
	private String[] getNS( double val ) {
		String pre = strPreNorth;
		String post = strPostNorth;
		if ( val < 0 ) {
			pre = strPreSouth;
			post = strPostSouth;
		}
		String[] ret = new String[2];
		ret[0] = pre;
		ret[1] = post;
		return ret;
	}

	/**
	 * getEW
	 */ 
	private String[] getEW( double val ) {
		String pre = strPreEast;
		String post = strPostEast;
		if ( val < 0 ) {
			pre = strPreWest;
			post = strPostWest;
		}
		String[] ret = new String[2];
		ret[0] = pre;
		ret[1] = post;
		return ret;
	}
	
	/**
	 * getInfoLatLng
	 */ 	
	private String getInfoLatLng( double val ) {	
		String str = ""; 
		String deci = mInfoFormat.format( Math.abs( val ) );
		if ( deci.length() == 3 ) {
			str = CHAR_SPACE + CHAR_SPACE ;
		} else if ( deci.length() == 4 ) {
			str = CHAR_SPACE ;
		}
		str += deci;
		return str;
	}
	
	/**
	 * log_d
	 */ 		 
	@SuppressWarnings("unused")
	private void log_d( String msg ) {
		if (D) Log.d( TAG, msg );		
	}

}
