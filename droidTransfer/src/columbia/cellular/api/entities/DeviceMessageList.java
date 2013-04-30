package columbia.cellular.api.entities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import columbia.cellular.api.service.ApiEntity;



public class DeviceMessageList extends ApiEntity{
	private ArrayList<DeviceMessage> messages;
	protected int limit;
	protected int offset;
	
	public DeviceMessageList(JSONArray messagesJson, int limit, int offset) {
		messages = new ArrayList<DeviceMessage>();
		
		this.limit = limit;
		this.offset = offset;
		
		if(messagesJson == null){
			return;
		}
		
		DeviceMessage tmpMessage;
		for(int i=0; i<messagesJson.length();i++){
			try {
				tmpMessage = new DeviceMessage(messagesJson.getJSONObject(i));
				messages.add(tmpMessage);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	public int size(){
		return messages.size();
	}
	
	@Override
	public String toString() {
		return "DeviceMessageList [messages=" + messages + "]";
	}

	public ArrayList<DeviceMessage> getMessages() {
		return messages;
	}

	public int getLimit() {
		return limit;
	}

	public int getOffset() {
		return offset;
	}
	
}

