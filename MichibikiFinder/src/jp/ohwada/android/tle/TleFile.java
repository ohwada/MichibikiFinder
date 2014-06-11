package jp.ohwada.android.tle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;

/**
 * TleFile
 */
public final class TleFile  { 
	// debug
    private final static boolean D = TleConstant.DEBUG; 

	/**
	 * constractor
	 */
    public TleFile() {
		// dummy
    }

	/**
	 * init
	 */ 
	public void init() {
		// make dir if not exists
		File dir = new File( getDir() );
		if ( !dir.exists() ) { 
			dir.mkdir();
		}
	}

	/**
	 * getFile
	 */ 
	public File getFile( String name ) {
		String path = getDir() + File.separator + name;
		File file = new File( path );
		return file;
	}
	
	/**
	 * write
	 * @param File file
	 * @param String msg
	 */ 
	public void write( File file, String data ) {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream( file, true );
			os.write( data.getBytes() );
		} catch ( FileNotFoundException e ) {
			if (D) e.printStackTrace();
		} catch ( IOException e ) {
			if (D) e.printStackTrace();
		}
		if ( os != null ) {
			try {
				os.close();
			} catch ( IOException e ) {
				if (D) e.printStackTrace();
			}
		}
	}

	/**
	 * read
	 * @param File file
	 * @return List<String> 
	 */ 
	public List<String> read( File file ) {
		List<String> list = new ArrayList<String>();
		FileInputStream is = null;
        String str = "";
		try{
			is = new FileInputStream( file );
        	BufferedReader buf = new BufferedReader( new InputStreamReader( is ) );
            while (((str = buf.readLine()) != null) && (str.length() > 1)) {
			    list.add( str );
			}
		} catch (IOException e) {
			if (D) e.printStackTrace();
		}
		if ( is != null ) {
			try {
				is.close();
			} catch ( IOException e ) {
				if (D) e.printStackTrace();
			}
		}
		return list;
    }

	/**
	 * isExpired
	 * @param File file
	 * @param long expire
	 * @return boolean : true = expired
	 */
    public boolean isExpired( File file, long expire ) {
		// if not exists
		if ( !file.exists() ) return true;
		// if expired	
		if ( System.currentTimeMillis() > ( file.lastModified() + expire ) ) return true;
		return false;
	}

	/**
	 * get dirctory
	 */ 
	private String getDir() {
		String path = Environment.getExternalStorageDirectory().getPath();
		path += File.separator + TleConstant.DIR;
		return path;
	}

}