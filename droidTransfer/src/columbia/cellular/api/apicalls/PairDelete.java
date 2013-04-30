package columbia.cellular.api.apicalls;

import columbia.cellular.api.entities.Device;
import columbia.cellular.api.service.ApiParam;
import columbia.cellular.api.service.ApiRequestWrapper;
import columbia.cellular.api.service.ApiServerConnector;
import columbia.cellular.droidtransfer.FtDroidActivity;


public class PairDelete extends PairWith {

	public PairDelete(FtDroidActivity activity) {
		super(activity);
		// TODO Auto-generated constructor stub
	}

	public void deletePairingWith(Device device) {
		String nickname = device.getNickname();
		String email = device.getEmail();
		apiRequest = new ApiRequestWrapper(ApiServerConnector.API_URL_PAIR_DELETE);
		if (email == null && nickname == null) {
			throw new IllegalArgumentException(
					"Device must contain either e-mail or nickname");
		}
		
		if (email != null) {
			apiRequest.addParam(new ApiParam<String>("email", email,
					ApiParam.TYPE_EMAIL));
		}

		if (nickname != null) {
			apiRequest.addParam(new ApiParam<String>("nickname", nickname,
					ApiParam.TYPE_STRING));
		}

		apiRequest.setListener(new DefaultRequestListener());
		processAsync();
	}

}
