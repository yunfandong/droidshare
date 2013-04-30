package columbia.cellular.api.service;

import android.util.Log;

public class ApiLog {
	
	static final String TAG = "ApiWarn";
	static public void w(String msg){
		Log.w(TAG, msg);
	}
	
	static public void e(String msg){
		Log.e(TAG, msg);
	}
	static public void e(String msg, Throwable t){
		Log.e(TAG, msg, t);
	}
	
	static public void i(String msg){
		Log.i(TAG, msg);
	}		
}
