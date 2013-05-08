package columbia.cellular.api.apicalls;

import columbia.cellular.api.service.ApiEntity;
import columbia.cellular.api.service.ApiError;

public interface ActivityApiResponseHandler {
	public abstract void entityReceived(ApiEntity entity);
	public abstract void handleError(ApiError[] errors, ApiEntity entity);
}
