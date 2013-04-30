package columbia.cellular.droidtransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import columbia.cellular.Utils.DLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Application;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class droidApp extends Application{
	public boolean isServiceRegistered = false;
	public String myNickName=null;
	public String myEmail=null;
	public String myIMEI=null;
	public String myRootPath=null;
	public boolean isNameSet = false;
	public String regId=null;
	
	/**
     * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
     */
    public static final String SERVER_URL = "http://localhost:8080/gcm-demo";

    /**
     * Google API project id registered to use GCM.
     */
    public static final String SENDER_ID = "178896580049";
	
	private Handler mainHandler;
	public String ACTION_SEND_REQUEST ="action_send_request";
	public int RESPONSE_MESSAGE = 0;
	
	@Override
	public void onCreate() {
		super.onCreate();
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
	
	public void setNickName (String name){
		this.myNickName = name;
	}
	public String getNickName(){
		return this.myNickName;
	}
	
	public void setEmail(String email){
		this.myEmail=email;
	}
	public String getEmail (){
		return this.myEmail;
	}
	
	public void setIMEI(String imei){
		this.myIMEI=imei;
	}
	public String getIMEI (){
		return this.myIMEI;
	}
	
	public void setRootPath(String root){
		this.myRootPath=root;
	}
	public String getRootPath(){
		return this.myRootPath;
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
	
	public void setRegId(String id){
		this.regId = id;
	}
	
	public String getRegId(){
		return this.regId;
	}
	
	public void sendMessageToHandler(Message msg) {
		if (mainHandler != null) {
			mainHandler.sendMessage(msg);
		}
	}
	
	
	public String getLoginJSON(){
		JSONObject a = new JSONObject();
		
    	return a.toString();	
	}
	
	public String makeLoginResponseJSON(List<Map<String,Object>> list) throws JSONException{
		
		/*The input list is supposed to be a list of users:
		 * for a user ,the map contains: "username" & "userstate"
		 * */
		String json="";
		JSONObject responseJSON = new JSONObject();
		JSONArray users = new JSONArray();
		
		for(Map<String,Object> map : list){
			JSONObject singleJSON = new JSONObject();
			//singleJSON.append("nickname", map.get("nickname"));
			//singleJSON.append("email", map.get("email"));
			//singleJSON.append("state",map.get("state") );
			responseJSON.accumulate("users", singleJSON);
		}
		
		return json;
	}
	
	public List<Map<String,Object>> parseLoginResponseJSON (String json) throws JSONException{
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		JSONObject responseJSON = new JSONObject(json);
		JSONArray users = responseJSON.getJSONArray("users");
		
		for (int i=0;i<users.length();i++){
			JSONObject user = users.getJSONObject(i);
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("nickename", user.get("nickname"));
			map.put("email", user.get("email"));
			map.put("state", user.get("state"));
			list.add(map);	
		}
		
		return list;
	}

}
