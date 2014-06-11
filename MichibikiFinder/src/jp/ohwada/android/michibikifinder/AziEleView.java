package jp.ohwada.android.michibikifinder;

import java.util.ArrayList;
import java.util.List;

import jp.ohwada.android.nmea.NmeaSatellite;
import jp.ohwada.android.satellite.AziEle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

/**
 * AziEleView
 */
public class AziEleView extends View {

	private final static String TAG  = "AziEleView";

	private final static int COLOR_GRAY = Color.argb( 255, 128, 128, 128 );
	private final static int COLOR_RED = Color.argb( 255, 255, 0, 0 );
	private final static int COLOR_BLUE = Color.argb( 255, 0, 0, 255 );
	private final static int COLOR_MAGENTA = Color.argb( 255, 255, 0, 255 );
	private final static int COLOR_DARK_GREEN = Color.argb( 255, 0, 128, 0 );
	private final static int COLOR_SKY_BLUE = Color.argb( 255, 128, 224, 224 );

	private final static String LABEL_NORTH = "N";
	private final static String LABEL_SOUTH = "S";
	private final static String LABEL_EAST = "E";
	private final static String LABEL_WEST = "W";
	
	private final static double DEG_TO_RAD = Math.PI / 180.0;
	private final static float AXIS_TEXT_SIZE = 16;
	private final static float AXIS_TEXT_MARGIN = 4;
	private final static float SAT_TEXT_SIZE = 16;
	private final static float SAT_RADIUS = 12;
    private final static float POINT_RADIUS = 5;

	private final static double ELEVATION_RANGE = 90;
	private final static double AZIMUTH_OFFSET = 270;
            
    private Bitmap mBitmapAxis = null;
    private Paint mPaintTrace = null;
    private Paint mPaintCurrent = null;
    private Paint mPaintPointer = null;
    private Paint mPaintName= null;
    private Paint mPaintSat = null;
    private Paint mPaintQz = null;
    
	private float mDensity = 0;
 		
	private float mCenterX = 0;  
	private float mCenterY = 0;  

    private float mAxisRadius = 0;
    private float mTextHalfHeight = 0;
	private float mSatRadius = 0;
	private float mPointRadius = 0;

    private List<Point> mTraceList = new ArrayList<Point>();
    private List<SatellitePoint> mSatelliteList = new ArrayList<SatellitePoint>();
	private int mCurrentNum = 0;
	private int mPointer = 0;
	private boolean isTrace = false;

    /** 
	 * === constructor ===
	 */	
	public AziEleView( Context context, AttributeSet attrs, int defStyleAttr ) {
     	super( context, attrs, defStyleAttr );
     	initView( context );
	}

    /** 
	 * === constructor ===
	 */	
	public AziEleView( Context context, AttributeSet attrs ) {
     	super( context, attrs );
     	initView( context );
    }
     	
    /** 
	 * === constractor ===
	 */	        
	public AziEleView( Context context ) {
	    super( context );
     	initView( context );
	}

    /** 
	 * initView
	 */	 
	private void initView( Context context ) {
		getScaledDensity();
		mPaintTrace = new Paint();
		mPaintTrace.setColor( COLOR_MAGENTA );
		mPaintCurrent = new Paint();
		mPaintCurrent.setColor( COLOR_RED );				
		mPaintPointer = new Paint();
		mPaintPointer.setColor( COLOR_BLUE );
		mPaintSat = new Paint();
		mPaintSat.setStyle( Paint.Style.STROKE );
		mPaintSat.setColor( COLOR_DARK_GREEN );
		mPaintSat.setStrokeWidth( 2 );		
		mPaintQz = new Paint();
		mPaintQz.setStyle( Paint.Style.STROKE );
		mPaintQz.setColor( COLOR_RED );
		mPaintQz.setStrokeWidth( 4 );
		mPaintName = new Paint( Paint.ANTI_ALIAS_FLAG );
		mPaintName.setTextSize( SAT_TEXT_SIZE * mDensity );
		FontMetrics metrics = mPaintName.getFontMetrics();
		mTextHalfHeight = (metrics.ascent + metrics.descent) / 2;
	}

    /** 
	 * getScaledDensity
	 */	
    private void getScaledDensity() { 
     	DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
 		mDensity = metrics.scaledDensity;
 	}
 
