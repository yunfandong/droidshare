package columbia.cellular.api.apicalls;

import org.json.JSONException;
import org.json.JSONObject;

import columbia.cellular.api.entities.Device;
import columbia.cellular.api.service.ApiLog;
import columbia.cellular.api.service.ApiParam;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.api.service.ApiServerConnector;
import columbia.cellular.droidtransfer.DroidApp;

public class Register extends ApiCall {
	
	

	public Register(DroidApp application) {
		super(application);
		// TODO Auto-generated constructor stub
	}

	public void registerDevice(Device device){
		apiRequest = new ApiRequestWrapper(ApiServerConnector.API_URL_REGIGSTER);
		apiRequest.addParam(new ApiParam<String>("nickname", device.getNickname()))
				.addParam(new ApiParam<String>("email", device.getEmail()))
				.addParam(new ApiParam<String>("device_id", device.getImei()))
				.addParam(new ApiParam<String>("gcm_app_id", device.getGcmAppID()))
		;
		apiRequest.setListener(new DefaultRequestListener());

		this.processAsync();
	}
	
	public void responseReceived(ApiResponse apiResponse) {
		if(!apiResponse.isSuccess()){
			ApiLog.e("Api Response returned false and response was received :o");
			return;
		}
		
		JSONObject responseObj = apiResponse.getJsonResponse();
		
		try {
			//process device response
			Device responseDevice = new Device(responseObj.getJSONObject("device"));
			androidApplication.setDeviceProperty(DroidApp.PREF_DEVICE_ID, ""+responseDevice.getId());
			androidApplication.setDeviceProperty(DroidApp.PREF_DEVICE_NICKNAME, responseDevice.getNickname());
			androidApplication.setDeviceProperty(DroidApp.PREF_DEVICE_TOKEN, responseDevice.getToken());
			androidApplication.setDeviceProperty(DroidApp.PREF_EMAIL_ADDRESS, responseDevice.getEmail());
			if(handler !=null){
				handler.entityReceived(responseDevice);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			if(handler !=null){
				handler.entityReceived(null);
			}
		}
		
	}

	@Override
	public void progressUpdated(long done, long total) {
		// TODO Auto-generated method stub
	}
	
	
}
