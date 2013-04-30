package columbia.cellular.api.apicalls;

import org.json.JSONException;
import org.json.JSONObject;

import columbia.cellular.api.entities.Device;
import columbia.cellular.api.entities.FtDroidActivity;
import columbia.cellular.api.service.ApiParam;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.api.service.ApiServerConnector;

public class GcmUpdate extends ApiCall {

	public GcmUpdate(FtDroidActivity activity) {
		super(activity);
	}

	public void update(String gcmId){
		apiRequest = new ApiRequestWrapper(ApiServerConnector.API_URL_GCM_UPDATE);
		apiRequest.addParam(new ApiParam<String>("gcm_app_id", gcmId));
		apiRequest.setListener(new DefaultRequestListener());
		processAsync();
	}
	
	@Override
	public void responseReceived(ApiResponse apiResponse) {
		// TODO Auto-generated method stub
		JSONObject responseObj = apiResponse.getJsonResponse();
		try {
			//process device response
			Device responseDevice = new Device(responseObj.getJSONObject("device"));
			androidActivity.entityReceived(responseDevice);
		} catch (JSONException e) {
			androidActivity.entityReceived(null);
		}
	}
	
}
