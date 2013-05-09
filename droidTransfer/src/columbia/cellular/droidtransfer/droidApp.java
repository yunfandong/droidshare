package columbia.cellular.droidtransfer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import columbia.cellular.Utils.DLog;
import columbia.cellular.api.apicalls.ActivityApiResponseHandler;
import columbia.cellular.api.apicalls.DownloadFile;
import columbia.cellular.api.entities.Device;
import columbia.cellular.api.service.ApiEntity;
import columbia.cellular.api.service.ApiError;
import columbia.cellular.file.OpenFileDialog;

public class DroidApp extends Application {
    public boolean isServiceRegistered = false;
    public boolean isNameSet = false;

    public static final String PREF_DEVICE_TOKEN = "device_token";
    public static final String PREF_DEVICE_NICKNAME = "nickname";
    public static final String PREF_DEVICE_ID = "id";
    public static final String PREF_GCM_REGISTRATION_ID = "gcm_reg_id";
    public static final String PREF_EMAIL_ADDRESS = "email";
    public static final String PREF_ROOT_PATH = "rootPath";
    public static final String PREFS_DEVICE = "devicePreferences";
    public static final String PREFS_SETTINGS = "deviceSettings";
    private Device device;
    private Map<String, Integer> images = null;
    private HashMap<String, Object> registry;
    private HashMap<Long, FileDownloadRecord> filedownloadQueue;

    /**
     * Google API project id registered to use GCM.
     */
    public static final String SENDER_ID = "178896580049";

