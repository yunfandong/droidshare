package columbia.cellular.api.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;

public interface ApiRequestListener extends ResponseHandler<ApiResponse>{
	public void handleException(Exception e);
	public ApiResponse handleResponse(HttpResponse response);
	public void requestStarting(ApiRequestWrapper request, HttpUriRequest httpUriRequest);
	public void updateProgress(long written, long total);
}
