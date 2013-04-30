package columbia.cellular.api.apicalls;



import org.json.JSONException;
import org.json.JSONObject;

import columbia.cellular.api.entities.Device;
import columbia.cellular.api.entities.DeviceMessage;
import columbia.cellular.api.entities.FtDroidActivity;
import columbia.cellular.api.service.ApiAuthenticator;
import columbia.cellular.api.service.ApiError;
import columbia.cellular.api.service.ApiLog;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiResponse;



public abstract class ApiCall {
	protected ApiRequestWrapper apiRequest;
	protected FtDroidActivity androidActivity;

	public ApiCall(FtDroidActivity activity) {
		this.androidActivity = activity;
		Device thisDevice = activity.getRegisteredDevice();
		if (thisDevice != null) {
			ApiAuthenticator.setDeviceNickname(thisDevice.getNickname());
			ApiAuthenticator.setDeviceToken(thisDevice.getToken());
		}
	}

	protected void processAsync() {
		if (apiRequest == null) {
			throw new IllegalArgumentException("api request is null");
		}
		ApiCallAsyncTask requestTask = new ApiCallAsyncTask(this);
		requestTask.execute(apiRequest);
	}

	public abstract void responseReceived(ApiResponse apiResponse);

	/**
	 * 
	 * @param done
	 * @param total
	 *            Usually performed on the UI thread
	 * 
	 */
	public void progressUpdated(long done, long total){}

	public void errorReceived(ApiResponse apiResponse) {
		ApiError[] errors = apiResponse.getErrors();
		JSONObject rawJSON = apiResponse.getJsonResponse();
		DeviceMessage messageEntity = null;
		if (rawJSON.has("ref_message") && !rawJSON.isNull("ref_message")) {
			try {
				messageEntity = new DeviceMessage(
						rawJSON.getJSONObject("ref_message"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
			}
		}
		androidActivity.handleError(errors, messageEntity);
	}

	public void emptyResponse(Exception e) {
		ApiLog.e("Exception: ", e);
		androidActivity.handleError(null, null);
	}

	protected void _processMessageResponse(ApiResponse apiResponse) {
		// TODO Auto-generated method stub
		JSONObject responseJSON = apiResponse.getJsonResponse();
		try {
			DeviceMessage deviceMsg = new DeviceMessage(
					responseJSON.getJSONObject("message"));
			androidActivity.entityReceived(deviceMsg);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			ApiLog.e("Could not created Message", e);
			androidActivity.handleError(null, null);
		}
	}
	
}
