package columbia.cellular.api.apicalls;

import org.json.JSONException;
import org.json.JSONObject;

import columbia.cellular.api.entities.Device;
import columbia.cellular.api.service.ApiParam;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.api.service.ApiServerConnector;
import columbia.cellular.droidtransfer.DroidApp;

public class GcmUpdate extends ApiCall {


	public GcmUpdate(DroidApp application) {
		super(application);
		// TODO Auto-generated constructor stub
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
		if(handler != null){
			try {
				//process device response
				Device responseDevice = new Device(responseObj.getJSONObject("device"));
				handler.entityReceived(responseDevice);
			} catch (JSONException e) {
				handler.entityReceived(null);
			}
		}
	}
	
}
