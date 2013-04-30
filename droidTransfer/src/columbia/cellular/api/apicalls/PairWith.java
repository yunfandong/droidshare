package columbia.cellular.api.apicalls;

import org.json.JSONException;
import org.json.JSONObject;

import columbia.cellular.api.entities.Device;
import columbia.cellular.api.entities.DeviceMessage;
import columbia.cellular.api.entities.FtDroidActivity;
import columbia.cellular.api.service.ApiLog;
import columbia.cellular.api.service.ApiParam;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiServerConnector;


public class PairWith extends ApiCall {

	public PairWith(FtDroidActivity activity) {
		super(activity);
	}

	public void pairWith(Device device){
		String nickname = device.getNickname();
		String email = device.getEmail();
		apiRequest = new ApiRequestWrapper(ApiServerConnector.API_URL_PAIR_WITH);
		if(email == null && nickname == null){
			throw new IllegalArgumentException("Device must contain either e-mail or nickname");
		}
		if(email != null){
			apiRequest.addParam(new ApiParam<String>("email", email, ApiParam.TYPE_EMAIL));
		}
		
		if(nickname != null){
			apiRequest.addParam(new ApiParam<String>("nickname", nickname, ApiParam.TYPE_STRING));
		}
		
		apiRequest.setListener(new DefaultRequestListener());
		processAsync();
	}
	
	
	@Override
	public void responseReceived(ApiResponse apiResponse) {
		// TODO Auto-generated method stub
		JSONObject responseJSON = apiResponse.getJsonResponse();
		try {
			DeviceMessage deviceMsg = new DeviceMessage(responseJSON.getJSONObject("message"));
			androidActivity.entityReceived(deviceMsg);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			ApiLog.e("Could not created Message", e);
			androidActivity.handleError(null, null);
		}
	}


	@Override
	public void progressUpdated(long done, long total) {
		// TODO Auto-generated method stub
		
	}

}
