package columbia.cellular.api.apicalls;


import columbia.cellular.api.entities.Device;
import columbia.cellular.api.service.ApiParam;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.api.service.ApiServerConnector;
import columbia.cellular.droidtransfer.DroidApp;



public class GetFileList extends ApiCall {
	
	
	public GetFileList(DroidApp application) {
		super(application);
		// TODO Auto-generated constructor stub
	}
	protected String apiEndPoint = ApiServerConnector.API_URL_GET_FILE_LIST;
	
	
	public void get(Device device, String path, boolean forceReload){
		String nickname = device.getNickname();
		int pairId = device.getId();
		
		if(nickname == null && pairId <= 0){
			throw new IllegalArgumentException("Device nickname and id empty");
		}
		
		apiRequest = new ApiRequestWrapper(apiEndPoint);
		apiRequest.setListener(new DefaultRequestListener());
		if(nickname != null){
			apiRequest.addParam(new ApiParam<String>("nickname", nickname));
		}
		
		if(pairId > 0){
			apiRequest.addParam(new ApiParam<String>("pair_id", pairId+""));
		}
		
		apiRequest.addParam(new ApiParam<String>("path", path));
		apiRequest.addParam(new ApiParam<String>("reload", forceReload ? "1": "0", ApiParam.TYPE_BOOL));
		
		processAsync();
	}
	
	public void get(Device device, String path){
		get(device, path, false);
	}
	@Override
	public void responseReceived(ApiResponse apiResponse) {
		_processMessageResponse(apiResponse);
	}


}
