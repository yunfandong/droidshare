package columbia.cellular.api.apicalls;

import columbia.cellular.api.entities.FtDroidActivity;
import columbia.cellular.api.service.ApiServerConnector;

public class GetFile extends GetFileList {
	
	public GetFile(FtDroidActivity activity) {
		super(activity);
		apiEndPoint = ApiServerConnector.API_URL_GET_FILE;
	}
	
	
}
