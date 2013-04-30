package columbia.cellular.api.service;

import java.util.Iterator;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiResponse {

	protected boolean success = true;
	protected ApiError[] errors;
	protected JSONObject jsonResponse;
	protected int statusCode;
	protected String rawResponse;

	public ApiResponse(String response, String contentType) {
		if (contentType.equals("application/json")) {
			try {
				jsonResponse = new JSONObject(response);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				ApiLog.w("JSON response error: " + e.getMessage());
			}
		} else {
			rawResponse = response;
		}
		statusCode = HttpStatus.SC_OK;
		//ApiLog.w("Raw Response: "+response + "\nContentType: "+contentType);
	}

	@SuppressWarnings("unchecked")
	public ApiResponse(String errorResponse, int statusCode) {
		success = false;
		this.statusCode = statusCode;
		if (statusCode == HttpStatus.SC_BAD_REQUEST
				|| statusCode == HttpStatus.SC_FORBIDDEN) {
			try {
				jsonResponse = new JSONObject(errorResponse);
				JSONObject errorMsgs = jsonResponse.getJSONObject("errors");
				if (!jsonResponse.isNull("errors")) {
					Iterator<String> keys = errorMsgs.keys();
					errors = new ApiError[errorMsgs.length()];
					int c = 0;
					String errorCode;
					while (keys.hasNext()) {
						errorCode = keys.next();
						errors[c++] = new ApiError(errorCode,
								errorMsgs.getString(errorCode));
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				ApiLog.e("JSON response error: " + e.getMessage()+" :: \n"+errorResponse);
			}
		} else {
			if (!jsonResponse.isNull("error")) {
				JSONObject err;
				try {
					err = jsonResponse.getJSONObject("error");
					if (!err.isNull("message")) {
						errors = new ApiError[1];
						errors[0] = new ApiError(this.statusCode + "",
								err.getString("message"));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					ApiLog.w("JSON response error: " + e.getMessage());
				}
			}
		}
	}

	public boolean isSuccess() {
		return success;
	}

	public ApiError[] getErrors() {
		return errors;
	}

	public JSONObject getJsonResponse() {
		return jsonResponse;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getRawResponse() {
		return rawResponse;
	}

}
