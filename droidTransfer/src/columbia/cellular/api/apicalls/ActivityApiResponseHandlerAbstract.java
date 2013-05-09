package columbia.cellular.api.apicalls;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import columbia.cellular.api.service.ApiEntity;
import columbia.cellular.api.service.ApiError;
import columbia.cellular.droidtransfer.R;

public abstract class ActivityApiResponseHandlerAbstract implements ActivityApiResponseHandler {
	protected Activity activity;
	
	public ActivityApiResponseHandlerAbstract(Activity activity) {
		this.activity = activity;
	}
	
	protected void _handleErrorsGeneric(ApiError[] errors, ApiEntity entity){
		if (errors != null && activity != null && !activity.isFinishing()) {
			String errorMessages = "";
			for (ApiError e : errors) {
				errorMessages += "\n" + e.getErrorMessage();
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setMessage(errorMessages).setTitle(R.string.error_dialog_title);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

						}
					});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}
}
