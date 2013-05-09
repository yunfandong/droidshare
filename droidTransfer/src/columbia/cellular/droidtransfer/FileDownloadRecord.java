package columbia.cellular.droidtransfer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;

public class FileDownloadRecord {
    private long totalLength = 0;
    private long totalDone = 0;
    protected static DroidApp application;
    protected String statusText;
    protected String destination;
    protected String remotePath;
    protected String deviceNickname;
    protected boolean started = false;
    protected boolean hasError = false;
    protected String baseFilename;
    protected NotificationManager mNotifyManager;
    protected NotificationCompat.Builder mBuilder;
    protected static int downloadNotificationID = 20000000;
    protected int notificationID;
    protected int percentDone = 0;
    protected boolean completed = false;
    public static PendingIntent intent;

    public FileDownloadRecord(String destinationFolder, String baseFilename, String path, String nickName) {
        this.destination = destinationFolder + System.getProperty("file.separator") + baseFilename;
        this.baseFilename = baseFilename;
        this.remotePath = path;
        deviceNickname = nickName;
    }

    public String getId() {
        return deviceNickname + ":" + remotePath + "|" + destination;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public long getTotalDone() {
        return totalDone;
    }

    public void setTotalDone(long totalDone) {
        this.totalDone = totalDone;
    }

    public static DroidApp getApplication() {
        return application;
    }

    public static void setApplication(DroidApp application) {
        FileDownloadRecord.application = application;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getDeviceNickname() {
        return deviceNickname;
    }

    public void setDeviceNickname(String deviceNickname) {
        this.deviceNickname = deviceNickname;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public String getBaseFilename() {
        return baseFilename;
    }

    public void setBaseFilename(String baseFilename) {
        this.baseFilename = baseFilename;
    }

    protected void initNotificationWith(NotificationManager manager, NotificationCompat.Builder builder) {
        mNotifyManager = manager;
        mBuilder = builder;
        mBuilder.setContentTitle(getBaseFilename()).setContentText(getStatusText()).setSmallIcon(R.drawable.ic_notify);
        notificationID = ++downloadNotificationID;
        doNotification(true);
    }

    public void updateProgress(long done, long total) {
        setTotalDone(done);
        setTotalLength(total);
        doNotification(false);
    }

    protected void doNotification(final boolean indeterminate) {
        int progressTmp = totalLength < 1 ? 0 : (int) (Math.floor((100 * totalDone) / totalLength));
        if (progressTmp <= percentDone && !indeterminate && !(completed || hasError)) {
            return;
        }
        percentDone = progressTmp;
        if (mNotifyManager != null && mBuilder != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(hasError){
                        mBuilder.setProgress(0, 0, false);
                    }else{
                        mBuilder.setProgress(100, percentDone, indeterminate);
                    }
                    // Displays the progress bar for the first time.
                    mBuilder.setContentTitle(getBaseFilename()).setContentText(
                            indeterminate ? getStatusText() : percentDone + "% " + getStatusText());
                    Notification note = mBuilder.build();
                    if(!isCompleted() && !isHasError()){
                        note.flags |= Notification.FLAG_ONGOING_EVENT;
                    }else{
                        note.flags = note.flags & (~Notification.FLAG_ONGOING_EVENT);
                        note.flags |= Notification.FLAG_AUTO_CANCEL;
                    }
                     
                    if(intent != null){
                        note.contentIntent = intent;
                    }
                    mNotifyManager.notify(notificationID, note);
                }
            }).start();
        }
    }

    public int getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(int notificationID) {
        this.notificationID = notificationID;
    }

    public int getPercentDone() {
        return percentDone;
    }

    public void setPercentDone(int percentDone) {
        this.percentDone = percentDone;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

}
