package columbia.cellular.api.apicalls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import columbia.cellular.api.entities.DeviceMessageList;
import columbia.cellular.api.entities.FtDroidActivity;
import columbia.cellular.api.service.ApiLog;
import columbia.cellular.api.service.ApiParam;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.api.service.ApiServerConnector;

public class Messages extends ApiCall {

	public Messages(FtDroidActivity activity) {
		super(activity);
	}

	public void poll(long reply_to, long since_ts) {
		apiRequest = new ApiRequestWrapper(ApiServerConnector.API_URL_MESSAGES);
		if (reply_to > 0) {
			apiRequest
					.addParam(new ApiParam<String>("reply_to", "" + reply_to));
		}

		if (since_ts > 0) {
			apiRequest
					.addParam(new ApiParam<String>("since_ts", "" + since_ts));
		}

		apiRequest.setListener(new DefaultRequestListener());
		processAsync();
	}

	public void poll() {
		poll(0, 0);
	}

	public void poll(long reply_to) {
		poll(reply_to, 0);
	}

	@Override
	public void responseReceived(ApiResponse apiResponse) {
		JSONObject responseJson = apiResponse.getJsonResponse();
		if (responseJson.has("messages") && !responseJson.isNull("messages")) {
			try {
				JSONArray messages = responseJson.getJSONArray("messages");
				int limit = responseJson.has("limit") ? responseJson.getInt("limit") : 0;
				int offset = responseJson.has("offset") ? responseJson.getInt("offset") : 0;
				
				DeviceMessageList messageList  = new DeviceMessageList(messages, limit, offset);
				androidActivity.entityReceived(messageList);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				ApiLog.e("JSONException : " + e.getMessage());
				androidActivity.handleError(null, null);
			}

		} else {
			androidActivity.handleError(null, null);
		}
	}

}
