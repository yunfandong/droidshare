package columbia.cellular.api.entities;

import columbia.cellular.api.service.ApiEntity;
import columbia.cellular.api.service.ApiError;
import android.app.Activity;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public abstract class FtDroidActivity extends ListActivity {
	
	public static final String PREF_DEVICE_TOKEN = "device_token";
	public static final String PREF_DEVICE_NICKNAME = "nickname";
	public static final String PREF_DEVICE_ID = "id";
	public static final String PREF_GCM_REGISTRATION_ID = "gcm_reg_id";
	public static final String PREF_EMAIL_ADDRESS = "email";
	public static final String PREFS_DEVICE = "devicePreferences";
	public static final String PREFS_SETTINGS = "deviceSettings";
	private Device device;
	
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

	public abstract void entityReceived(ApiEntity entity);
	public abstract void handleError(ApiError[] errors, ApiEntity entity);
	
}

