package columbia.cellular.api.apicalls;

import java.io.File;

import columbia.cellular.Utils.DLog;
import columbia.cellular.api.service.ApiParam;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiServerConnector;
import columbia.cellular.droidtransfer.DroidApp;
import columbia.cellular.droidtransfer.FileDownloadRecord;



public class DownloadFile extends ApiCall {

	private FileDownloadRecord downloadRecord;
	
	public DownloadFile(DroidApp application, FileDownloadRecord downloadRecord) {
		super(application);
		this.downloadRecord = downloadRecord;
	}

	public void startDownload(long fileID, File outFile){
		apiRequest = new ApiRequestWrapper(ApiServerConnector.API_URL_DOWNLOAD_FILE);
		apiRequest.setListener(new FileDownloadRequestListerner(outFile));
		apiRequest.addParam(new ApiParam<String>("file_id", fileID+"", ApiParam.TYPE_BIGINT));
		processAsync();
	}
	
	@Override
	public void responseReceived(ApiResponse apiResponse) {
		if(handler != null){
			handler.entityReceived(null);
		}
	}
	
	@Override
	public void errorReceived(ApiResponse apiResponse) {
		DLog.w("Error response received");
		super.errorReceived(apiResponse);
	}
	@Override
	public void progressUpdated(long done, long total) {
		// TODO Auto-generated method stub
		//ApiLog.i("Downloaded : "+done + " / "+ total);
		downloadRecord.updateProgress(done, total);
	}
	
	
}
