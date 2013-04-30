package columbia.cellular.api.apicalls;

import columbia.cellular.api.entities.FtDroidActivity;
import columbia.cellular.api.service.ApiResponse;



public class GetFileList extends ApiCall {
	
	
	public GetFileList(FtDroidActivity activity) {
		super(activity);
		// TODO Auto-generated constructor stub
	}
	
	public void get(String path, String nickname, int pairId){
		
	}
	
	

	@Override
	public void responseReceived(ApiResponse apiResponse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void progressUpdated(long done, long total) {
		// TODO Auto-generated method stub
		
	}

}
