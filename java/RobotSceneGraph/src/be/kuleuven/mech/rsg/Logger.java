package be.kuleuven.mech.rsg;

import android.util.Log;

/**
 * Abstraction of the Android logger to make the RSG
 * library independent from Android.
 * Use the useAndoidLogger flag to configure the desired behavior.
 * 
 * @author Sebastian Blumenthal
 *
 */
public class Logger {

	/** 
	 * Configure if Android logger is used. 
	 * If true the Android default logger is uesd. 
	 * If false the a standard System.out.println will be used.
	 * Default value is true.
	 */ 
	static private boolean useAndroidLogger = true;
	
	public static boolean isUseAndroidLogger() {
		return useAndroidLogger;
	}

	public static void setUseAndroidLogger(boolean useAndoidLogger) {
		Logger.useAndroidLogger = useAndoidLogger;
	}

	static public void debug(String tag, String message) {
		if (useAndroidLogger) {
			Log.d(tag, message);
		} else {
			System.out.println("[DEBUG] " + tag + ": " + message); 
		}
	}
	
	static public void info(String tag, String message) {
		if (useAndroidLogger) {
			Log.i(tag, message);
		} else {
			System.out.println("[INFO] " + tag + ": " + message); 
		}
	}
	
	static public void warning(String tag, String message) {
		if (useAndroidLogger) {
			Log.w(tag, message);
		} else {
			System.out.println("[WARNING] " + tag + ": " + message); 
		}
	}
	
	static public void error(String tag, String message) {
		if (useAndroidLogger) {
			Log.e(tag, message);
		} else {
			System.out.println("[ERROR] " + tag + ": " + message); 
		}
	}
	
}
