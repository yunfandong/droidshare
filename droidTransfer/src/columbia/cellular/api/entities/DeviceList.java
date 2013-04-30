package columbia.cellular.api.entities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import columbia.cellular.api.service.ApiEntity;

public class DeviceList extends ApiEntity{
	private ArrayList<Device> devices;
	
	public DeviceList(JSONArray devicesJson) {
		devices = new ArrayList<Device>();
		if(devicesJson == null){
			return;
		}
		
		Device tmpDevice;
		for(int i=0; i<devicesJson.length();i++){
			try {
				tmpDevice = new Device(devicesJson.getJSONObject(i));
				devices.add(tmpDevice);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	public int size(){
		return devices.size();
	}

	public ArrayList<Device> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<Device> devices) {
		this.devices = devices;
	}

	@Override
	public String toString() {
		return "DeviceList [devices=" + devices + "]";
	}
	
	
}