    private Handler mainHandler;
    public String ACTION_SEND_REQUEST = "action_send_request";
    public static int RESPONSE_MESSAGE = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        registry = new HashMap<String, Object>();
        filedownloadQueue = new HashMap<Long, FileDownloadRecord>();
        DLog.i("Application Started");
        createDefaultSettings();
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void showToastLong(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public boolean getServiceState() {
        return isServiceRegistered;
    }

    public void setServiceState(boolean s) {
        this.isServiceRegistered = s;
    }

    public void SetNameFlag(boolean b) {
        this.isNameSet = b;
    }

    public boolean whetherNameSet() {
        return this.isNameSet;
    }

    public void setMainHandler(Handler handler) {
        this.mainHandler = handler;
    }

    public void sendMessageToHandler(Message msg) {
        if (mainHandler != null) {
            mainHandler.sendMessage(msg);
        }
    }

    public void writeAnything(String any) {
        Log.i("Anything", "Anything " + any);
    }

    public void saveToRegistry(String key, Object object) {
        registry.put(key, object);
    }

    public Object getFromRegistry(String key, Object defaultVal) {
        if (registry.containsKey(key)) {
            return registry.get(key);
        }
        return defaultVal;
    }

    public Object getFromRegistry(String key) {
        return getFromRegistry(key, null);
    }

    public boolean isRegistered() {
        String deviceNickname = deviceProperty(PREF_DEVICE_NICKNAME, "");
        String deviceToken = deviceProperty(PREF_DEVICE_TOKEN, "");
        return deviceNickname.length() > 0 && deviceToken.length() > 0;
    }

    protected String getPreference(String category, String name, String defaultVal) {
        SharedPreferences pref = getSharedPreferences(category, Activity.MODE_PRIVATE);
        return pref.getString(name, defaultVal);
    }

    protected String getPreference(String category, String name) {
        return getPreference(category, name, "");
    }

    protected void savePreference(String category, String name, String value) {
        SharedPreferences pref = getSharedPreferences(category, Activity.MODE_PRIVATE);
        Editor prefEditor = pref.edit();
        prefEditor.putString(name, value);
        prefEditor.commit();
    }

    public String getSetting(String name, String defaultVal) {
        return getPreference(PREFS_SETTINGS, name, defaultVal);
    }

    public void saveSetting(String name, String value) {
        savePreference(PREFS_SETTINGS, name, value);
    }

    public String deviceProperty(String name, String defaultVal) {
        return getPreference(PREFS_DEVICE, name, defaultVal);
    }

    public void setDeviceProperty(String name, String value) {
        savePreference(PREFS_DEVICE, name, value);
    }

    public Device getRegisteredDevice() {
        if (!isRegistered()) {
            return null;
        }

        if (device == null) {
            device = new Device(deviceProperty(PREF_DEVICE_NICKNAME, ""), deviceProperty(PREF_EMAIL_ADDRESS, ""), null);
            device.setGcmAppID(deviceProperty(PREF_GCM_REGISTRATION_ID, ""));
            device.setId(Integer.parseInt(deviceProperty(PREF_DEVICE_ID, "0")));
            device.setToken(deviceProperty(PREF_DEVICE_TOKEN, null));
        }

        return device;
    }

    protected void createDefaultSettings() {
        String rootPath = getSetting(PREF_ROOT_PATH, "");
        if (rootPath.length() < 1) {
            rootPath = Environment.getExternalStorageDirectory().getPath();
            if (rootPath.length() < 1) {
                rootPath = Environment.getRootDirectory().getPath();
            }
            saveSetting(PREF_ROOT_PATH, Environment.getExternalStorageDirectory().getPath());
        }
    }

    public Map<String, Integer> getImageIds() {
        if (images == null) {
            images = new HashMap<String, Integer>();
            // file type adapter
            images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root); // root
            images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up); // back
            images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder); // folder
            images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
            images.put("music", R.drawable.filedialog_music);
            images.put("film", R.drawable.filedialog_film);
            images.put("pdf", R.drawable.filedialog_pdf);
            images.put("ppt", R.drawable.filedialog_ppt);
            images.put("db", R.drawable.filedialog_db);
            images.put("zip", R.drawable.filedialog_zip);
            images.put("txt", R.drawable.filedialog_txt);
            images.put("xls", R.drawable.filedialog_xls);
            images.put("java", R.drawable.filedialog_java);
            images.put("html", R.drawable.filedialog_html);
            images.put("code", R.drawable.filedialog_code);
            images.put("picture", R.drawable.filedialog_picture);
            images.put("application", R.drawable.filedialog_application);
            images.put("other", R.drawable.filedialog_file);
        }
        return images;

    }

    public void queueFileDownload(long messageID, FileDownloadRecord fileDownloadreq) {
        if (!filedownloadQueue.containsKey(messageID)) {
            fileDownloadreq.setStatusText(getString(R.string.filedownload_waiting));
            filedownloadQueue.put(messageID, fileDownloadreq);
            showToastLong(String.format(getString(R.string.filedownload_queued), fileDownloadreq.getRemotePath()));
            doProgressNotification(fileDownloadreq, messageID);
        }
    }

    public void fileDownloadHasError(long messageId, String error) {
        if (filedownloadQueue.containsKey(messageId)) {
            filedownloadQueue.get(messageId).setHasError(true);
            filedownloadQueue.get(messageId).setStatusText("Error: " + error);
        } else {
            DLog.i("Download with Message ID: " + messageId + " was not found");
        }
    }

    public void startFileDownload(long messageID, long fileId) {
        if (!filedownloadQueue.containsKey(messageID)) {
            DLog.w("File download with ID: " + messageID + " was not found");
            return;
        }

        final FileDownloadRecord record = filedownloadQueue.get(messageID);

        DownloadFile fileDownloader = new DownloadFile(this, record);
        fileDownloader.setResponseHandler(new ActivityApiResponseHandler() {
            public void handleError(ApiError[] errors, ApiEntity entity) {
                if (errors != null && errors.length > 0) {
                    String errorMessage = "";
                    for (ApiError err : errors) {
                        errorMessage += err.getErrorMessage() + "\n";
                    }
                    record.setStatusText("Error: " + errorMessage.trim());
                    DLog.w("Errors occured :" + errorMessage);
                } else {
                    record.setStatusText("Could not save file.");
                    DLog.w("Download failed and no error received");
                }
                record.setHasError(true);
                record.setCompleted(true);
                record.doNotification(true);
            }

            @Override
            public void entityReceived(ApiEntity entity) {
                record.setCompleted(true);
                record.setStatusText(getString(R.string.filedownload_completed));
                record.doNotification(false);
                showToast(record.getBaseFilename() + " successfully downloaded!");
            }
        });

        File outFile = new File(record.getDestination());
        record.setStarted(true);
        record.setStatusText(String.format(getString(R.string.filedownload_starting), record.getDestination()));

        fileDownloader.startDownload(fileId, outFile);
    }

    protected void doProgressNotification(final FileDownloadRecord record, final long messageId) {
        if (FileDownloadRecord.intent == null) {
            Intent notificationIntent = new Intent(this, FileDownloadActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            FileDownloadRecord.intent = PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationManager mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        record.initNotificationWith(mNotifyManager, mBuilder);
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public HashMap<Long, FileDownloadRecord> getFiledownloadQueue() {
        return filedownloadQueue;
    }

    public void setFiledownloadQueue(HashMap<Long, FileDownloadRecord> filedownloadQueue) {
        this.filedownloadQueue = filedownloadQueue;
    }

}
