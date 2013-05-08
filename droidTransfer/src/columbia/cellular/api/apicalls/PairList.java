package columbia.cellular.api.apicalls;

import org.json.JSONException;
import org.json.JSONObject;

import columbia.cellular.api.entities.DeviceList;
import columbia.cellular.api.service.ApiLog;
import columbia.cellular.api.service.ApiParam;
import columbia.cellular.api.service.ApiResponse;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiServerConnector;
import columbia.cellular.droidtransfer.DroidApp;

public class PairList extends ApiCall {

	public PairList(DroidApp application) {
		super(application);
		// TODO Auto-generated constructor stub
	}

	public void getPairList(int pair_id, int limit, int offset) {
		apiRequest = new ApiRequestWrapper(ApiServerConnector.API_URL_PAIR_LIST);
		apiRequest
				.addParam(
						new ApiParam<String>("pair_id", "" + pair_id,
								ApiParam.TYPE_INT))
				.addParam(
						new ApiParam<String>("limit", "" + limit,
								ApiParam.TYPE_INT))
				.addParam(
						new ApiParam<String>("offset", "" + offset,
								ApiParam.TYPE_INT));
		apiRequest.setListener(new DefaultRequestListener());
		processAsync();
	}

	public void getPairList() {
		getPairList(0, 0, 0);
	}

	public void getPairList(int limit, int offset) {
		getPairList(0, limit, offset);
	}

	@Override
	public void responseReceived(ApiResponse apiResponse) {
		if (handler != null) {
			JSONObject responseJSON = apiResponse.getJsonResponse();
			try {
				DeviceList deviceList = new DeviceList(
						responseJSON.getJSONArray("pairs"));
				handler.entityReceived(deviceList);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				ApiLog.e("Could not create device List", e);
				handler.handleError(null, null);
			}
		}
	}

	@Override
	public void progressUpdated(long done, long total) {
		// TODO Auto-generated method stub

	}

}
