package columbia.cellular.droidtransfer;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
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
import columbia.cellular.api.apicalls.SendFile;
import columbia.cellular.api.apicalls.SendFileList;
import columbia.cellular.api.entities.DeviceMessage;
import columbia.cellular.api.entities.DeviceMessageList;
import columbia.cellular.api.service.ApiLog;
import columbia.cellular.file.FileListGen;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {
	public final static String MSG_PAIR_REQUEST = "pairing-request";
	public final static String MSG_PAIR_ACCEPTED = "pairing-accepted";
	public final static String MSG_PAIR_REJECTED = "pairing-rejected";
	public final static String MSG_PAIR_DELETED = "pairing-deleted";
	public final static String MSG_FILE_LIST_REQUEST = "file-list-request";
	public final static String MSG_FILE_LIST_ERROR = "file-list-error";
	public final static String MSG_FILE_LIST = "file-list";
	public final static String MSG_FILE_REQUEST = "file-request";
	public final static String MSG_FILE = "file";
	public final static String MSG_FILE_ERROR = "file-error";

	int count = 0;
	private static final String TAG = "GCMIntentService";
	public static final String SENDER_ID = "178896580049";

	private Context applicationContext;

	public GCMIntentService() {
		super(SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		DLog.i("Device registered: regId = " + registrationId);
		DLog.i("ID length:" + registrationId.length());
		SharedPreferences pref = getSharedPreferences(
				FtDroidActivity.PREFS_DEVICE, Activity.MODE_PRIVATE);
		Editor prefEditor = pref.edit();
		prefEditor.putString(FtDroidActivity.PREF_GCM_REGISTRATION_ID,
				registrationId);
		prefEditor.commit();
		if (LoginActivity.instance != null) {
			LoginActivity.instance.registrationReceived(registrationId);
			DLog.i("Updating on the server...");
			if (LoginActivity.instance.isRegistered()) {
				GcmUpdate updateGcm = new GcmUpdate(LoginActivity.instance);
				updateGcm.update(registrationId);
			}
		}
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		DLog.i("Device unregistered");
		// displayMessage(context, getString(R.string.gcm_unregistered));
		if (GCMRegistrar.isRegisteredOnServer(context)) {
			// ServerUtilities.unregister(context, registrationId);
		} else {
			// This callback results from the call to unregister made on
			// ServerUtilities when the registration to the server failed.
			DLog.i("Ignoring unregister callback");
		}
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i(TAG, "Received message");
		applicationContext = context;
		String msgNotify = getString(R.string.gcm_message);
		String message = intent.getStringExtra("messages");

		try {
			JSONArray messagesJson = new JSONArray(message);
			DeviceMessageList messages = new DeviceMessageList(messagesJson, 0,
					0);
			for (DeviceMessage devMsg : messages.getMessages()) {
				_handleMessage(devMsg);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			ApiLog.e("Cannot interprete received message...", e);
		}

		//DLog.i("Message Received Object" + message);
		//generateNotification(context, msgNotify);
	}

	protected void _handleMessage(DeviceMessage message) {
		String messageContent = message.getContent();
		if (messageContent == null) {
			DLog.i("Message Content is empty");
			return;
		}

		DLog.i("Message Content is : "+messageContent);
		if (messageContent.equals(MSG_FILE_LIST_REQUEST)) {
			_handleSendFileList(message);
		} else if (messageContent.equals(MSG_FILE_REQUEST)) {
			_handleSendFile(message);
		} else if (messageContent.equals(MSG_PAIR_REJECTED)) {
			_handlePairingRejected(message);
		} else if (messageContent.equals(MSG_PAIR_ACCEPTED)) {
			_handlePairingAccepted(message);
		} else if (messageContent.equals(MSG_PAIR_REQUEST)) {
			_handlePairRequest(message);
		} else if (messageContent.equals(MSG_PAIR_DELETED)) {
			_handlePairDeleted(message);
		}

	}

	private void _handlePairDeleted(DeviceMessage message) {
		// refresh the stuff
	}

	@SuppressWarnings("deprecation")
	private void _handlePairRequest(DeviceMessage message) {
		String notifyMessage = String.format("%s (%s) wants to pair with you.",
				message.getSender().getNickname(), message.getSender()
						.getEmail());
		generateNotification(applicationContext, notifyMessage);
	}

	private void _handleSendFileList(DeviceMessage message) {
		String path = message.getMetaFilePath();
		if (path == null) {
			DLog.i("File List path is null");
			return;
		}

		DLog.i("Handling send file list");
		if (MainActivity.getInstance() != null) {
			DLog.i("Found a main activity");
			SendFileList sender = new SendFileList(MainActivity.getInstance());
			String rootPath = MainActivity.getInstance().getSetting(
					FtDroidActivity.PREF_ROOT_PATH, "");
			FileListGen fileListGen = new FileListGen(rootPath, path);
			String error = null;
			JSONObject fileListJson = null;
			try {
				fileListJson = fileListGen.getListJSON();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				error = e.getMessage();
			}

			error = fileListGen.getErrorMessage();
			sender.setReturnEvents(false);
			sender.send(path, fileListJson, message.getMessageID(), error);
		}
		
	}

	private void _handleSendFile(DeviceMessage message) {
		String path = message.getMetaFilePath();
		if (path == null) {
			DLog.e("Cannot sent file to unknown path");
			return;
		}

		if (MainActivity.getInstance() != null) {
			String rootPath = MainActivity.getInstance().getSetting(
					FtDroidActivity.PREF_ROOT_PATH, "");
			String fullPath = rootPath + path;
			String error = null;
			File fileToSend = new File(fullPath);
			if (!fileToSend.exists()) {
				error = "File was not found";
			}

			if (fileToSend.isDirectory()) {
				error = "Path is not that of a file";
			}

			if (!fileToSend.canRead()) {
				error = "Read access denied";
			}

			if (error != null) {
				fileToSend = null;
			}

			SendFile fileSender = new SendFile(MainActivity.getInstance());
			fileSender.setReturnEvents(false);
			// @TODO create the upload progress bar
			fileSender.send(message.getMessageID(), path, fileToSend, error);
		}
	}

	private void _handlePairingRejected(DeviceMessage message) {
		// notify
	}

	private void _handlePairingAccepted(DeviceMessage message) {
		// notify
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.i(TAG, "Received deleted messages notification");
		String message = getString(R.string.gcm_deleted, total);
		// displayMessage(context, message);
		// notifies user
		generateNotification(context, message);
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
		// displayMessage(context, getString(R.string.gcm_error, errorId));
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		Log.i(TAG, "Received recoverable error: " + errorId);
		// displayMessage(context, getString(R.string.gcm_recoverable_error,
		// errorId));
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	public static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_stat_gcm;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// Notification notification = new Notification(icon, message, when);
		Notification notification = new Notification(icon, message, when);
		String title = context.getString(R.string.app_name);

		Intent notificationIntent = new Intent(context, MainActivity.class);
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, notification);
	}

}
