package columbia.cellular.droidtransfer;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import columbia.cellular.Utils.DLog;
import columbia.cellular.api.entities.Device;
import columbia.cellular.file.OpenFileDialog;

public class DroidApp extends Application{
	public boolean isServiceRegistered = false;
	public boolean isNameSet = false;

	public static final String PREF_DEVICE_TOKEN = "device_token";
	public static final String PREF_DEVICE_NICKNAME = "nickname";
	public static final String PREF_DEVICE_ID = "id";
	public static final String PREF_GCM_REGISTRATION_ID = "gcm_reg_id";
	public static final String PREF_EMAIL_ADDRESS = "email";
	public static final String PREF_ROOT_PATH = "rootPath";
	public static final String PREFS_DEVICE = "devicePreferences";
	public static final String PREFS_SETTINGS = "deviceSettings";
	private Device device;
	
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
		createDefaultSettings();
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

	public boolean isRegistered(){
		String deviceNickname = deviceProperty(PREF_DEVICE_NICKNAME, "");
		String deviceToken = deviceProperty(PREF_DEVICE_TOKEN, "");
		return deviceNickname.length() > 0 && deviceToken.length() > 0;
	}
	

	protected String getPreference(String category, String name, String defaultVal){
		SharedPreferences pref = getSharedPreferences(category, Activity.MODE_PRIVATE);
		return pref.getString(name, defaultVal);
	}
	protected String getPreference(String category, String name){
		return getPreference(category, name, "");
	}
	
	protected void savePreference(String category, String name, String value){
		SharedPreferences pref = getSharedPreferences(category, Activity.MODE_PRIVATE);
		Editor prefEditor = pref.edit();
		prefEditor.putString(name, value);
		prefEditor.commit();
	}	
	
	public String getSetting(String name, String defaultVal){
		return getPreference(PREFS_SETTINGS, name, defaultVal);
	}
	
	public void saveSetting(String name, String value){
			savePreference(PREFS_SETTINGS, name, value);
	}
	
	public String deviceProperty(String name, String defaultVal){
		return getPreference(PREFS_DEVICE, name, defaultVal);
	}
	
	public void setDeviceProperty(String name, String value){
			savePreference(PREFS_DEVICE, name, value);
	}	

	public Device getRegisteredDevice(){
		if(!isRegistered()){
			return null;
		}
		
		if(device == null){
			device = new Device(deviceProperty(PREF_DEVICE_NICKNAME, ""), deviceProperty(PREF_EMAIL_ADDRESS, ""), null);
			device.setGcmAppID(deviceProperty(PREF_GCM_REGISTRATION_ID, ""));
			device.setId(Integer.parseInt(deviceProperty(PREF_DEVICE_ID, "0")));
			device.setToken(deviceProperty(PREF_DEVICE_TOKEN, null));
		}
		
		return device;
	}

	protected void createDefaultSettings(){
		String rootPath = getSetting(PREF_ROOT_PATH, "");
		if(rootPath.length() < 1){
			rootPath = Environment.getExternalStorageDirectory().getPath();
			if(rootPath.length() < 1){
				rootPath = Environment.getRootDirectory().getPath();                                                                                                                                                                                                                                                                                                                                                                                                                                         
			}
			saveSetting(PREF_ROOT_PATH, Environment.getExternalStorageDirectory().getPath());
		}
	}
	
	public Map<String, Integer> getImageIds() {
		Map<String, Integer> images = new HashMap<String, Integer>();
		// file type adapter
		images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root); // root
		images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up); // back
		images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder); // folder
		images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
		images.put("music", R.drawable.filedialog_music);
		images.put("film", R.drawable.filedialog_film);
		images.put("pdf", R.drawable.filedialog_pdf);
		images.put("ppt", R.drawable.filedialog_ppt);
		images.put("db", R.drawable.filedialog_db);
		images.put("zip", R.drawable.filedialog_zip);
		images.put("txt", R.drawable.filedialog_txt);
		images.put("xls", R.drawable.filedialog_xls);
		images.put("java", R.drawable.filedialog_java);
		images.put("html", R.drawable.filedialog_html);
		images.put("code", R.drawable.filedialog_code);
		images.put("picture", R.drawable.filedialog_picture);
		images.put("application", R.drawable.filedialog_application);
		images.put("other", R.drawable.filedialog_file);
		return images;

	}
	
}
