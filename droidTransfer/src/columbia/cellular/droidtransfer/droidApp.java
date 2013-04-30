package columbia.cellular.droidtransfer;

import java.util.HashMap;

import android.app.Application;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import columbia.cellular.Utils.DLog;

public class droidApp extends Application{
	public boolean isServiceRegistered = false;
	public boolean isNameSet = false;
	
	private HashMap<String, Object> registry;
	
	/**
     * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
     */

    /**
     * Google API project id registered to use GCM.
     */
    public static final String SENDER_ID = "178896580049";
	
	private Handler mainHandler;
	public String ACTION_SEND_REQUEST ="action_send_request";
	public static int RESPONSE_MESSAGE = 0;
	
	@Override
	public void onCreate() {
		super.onCreate();
		registry = new HashMap<String, Object>();
		DLog.i("Application Started");
	}
	
	public void showToast(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
	public void showToastLong(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
	
	public boolean getServiceState(){
		return isServiceRegistered;
	}
	
	public void setServiceState(boolean s){
		this.isServiceRegistered = s;
	}
	
	public void SetNameFlag (boolean b){
		this.isNameSet = b;
	}
	
	public boolean whetherNameSet(){
		return this.isNameSet;
	}
	

	public void setMainHandler(Handler handler){
		this.mainHandler = handler;
	}
	
	public void sendMessageToHandler(Message msg) {
		if (mainHandler != null) {
			mainHandler.sendMessage(msg);
		}
	}
	

	
	public void writeAnything(String any){
		Log.i("Anything", "Anything "+any);
	}
	
	public void saveToRegistry(String key, Object object){
		registry.put(key, object);
	}
	
	public Object getFromRegistry(String key, Object defaultVal){
		if(registry.containsKey(key)){
			return registry.get(key);
		}
		return defaultVal;
	}
	
	public Object getFromRegistry(String key){
		return getFromRegistry(key, null);
	}

}
