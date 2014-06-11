package jp.ohwada.android.tle;

import jp.ohwada.android.michibikifinder.Constant;

/**
 * TleConstant
 */
public class TleConstant {

    public final static boolean DEBUG = Constant.DEBUG;
	public final static String TAG = "Tle";

	public final static String DIR = Constant.DIR;
		
	// 2 day
	public final static long TIME_EXPIRE = 2 * 24 * 60 * 60;
	
	// TLE
	public final static String URL_GPS = "lhttp://www.celestrak.com/NORAD/elements/gps-ops.txt";
	public final static String URL_SBAS = "http://www.celestrak.com/NORAD/elements/sbas.txt";

	public final static String FILE_GPS = "gps-ops.txt";
	public final static String FILE_SBAS = "sbas.txt";
		
	public final static String NAME_QZS1 = "QZS-1";
}
