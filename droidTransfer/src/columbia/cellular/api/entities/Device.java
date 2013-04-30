package columbia.cellular.api.entities;



import org.json.JSONException;
import org.json.JSONObject;

import columbia.cellular.api.service.ApiEntity;
import columbia.cellular.api.service.ApiLog;

public final class Device extends ApiEntity {
	
	protected int id = 0;
	protected String imei;
	protected String email;
	protected boolean online;
	protected String token;
	protected String lastSeen;
	protected String nickname;
	protected String gcmAppID;
	
	public Device(JSONObject device){
		if(device == null){
			ApiLog.w("Device object is null");
		}
		
		try {
			id = device.getInt("id");
			imei = device.has("device_id") ? device.getString("device_id") : "";
			email = device.getString("email");
			nickname = device.getString("nickname");
			online = device.has("online") ? device.getBoolean("online") : false;
			lastSeen = device.getString("last_seen");
			token = device.has("token") ? device.getString("token") : "";
			gcmAppID = device.has("gcm_app_id") ? device.getString("gcm_app_id") : "";
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			ApiLog.w("JSON error occured while processing device: "+ e.getMessage());
		}
		
	}
	
	public Device(String nickname, String email, String imei, String gcmId){
		this.nickname  = nickname;
		this.email = email;
		this.gcmAppID = gcmId;
		this.imei = imei;
	}
	
	public Device(String nickname, String email, String imei){
		this(nickname, email, imei, "");
	}
	
	public Device(String nickname, String email){
		this(nickname, email, "", "");
	}	

	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getImei() {
		return imei;
	}


	public void setImei(String imei) {
		this.imei = imei;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public boolean isOnline() {
		return online;
	}


	public void setOnline(boolean online) {
		this.online = online;
	}


	public String getToken() {
		return token;
	}


	public void setToken(String token) {
		this.token = token;
	}


	public String getLastSeen() {
		return lastSeen;
	}


	public void setLastSeen(String lastSeen) {
		this.lastSeen = lastSeen;
	}


	public String getNickname() {
		return nickname;
	}


	public void setNickname(String nickname) {
		this.nickname = nickname;
	}


	public String getGcmAppID() {
		return gcmAppID;
	}


	public void setGcmAppID(String gcmAppID) {
		this.gcmAppID = gcmAppID;
	}

	@Override
	public String toString() {
		return "Device [id=" + id + ", imei=" + imei + ", email=" + email
				+ ", online=" + online + ", token=" + token + ", lastSeen="
				+ lastSeen + ", nickname=" + nickname + ", gcmAppID="
				+ gcmAppID + "]";
	}
	
	
	
}
