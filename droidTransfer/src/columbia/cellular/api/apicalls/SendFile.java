package columbia.cellular.api.apicalls;

import java.io.File;

import columbia.cellular.api.entities.FtDroidActivity;
import columbia.cellular.api.service.ApiLog;
import columbia.cellular.api.service.ApiParam;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.api.service.ApiServerConnector;


public class SendFile extends ApiCall {

	public SendFile(FtDroidActivity activity) {
		super(activity);
	}

	public void send(long in_reply_to, String path, File requestedFile, String errorMessage) {
		apiRequest = new ApiRequestWrapper(ApiServerConnector.API_URL_SEND_FILE);
		
		apiRequest.addParam(new ApiParam<String>("in_reply_to", in_reply_to+"", ApiParam.TYPE_BIGINT))
				.addParam(new ApiParam<String>("path", path, ApiParam.TYPE_STRING))
				;
		
		if(requestedFile != null){
			apiRequest.addParam(new ApiParam<File>("file", requestedFile, ApiParam.TYPE_FILE));
			apiRequest.setMultipart(true);
		}
		
		if(errorMessage != null){
			apiRequest.addParam(new ApiParam<String>("error", errorMessage));
		}
		
		processAsync();
	}
	
	public void send(long in_reply_to, String path, File requestedFile){
		send(in_reply_to, path, requestedFile, null);
	}
	
	public void send(long in_reply_to, String path, String errString){
		send(in_reply_to, path, null, errString);
	}
	
	
	@Override
	public void responseReceived(ApiResponse apiResponse) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void progressUpdated(long done, long total) {
		// TODO Auto-generated method stub
		super.progressUpdated(done, total);
		ApiLog.i("Uploaded "+done +" of "+total);
	}

}
