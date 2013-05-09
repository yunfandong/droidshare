package columbia.cellular.api.apicalls;

import org.json.JSONException;
import org.json.JSONObject;

import columbia.cellular.Utils.DLog;
import columbia.cellular.api.entities.Device;
import columbia.cellular.api.entities.DeviceMessage;
import columbia.cellular.api.service.ApiAuthenticator;
import columbia.cellular.api.service.ApiError;
import columbia.cellular.api.service.ApiLog;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.droidtransfer.DroidApp;

public abstract class ApiCall {
	protected ApiRequestWrapper apiRequest;
	protected DroidApp androidApplication;
	protected ActivityApiResponseHandler handler;

	protected boolean returnEvents = true;

	public ApiCall(DroidApp application) {
		this.androidApplication = application;
		Device thisDevice = application.getRegisteredDevice();
		if (thisDevice != null) {
			ApiAuthenticator.setDeviceNickname(thisDevice.getNickname());
			ApiAuthenticator.setDeviceToken(thisDevice.getToken());
		}
	}

	public ApiCall(DroidApp application, ActivityApiResponseHandler handler) {
		this(application);
		this.handler = handler;
	}

	public void setResponseHandler(ActivityApiResponseHandler hd) {
		handler = hd;
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
	public void progressUpdated(long done, long total) {
	}

	public void errorReceived(ApiResponse apiResponse) {
		if (!returnEvents || handler == null) {
			return;
		}

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
		handler.handleError(errors, messageEntity);
	}

	public void emptyResponse(Exception e) {
		if (!returnEvents && handler != null) {
			return;
		}
		ApiLog.e("Exception: ", e);
		handler.handleError(null, null);
	}

	protected void _processMessageResponse(ApiResponse apiResponse) {
		if (!returnEvents || handler == null) {
			return;
		}

		// TODO Auto-generated method stub
		JSONObject responseJSON = apiResponse.getJsonResponse();
		if (responseJSON == null) {
			DLog.w("Api's response is null, something is wrong");
			return;
		}
		
		try {
			DeviceMessage deviceMsg = new DeviceMessage(
					responseJSON.getJSONObject("message"));
			handler.entityReceived(deviceMsg);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			ApiLog.e("Could not create Message", e);
			handler.handleError(null, null);
		}
	}

	public ApiRequestWrapper getApiRequest() {
		return apiRequest;
	}

	public void setApiRequest(ApiRequestWrapper apiRequest) {
		this.apiRequest = apiRequest;
	}

	public boolean isReturnEvents() {
		return returnEvents;
	}

	public void setReturnEvents(boolean returnEvents) {
		this.returnEvents = returnEvents;
	}

}
