package columbia.cellular.api.apicalls;


import columbia.cellular.api.service.ApiLog;
import columbia.cellular.api.service.ApiRequestHandler;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.api.service.ApiServerConnector;
import android.os.AsyncTask;

public class ApiCallAsyncTask extends AsyncTask <ApiRequestWrapper, ApiRequestHandler, ApiResponse> {
	
	private ApiCall apiCall;
	private DefaultRequestListener requestHandler;
	public ApiCallAsyncTask(ApiCall aCall) {
		apiCall = aCall;
	}

	@Override
	protected ApiResponse doInBackground(ApiRequestWrapper... params) {
		if(params.length < 1){
			ApiLog.e("No API request set");
			return null;
		}
		ApiRequestWrapper request = params[0];
		
		//inform the request handler of whom to receive progrees
		if(request.getListener() != null){
			requestHandler = (DefaultRequestListener) request.getListener();
			requestHandler.setRequestTask(this);
		}
		return ApiServerConnector.getInstance().makeHttpRequest(request);
	}
	
	public void publishProgressExt(ApiRequestHandler reqHandler){
		publishProgress(reqHandler);
	}
	
	public void onProgressUpdate (ApiRequestHandler... handlerParams){
		//inform the ApiCall on UI thread of upload progress
		if(handlerParams.length > 0){
			ApiRequestHandler handler = handlerParams[0];
			apiCall.progressUpdated(handler.getTotalDone(), handler.getTotalLength());			
		}
	}

	@Override
	protected void onPostExecute(ApiResponse response) {
		if(response != null){
			if(response.isSuccess()){
				apiCall.responseReceived(response);
			}else{
				apiCall.errorReceived(response);
			}
		}else{
			if(requestHandler != null){
				apiCall.emptyResponse(requestHandler.getLastException());
			}
		}
	}
}