    /** 
	 * === onWindowFocusChanged ===
	 */	
	@Override  
	public void onWindowFocusChanged( boolean hasFocus ) {  
		super.onWindowFocusChanged( hasFocus );  
		int width = getWidth();  
		int height = getHeight();
		float text_hm = ( AXIS_TEXT_SIZE + AXIS_TEXT_MARGIN ) * mDensity; 
		mCenterX = width / 2;  
		mCenterY =  height / 2;
		float h = ( height - text_hm ) / 2;
		float r = Math.min( mCenterX, h );
		mAxisRadius = (float) ( 0.9 * r ); 
		mSatRadius = SAT_RADIUS * mDensity;
		mPointRadius = POINT_RADIUS * mDensity;;		
		initAxis( width, height );
		invalidate();   
	}

    /** 
	 * initAxis
	 */	
	private void initAxis( int width, int height ) {
		Paint paint_back = new Paint();
		paint_back.setColor( COLOR_SKY_BLUE );		
		Paint paint_line = new Paint();
		Paint paint_circle = new Paint();
		paint_circle.setStyle( Paint.Style.STROKE );
		Paint paint_edge = new Paint();
		paint_edge.setStyle( Paint.Style.STROKE );
		paint_edge.setStrokeWidth( 2 );		
		Paint paint_text = new Paint( Paint.ANTI_ALIAS_FLAG );
		paint_text.setTextSize( AXIS_TEXT_SIZE * mDensity );

		float text_h = AXIS_TEXT_SIZE * mDensity; 		
		float text_m = AXIS_TEXT_MARGIN * mDensity;
		float back_r = mAxisRadius + text_h + text_m;
		float line_x0 = mCenterX - mAxisRadius;
		float line_x1 = mCenterX + mAxisRadius;
		float line_y0 = mCenterY - mAxisRadius;
		float line_y1 = mCenterY + mAxisRadius;
		float text_w = paint_text.measureText( LABEL_NORTH );
		float n_x = mCenterX - text_w / 2;
		float n_y = line_y0 - text_m;
		float s_x = n_x;
		// todo
		float s_y = line_y1 + text_m + text_h - 4;
		float e_x = line_x0 - text_m - text_w;
		float e_y = mCenterY + text_h / 2 ;
		float w_x = line_x1 + text_m ;
		float w_y = e_y;
		
		mBitmapAxis = Bitmap.createBitmap( width, height, Bitmap.Config.RGB_565 );
		Canvas canvas = new Canvas();
		canvas.setBitmap( mBitmapAxis );
		canvas.drawColor( COLOR_GRAY );
		canvas.drawCircle( mCenterX, mCenterY, back_r, paint_back );
        canvas.drawLine( line_x0, mCenterY, line_x1, mCenterY, paint_line );
        canvas.drawLine( mCenterX, line_y0, mCenterX, line_y1, paint_line );
		canvas.drawText( LABEL_NORTH, n_x, n_y, paint_text );
		canvas.drawText( LABEL_SOUTH, s_x, s_y, paint_text );
		canvas.drawText( LABEL_EAST, e_x, e_y, paint_text );
		canvas.drawText( LABEL_WEST, w_x, w_y, paint_text );
		
		float div = mAxisRadius / 3;
		canvas.drawCircle( mCenterX, mCenterY, div, paint_circle );
		canvas.drawCircle( mCenterX, mCenterY, 2 * div, paint_circle );
		canvas.drawCircle( mCenterX, mCenterY, mAxisRadius, paint_edge );
	}
	        
