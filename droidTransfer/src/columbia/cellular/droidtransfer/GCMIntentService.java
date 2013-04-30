package columbia.cellular.droidtransfer;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import columbia.cellular.Utils.DLog;
import columbia.cellular.api.apicalls.GcmUpdate;
import columbia.cellular.api.entities.FtDroidActivity;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;



public class GCMIntentService extends GCMBaseIntentService {
	    
    private static final String TAG = "GCMIntentService";
    public static final String SENDER_ID = "178896580049";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        DLog.i( "Device registered: regId = " + registrationId);
        DLog.i("ID length:"+registrationId.length());
		SharedPreferences pref = getSharedPreferences(FtDroidActivity.PREFS_DEVICE, Activity.MODE_PRIVATE);
		Editor prefEditor = pref.edit();
		prefEditor.putString(FtDroidActivity.PREF_GCM_REGISTRATION_ID, registrationId);
		prefEditor.commit();
		if(LoginActivity.instance != null){
			LoginActivity.instance.registrationReceived(registrationId);
			DLog.i("Updating on the server...");
			if(LoginActivity.instance.isRegistered()){
				GcmUpdate updateGcm = new GcmUpdate(LoginActivity.instance);
				updateGcm.update(registrationId);
			}
		}
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        DLog.i("Device unregistered");
  //      displayMessage(context, getString(R.string.gcm_unregistered));
        if (GCMRegistrar.isRegisteredOnServer(context)) {
  //          ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            DLog.i("Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        String message = getString(R.string.gcm_message);
  //      displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
 //       displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
 //       displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
 //       displayMessage(context, getString(R.string.gcm_recoverable_error,
 //               errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    public static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_stat_gcm;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        //Notification notification = new Notification(icon, message, when);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        
        Intent notificationIntent = new Intent(context, MainActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }

}
