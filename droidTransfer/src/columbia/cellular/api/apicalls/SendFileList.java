package columbia.cellular.api.apicalls;

import org.json.JSONObject;

import columbia.cellular.api.service.ApiParam;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.api.service.ApiServerConnector;
import columbia.cellular.droidtransfer.FtDroidActivity;
public class SendFileList extends ApiCall{

	
	public SendFileList(FtDroidActivity activity) {
		super(activity);
	}
	
	public void send(String path, JSONObject fileList, long in_reply_to, String errorMessage){
		apiRequest = new ApiRequestWrapper(ApiServerConnector.API_URL_SEND_FILE_LIST);
		String fileListJson = fileList == null ? "{}" : fileList.toString();
		apiRequest.setListener(new DefaultRequestListener());
		apiRequest.addParam(new ApiParam<String>("path", path))
					.addParam(new ApiParam<String>("in_reply_to", ""+in_reply_to, ApiParam.TYPE_BIGINT))
					.addParam(new ApiParam<String>("file_list", fileListJson, ApiParam.TYPE_JSON));
		
		if(errorMessage != null){
			apiRequest.addParam(new ApiParam<String>("error", errorMessage));
		}
		
		processAsync();
	}
	
	public void send(String path, JSONObject fileList, long in_reply_to){
		send(path, fileList, in_reply_to, null);
	}
	
	public void send(String path, long in_reply_to, String errorMessage){
		send(path, null, in_reply_to, errorMessage);
	}	

	@Override
	public void responseReceived(ApiResponse apiResponse) {
		// TODO Auto-generated method stub
		_processMessageResponse(apiResponse);
	}

}
