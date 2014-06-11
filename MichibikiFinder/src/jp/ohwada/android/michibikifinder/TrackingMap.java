package jp.ohwada.android.michibikifinder;

import java.util.ArrayList;
import java.util.List;

import jp.ohwada.android.satellite.LatLng;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * TrackingMap
 */
public class TrackingMap {

	private final static boolean D = true;
	private final static String TAG = "TrackingMap";

	private final static int COLOR_BLACK = Color.argb( 255, 0, 0, 0 );
	private final static int COLOR_RED = Color.argb( 255, 255, 0, 0 );
	private final static int COLOR_BLUE = Color.argb( 255, 0, 0, 255 );
	private final static int COLOR_MAGENTA = Color.argb( 255, 255, 0, 255 );

	private final static double GEO_LAT = 0;
	private final static double GEO_LNG = 135;
	private final static int GEO_ZOOM = 3;

	private final static float RADIUS_POINT = 8.0f;
	private final static float RADIUS_LOCATION = 5.0f;
		    	
	private MapView mMapView;
	private MapController mMapController;

	private Paint mPaintTrace;
	private Paint mPaintCurrent;
	private Paint mPaintPointer;
	private Paint mPaintLocation;

	private List<GeoPoint> mGeoPointList = null;
	private GeoPoint mGeoPointLocation = null;

	private int mCurrentNum = 0;
	private int mPointer = 0;

	private boolean isTrace = false;
			
    /** 
	 * === constructor ===
	 */	
	public TrackingMap( MapView view ) {
		mMapView = view;
	}

    /** 
	 * init
	 */	
	public void init() { 			
		mMapController = mMapView.getController();
    	mMapController.setCenter( getGeoPoint( GEO_LAT, GEO_LNG ) );
    	mMapController.setZoom( GEO_ZOOM );
		mPaintTrace = new Paint();
		mPaintTrace.setColor( COLOR_MAGENTA );
    	mPaintCurrent = new Paint();
		mPaintCurrent.setColor( COLOR_RED );
    	mPaintPointer = new Paint();
		mPaintPointer.setColor( COLOR_BLUE );
 		mPaintLocation = new Paint();
		mPaintLocation.setColor( COLOR_BLACK );
		mMapView.getOverlays().add( mOverlay );
    }

    /** 
	 * setList
	 */	
	public void setList( List<LatLng> list ) {
		mGeoPointList = getGeoPointList( list );
		mMapView.invalidate(); 
	}

    /** 
	 * setLocation
	 */	
	public void setLocation( double lat, double lng ) {
		mGeoPointLocation = getGeoPoint( lat, lng );
		mMapView.invalidate(); 
	}

    /** 
	 * getGeoPointList
	 */	
	private List<GeoPoint> getGeoPointList( List<LatLng> list1 ) {
		List<GeoPoint> list2 = new ArrayList<GeoPoint>(); 
		for ( int i = 0; i < list1.size(); i++ ) {  
			LatLng sat = list1.get( i );
			list2.add( getGeoPoint( sat ) );
    	}
       	return list2;         
    }

	/**
	 * getGeoPoint
	 */
	private GeoPoint getGeoPoint( LatLng sat ) {
		return getGeoPoint( sat.getLatitudeDeg(), sat.getLongitudeDeg() );
	}

	/**
	 * getGeoPoint
	 */
	private GeoPoint getGeoPoint( double latitude, double longitude ) {
		int lat = (int)( latitude * 1E6 );
		int lng = (int)( longitude * 1E6 );
		GeoPoint point = new GeoPoint( lat, lng );
		return point;	
	}

    /** 
     * setCurrenNum
     */	
	public void setCurrentNum( int num ) {
		mCurrentNum = num;  
		mMapView.invalidate(); 
	}

    /** 
     * updatePointer
     */	
	public void updatePointer( int num ) {
		mPointer = num;     
		mMapView.invalidate(); 
	}

    /** 
     * setTrace
     */	
	public void setTrace( boolean flag ) {
		isTrace = flag;      
	}

    /** 
     * Overlay
     */	
	Overlay mOverlay = new Overlay() {
		@Override
		public void draw( Canvas canvas, MapView view, boolean shadow ) {
			drawOverlay( canvas, view, shadow );
		}
	};

    /** 
     * drawOverlay
     */	
	private void drawOverlay( Canvas canvas, MapView view, boolean shadow ) {
		if ( shadow ) return;
		Projection projection = view.getProjection();

		Point current = null;
		Point pointer = null;

		// line
		if ( mGeoPointList != null ) {
			int size = mGeoPointList.size();
			float[] points = new float[ 2 * size ];
			for ( int i = 0; i < size; i ++ ) {
				Point p = projection.toPixels( mGeoPointList.get( i ), null );
				int ii = 2 * i;
				points[ ii ] = p.x;
				points[ ii + 1 ] = p.y;
				if ( i == mCurrentNum ) {
					current = p;
				}	
				if ( i == mPointer ) {
					pointer = p;
				}
			}
			canvas.drawLines( points, mPaintTrace );
		}
		
		// location
		if ( mGeoPointLocation != null ) {
			Point p = projection.toPixels( mGeoPointLocation, null );
			canvas.drawCircle( p.x, p.y, RADIUS_LOCATION, mPaintLocation );
		}

		// Current
		if ( current != null ) {				
			canvas.drawCircle( current.x, current.y, RADIUS_POINT, mPaintCurrent );
		}
				
		// pointer
		if ( isTrace && ( pointer != null )) {				
			canvas.drawCircle( pointer.x, pointer.y, RADIUS_POINT, mPaintPointer );
		}

	}
							    
	/**
	 * log_d
	 */	
	@SuppressWarnings("unused")
	private void log_d( String msg ) {
		if (D) Log.d( TAG, msg );
	}

}