    /** 
     * === onDraw ===
     */	        
	@Override
    protected void onDraw( Canvas canvas ) {
    	// axis
    	if ( mBitmapAxis != null ) {
    		canvas.drawBitmap( mBitmapAxis, 0, 0, null );
    	}

		// trace
		int size = mTraceList.size();
		float[] points = new float[ 2 * size ];
		Point current = null;
		Point pointer = null;
		for ( int i = 0; i < size; i++ ) {
			Point p = mTraceList.get( i );
			int ii = 2 * i;
			points[ ii ] = (float)p.x;
			points[ ii + 1 ] = (float)p.y;
			if ( i == mCurrentNum ) {
				current = p;
			}	
		    if ( i == mPointer ) {
		    	pointer = p;
		    }
        }
		canvas.drawLines( points, mPaintTrace );

		// Current
		if ( current != null ) {
			canvas.drawCircle( current.x, current.y, mPointRadius, mPaintCurrent );				
		}
				
		// pointer
		if ( isTrace && ( pointer != null )) {	
			canvas.drawCircle( pointer.x, pointer.y, mPointRadius, mPaintPointer );			
		}

		// 	satellite	
		for ( int i = 0; i < mSatelliteList.size(); i++ ) {
			SatellitePoint p = mSatelliteList.get( i );
			if ( p.is_qz ) {
		    	canvas.drawCircle( p.sat_x, p.sat_y, mSatRadius, mPaintQz );
		    } else {
				canvas.drawCircle( p.sat_x, p.sat_y, mSatRadius, mPaintSat );
		    }
		    canvas.drawText( p.name, p.name_x, p.name_y, mPaintName );
        }
 		    
	}

    /** 
     * setTraceList
     */	        
    public void setTraceList( List<AziEle> list ) {
     	mTraceList.clear();
		for( AziEle sat : list ) {
	    	float[] xy = getXY( sat.getElevationDeg(), sat.getAzimuthDeg() );
	    	int x = (int)( xy[0] * mAxisRadius + mCenterX );
 			int y = (int)( xy[1] * mAxisRadius + mCenterY );
	    	Point point = new Point( x, y );
    		mTraceList.add( point );
		} 
        invalidate();            
    }

    /** 
     * setSatelliteList
     */	        
    public void setSatelliteList( List<NmeaSatellite> satellites ) {
     	mSatelliteList.clear();
		for( NmeaSatellite sat : satellites ) {
			if ( sat != null ) {
				SatellitePoint point = new SatellitePoint( sat );
    			if ( point.valid ) {
    				mSatelliteList.add( point );
	    		}
    		}
		} 
        invalidate();            
    }

   /** 
     * setCurrenNum
     */	
	public void setCurrentNum( int num ) {
		mCurrentNum = num;    
	}

    /** 
     * updatePointer
     */	
	public void updatePointer( int num ) {
		mPointer = num;
        invalidate();       
	}

    /** 
     * setTrace
     */	
	public void setTrace( boolean flag ) {
		isTrace = flag;
        invalidate();       
	}
		
    /** 
     * getXY
     */	 
    private float[] getXY( double elevation_deg, double azimuth_deg ) {
 		float[] ret = new float[2];
    	double r = ( ELEVATION_RANGE - elevation_deg ) / ELEVATION_RANGE;
    	// north : right (0) -> top (90)
 		double a = ( AZIMUTH_OFFSET + azimuth_deg ) * DEG_TO_RAD;
 		ret[0] = (float)( r * Math.cos( a ) );
 		ret[1] = (float)( r * Math.sin( a ) );
		return ret;
	}
 
    /** 
     * class SatellitePoint
     */	
 	private class SatellitePoint { 

		public boolean valid = false; 
		public String name = "";	
		public boolean is_qz = false;
		public float sat_x = 0;
		public float sat_y = 0;
		public float name_x = 0;
		public float name_y = 0;

    	/** 
		 * constractor
		 */			
 		public SatellitePoint( NmeaSatellite sat ) {
 		 	double ele = sat.getElevation();
 		 	double azi = sat.getAzimuth();
 		 	double snr = sat.getSnr();
 			if (( ele < 2 )&&( azi  < 1 )) return;
 			if ( snr < 1 ) return;
 			valid = true; 
 		 	is_qz= sat.isQz() ;
 			name = is_qz ? "Q" : "" ;
			name += Integer.toString( sat.getPrn() );
			float[] xy = getXY( ele, azi );
			// todo
	    	sat_x = - xy[0] * mAxisRadius + mCenterX;
 			sat_y = xy[1] * mAxisRadius + mCenterY;
			name_x = sat_x - mPaintName.measureText( name ) / 2;
			name_y = sat_y - mTextHalfHeight;
		}	
	}	
   
	/**
	 * log_d
	 */	
	@SuppressWarnings("unused")
	private void log_d( String str ) {
		Log.d( TAG, str );
	}
}