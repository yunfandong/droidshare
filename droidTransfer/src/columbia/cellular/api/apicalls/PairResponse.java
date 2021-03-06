package columbia.cellular.api.apicalls;

import org.json.JSONException;
import org.json.JSONObject;

import columbia.cellular.api.entities.DeviceMessage;
import columbia.cellular.api.service.ApiLog;
import columbia.cellular.api.service.ApiParam;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiServerConnector;
import columbia.cellular.droidtransfer.DroidApp;

public class PairResponse extends ApiCall{

	
	public PairResponse(DroidApp application) {
		super(application);
		// TODO Auto-generated constructor stub
	}

	public void sendResponse(boolean response, long inReplyTo){
		apiRequest = new ApiRequestWrapper(ApiServerConnector.API_URL_PAIR_RESPONSE);
		apiRequest.addParam(new ApiParam<String>("response", response ? "1" : "0", ApiParam.TYPE_BOOL))
				.addParam(new ApiParam<String>("in_reply_to", ""+inReplyTo));
		
		apiRequest.setListener(new DefaultRequestListener());
		processAsync();
	}

	@Override
	public void responseReceived(ApiResponse apiResponse) {
		// TODO Auto-generated method stub
		if(handler == null){
			return;
		}
		JSONObject responseJSON = apiResponse.getJsonResponse();
		try {
			DeviceMessage deviceMsg = new DeviceMessage(responseJSON.getJSONObject("message"));
			handler.entityReceived(deviceMsg);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			ApiLog.e("Could not created Message", e);
			handler.handleError(null, null);
		}		
	}

	@Override
	public void progressUpdated(long done, long total) {
		// TODO Auto-generated method stub
	}

}
