package columbia.cellular.api.apicalls;

import columbia.cellular.api.service.ApiRequestHandler;

public class DefaultRequestListener extends ApiRequestHandler {
	protected ApiCallAsyncTask requestTask;

	public ApiCallAsyncTask getRequestTask() {
		return requestTask;
	}

	public void setRequestTask(ApiCallAsyncTask requestTask) {
		this.requestTask = requestTask;
	}
	
	@Override
	public void uploadProgress(long written, long total) {
		// TODO Auto-generated method stub
		super.uploadProgress(written, total);
		if(requestTask != null){
		}
	}
}
