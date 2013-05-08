package columbia.cellular.api.apicalls;

import java.io.File;

import columbia.cellular.api.service.ApiLog;
import columbia.cellular.api.service.ApiParam;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiServerConnector;
import columbia.cellular.droidtransfer.DroidApp;



public class DownloadFile extends ApiCall {


	
	public DownloadFile(DroidApp application) {
		super(application);
		// TODO Auto-generated constructor stub
	}

	public void startDownload(long fileID, File outFile){
		apiRequest = new ApiRequestWrapper(ApiServerConnector.API_URL_DOWNLOAD_FILE);
		apiRequest.setListener(new FileDownloadRequestListerner(outFile));
		apiRequest.addParam(new ApiParam<String>("file_id", fileID+"", ApiParam.TYPE_BIGINT));
		processAsync();
	}
	
	@Override
	public void responseReceived(ApiResponse apiResponse) {
		// TODO Auto-generated method stub
		if(handler != null){
			handler.entityReceived(null);
		}
	}
	
	@Override
	public void progressUpdated(long done, long total) {
		// TODO Auto-generated method stub
		ApiLog.i("Downloaded: "+done + " of "+ total);
	}
	
	
}
