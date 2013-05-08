package columbia.cellular.api.apicalls;

import columbia.cellular.api.service.ApiServerConnector;
import columbia.cellular.droidtransfer.DroidApp;

public class GetFile extends GetFileList {
	
	public GetFile(DroidApp application) {
		super(application);
		apiEndPoint = ApiServerConnector.API_URL_GET_FILE;
	}
	
	
}
