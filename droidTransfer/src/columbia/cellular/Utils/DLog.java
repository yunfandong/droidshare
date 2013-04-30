package columbia.cellular.Utils;

import android.util.Log;


public class DLog {
private static String tag = "# droidTransfer #";
	
	private static boolean debug = true;

	public static boolean isDebug() {
		return debug;
	}
	
	public static void v(String message) {
		if (debug) {
			Log.v(tag, message);
		}
	}

	public static void d(String message) {
		if (debug) {
			Log.d(tag, message);
		}
	}

	public static void i(String message) {
		if (debug) {
			Log.i(tag, message);
		}
	}

	public static void w(String message) {
		if (debug) {
			Log.w(tag, message);
		}
	}

	public static void e(String message) {
		if (debug) {
			Log.e(tag, message);
		}
	}

}
