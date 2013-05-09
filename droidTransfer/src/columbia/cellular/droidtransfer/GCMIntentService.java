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
import columbia.cellular.api.apicalls.ActivityApiResponseHandler;
import columbia.cellular.api.apicalls.GcmUpdate;
import columbia.cellular.api.apicalls.SendFile;
import columbia.cellular.api.apicalls.SendFileList;
import columbia.cellular.api.entities.DeviceMessage;
import columbia.cellular.api.entities.DeviceMessageList;
import columbia.cellular.api.service.ApiEntity;
import columbia.cellular.api.service.ApiError;
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
	protected DroidApp application;

	private Context applicationContext;

	public GCMIntentService() {
		super(SENDER_ID);
		application = (DroidApp) getApplication();
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		DLog.i("Device registered: regId = " + registrationId);
		DLog.i("ID length:" + registrationId.length());
		SharedPreferences pref = getSharedPreferences(DroidApp.PREFS_DEVICE,
				Activity.MODE_PRIVATE);
		Editor prefEditor = pref.edit();
		prefEditor.putString(DroidApp.PREF_GCM_REGISTRATION_ID, registrationId);
		prefEditor.commit();
		if (LoginActivity.instance != null) {
			LoginActivity.instance.registrationReceived(registrationId);
			DLog.i("Updating on the server...");
			if (((DroidApp) getApplication()).isRegistered()) {
				GcmUpdate updateGcm = new GcmUpdate((DroidApp) getApplication());
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
		application = (DroidApp) getApplication();
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

		// DLog.i("Message Received Object" + message);
		// generateNotification(context, msgNotify);
	}

	protected void _handleMessage(DeviceMessage message) {
		String messageContent = message.getContent();
		if (messageContent == null) {
			DLog.i("Message Content is empty");
			return;
		}

		DLog.i("Message Received is : " + messageContent);
		if (messageContent.equals(MSG_FILE_LIST_REQUEST)) {
			_handleSendFileList(message);
		} else if (messageContent.equals(MSG_FILE_REQUEST)) {
			_handleSendFile(message);
		} else if (messageContent.equals(MSG_PAIR_REJECTED)) {
			_handlePairingRejected(message);
		} else if (messageContent.equals(MSG_PAIR_ACCEPTED)) {
			_handlePairingAccepted(message);
		} else if (messageContent.equals(MSG_FILE)) {
			_handleFileDownload(message);
		} else if (messageContent.equals(MSG_FILE_ERROR)) {
			_handleFileError(message);
		} else if (messageContent.equals(MSG_PAIR_REQUEST)) {
			_handlePairRequest(message);
		} else if (messageContent.equals(MSG_PAIR_DELETED)) {
			_handlePairDeleted(message);
		}

	}

	private void _handleFileError(DeviceMessage message) {
		DLog.i("New Message: " + message.getMessageID() + " is reply to : "
				+ message.getInReplyTo() + " with error: "
				+ message.getMetaErrorMessage() + " Path: "
				+ message.getMetaFilePath());

		application.fileDownloadHasError(message.getInReplyTo(),
				message.getMetaErrorMessage());
	}

	private void _handleFileDownload(DeviceMessage message) {
		DLog.i("New Message: " + message.getMessageID() + " is reply to : "
				+ message.getInReplyTo() + " For file ID: "
				+ message.getMetaFileID() + " Path: "
				+ message.getMetaFilePath());

		application.startFileDownload(message.getInReplyTo(),
				message.getMetaFileID());
	}

	private void _handlePairDeleted(DeviceMessage message) {
		String notifyMessage = String.format(
				"You are no longer paired with %s (%s).", message.getSender()
						.getNickname(), message.getSender().getEmail());

		Intent notifyIntent = new Intent(applicationContext, MainActivity.class);
		notifyIntent.putExtra(MainActivity.EXTRA_PAIR_LIST_REFRESH, true);

		generateNotification(applicationContext, notifyMessage,
				"Pairing Deleted", notifyIntent);
	}

	private void _handlePairRequest(DeviceMessage message) {
		String notifyMessage = String.format("%s (%s) wants to pair with you.",
				message.getSender().getNickname(), message.getSender()
						.getEmail());

		Intent notifyIntent = new Intent(applicationContext, MainActivity.class);
		notifyIntent.putExtra(MainActivity.EXTRA_PAIR_MESSAGE, notifyMessage);
		notifyIntent.putExtra(MainActivity.EXTRA_PAIR_MESSAGE_ID,
				"" + message.getMessageID());

		generateNotification(applicationContext, notifyMessage,
				"Pairing Request", notifyIntent);
	}

	private void _handleSendFileList(DeviceMessage message) {
		String path = message.getMetaFilePath();
		if (path == null) {
			DLog.i("File List path is null");
			return;
		}

		DLog.i("Sending file list");
		SendFileList sender = new SendFileList(application);
		String rootPath = application.getSetting(DroidApp.PREF_ROOT_PATH, "");
		FileListGen fileListGen = new FileListGen(rootPath, path);
		String error = null;
		JSONObject fileListJson = null;
		try {
			fileListJson = fileListGen.getListJSON();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			error = e.getMessage();
			DLog.e("JSON error: " + e);
		}

		error = fileListGen.getErrorMessage();
		sender.setResponseHandler(new ActivityApiResponseHandler() {
			public void handleError(ApiError[] errors, ApiEntity entity) {
				if (errors != null) {
					for (ApiError error : errors) {
						DLog.w("SendFile: " + error.getErrorCode() + ": "
								+ error.getErrorMessage());
					}
				}
			}

			@Override
			public void entityReceived(ApiEntity entity) {
				DLog.i("File list sent: "+entity);
			}
		});

		sender.send(path, fileListJson, message.getMessageID(), error);
	}

	private void _handleSendFile(DeviceMessage message) {
		String path = message.getMetaFilePath();
		DLog.i("Sending file..." + path);
		if (path == null) {
			DLog.e("Cannot send file to unknown path");
			return;
		}

		String rootPath = ((DroidApp) getApplication()).getSetting(
				DroidApp.PREF_ROOT_PATH, "");
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

		SendFile fileSender = new SendFile(application);
		// @TODO create the upload progress bar
		// DLog.i("About to send: "
		// + (fileSender == null ? error : fileToSend.getAbsolutePath()));
		fileSender.setResponseHandler(new ActivityApiResponseHandler() {
			@Override
			public void handleError(ApiError[] errors, ApiEntity entity) {
				if (errors != null) {
					for (ApiError error : errors) {
						DLog.w("SendFile: " + error.getErrorCode() + ": "
								+ error.getErrorMessage());
					}
				}
			}

			@Override
			public void entityReceived(ApiEntity entity) {
				DLog.i("File sent successfully");
			}
		});

		fileSender.send(message.getMessageID(), path, fileToSend, error);
	}

	private void _handlePairingRejected(DeviceMessage message) {
		String notifyMessage = message.getSender().getNickname()
				+ " rejected your pairing request.";
		generateNotification(applicationContext, notifyMessage,
				"Pairing Rejected", null);
	}

	private void _handlePairingAccepted(DeviceMessage message) {
		// notify
		String notifyMessage = String.format(
				"%s (%s) accepted your pairing request.", message.getSender()
						.getNickname(), message.getSender().getEmail());

		Intent notifyIntent = new Intent(applicationContext, MainActivity.class);
		notifyIntent.putExtra(MainActivity.EXTRA_PAIR_LIST_REFRESH, true);

		generateNotification(applicationContext, notifyMessage,
				"Pairing Accepted", notifyIntent);

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
		return super.onRecoverableError(context, errorId);
	}

	public static void generateNotification(Context context, String message) {
		generateNotification(context, message, null, null);
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	@SuppressWarnings("deprecation")
	public static void generateNotification(Context context, String message,
			String title, Intent notificationIntent) {

		if (notificationIntent == null) {
			notificationIntent = new Intent(context, MainActivity.class);
		}

		// DLog.i("Extras to send: "+notificationIntent.getExtras());
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification = new Notification.Builder(context)
				.setContentTitle(
						title == null ? context.getString(R.string.app_name)
								: title).setContentText(message)
				.setSmallIcon(R.drawable.ic_launcher_new).setContentIntent(intent)
				.setWhen(System.currentTimeMillis()).getNotification();
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		// notification.flags |= Notification.FLAG_INSISTENT;
		notification.flags |= Notification.DEFAULT_SOUND;
		notification.flags |= Notification.DEFAULT_LIGHTS;

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0, notification);
	}

}
