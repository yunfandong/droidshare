package columbia.cellular.droidtransfer;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import columbia.cellular.Utils.CommonUtils;
import columbia.cellular.Utils.DLog;
import columbia.cellular.Utils.RelativeDate;
import columbia.cellular.api.apicalls.ActivityApiResponseHandlerAbstract;
import columbia.cellular.api.apicalls.GetFile;
import columbia.cellular.api.apicalls.GetFileList;
import columbia.cellular.api.apicalls.Messages;
import columbia.cellular.api.entities.Device;
import columbia.cellular.api.entities.DeviceMessage;
import columbia.cellular.api.entities.DeviceMessageList;
import columbia.cellular.api.service.ApiEntity;
import columbia.cellular.api.service.ApiError;
import columbia.cellular.file.CallbackBundle;
import columbia.cellular.file.OpenFileDialog;
import columbia.cellular.file.SelectRootDialog;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class FileListActivity extends ListActivity {

    protected View fileListLoadingView;
    protected TextView loadingMessage;
    protected TextView folderEmptyView;
    protected View fileListView;
    protected JSONArray listOfFiles;
    protected DroidApp application;
    public final int WAIT_SLEEP_INTERVAL = 2000;
    protected boolean isFinished = false;
    /**
     * Wait timeout in milliseconds
     */
    protected final long WAIT_TIMEOUT = 60000;
    protected String currentNickname = "";
    protected String currentPath = "";

    public static final String EXTRA_NICKNAME = "nickname";
    public static final String EXTRA_PATH = "path";
    protected static String lastSaveLocation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_file_list);
        currentNickname = getIntent().getStringExtra(EXTRA_NICKNAME);
        currentPath = getIntent().getStringExtra(EXTRA_PATH);
        // Show the Up button in the action bar.
        initViews();
        application = (DroidApp) getApplication();
        if (lastSaveLocation.length() < 1) {
            lastSaveLocation = application.getSetting(DroidApp.PREF_ROOT_PATH, Environment
                    .getExternalStorageDirectory().getAbsolutePath());
        }
        // setupActionBar();
        DLog.i("Current nickname: " + currentNickname + " Path: " + currentPath);

        if (currentNickname == null || currentPath == null) {
            finish();
        } else {
            loadFileList();
        }
    }

    private String getFileListRegistryKey(String nickname, String path) {
        return "fileList." + nickname + "." + path;
    }

    protected void loadFileList() {
        final String registryKey = getFileListRegistryKey(currentNickname, currentPath);
        if (application.getFromRegistry(registryKey) != null) {
            listOfFiles = (JSONArray) application.getFromRegistry(registryKey);
            updateFileListView();
            return;
        }

        loadingMessage.setText(getString(R.string.loading_filelist) + "(" + currentNickname + ":" + currentPath + ")");
        showProgress(true);
        GetFileList fileListGetter = new GetFileList(application);

        fileListGetter.setResponseHandler(new ActivityApiResponseHandlerAbstract(this) {

            @Override
            public void handleError(ApiError[] errors, ApiEntity entity) {
                _handleErrorsGeneric(errors, entity);
            }

            @Override
            public void entityReceived(ApiEntity entity) {
                DeviceMessage message = (DeviceMessage) entity;
                _handleFileListMessage(message, currentNickname, currentPath);
            }
        });

        fileListGetter.get(new Device(currentNickname, null), currentPath);
    }

    private void _handleFileListMessage(DeviceMessage message, String nickname, String path) {
        JSONObject fileList = message.getMetaFileList();
        String errorMessage = message.getMetaErrorMessage();
        if (errorMessage != null && errorMessage.length() > 0) {
            showErrorAlert(errorMessage);
        } else if (fileList != null) {
            processFileListReceived(fileList, getFileListRegistryKey(nickname, path));
        } else {
            // wait for reply
            if (!message.getSender().getNickname().equals(currentNickname)) {
                waitForFileListReply(message.getMessageID(), System.currentTimeMillis() + WAIT_TIMEOUT);
            } else {
                waitForFileListReply(message.getInReplyTo(), System.currentTimeMillis() + WAIT_TIMEOUT);
                DLog.i("Skew occured...");
            }
        }
    }

    private void processFileListReceived(JSONObject fileListObject, String registryKey) {
        showProgress(false);
        try {
            // copy was found on server
            listOfFiles = fileListObject.getJSONArray("files");
            application.saveToRegistry(registryKey, listOfFiles);
            updateFileListView();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            DLog.w("Could not get file list : " + e.getMessage());
            application.showToast("Directory is empty");
            folderEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private void waitForFileListReply(final long messageId, final long retryTill) {
        if (isFinished) {
            DLog.i("Activity has finished");
            return;
        }
        Messages messageReceiver = new Messages(application);
        DLog.i("Waiting for response, seconds left: " + ((retryTill - System.currentTimeMillis()) / 1000));
        messageReceiver.setResponseHandler(new ActivityApiResponseHandlerAbstract(this) {
            @Override
            public void handleError(ApiError[] errors, ApiEntity entity) {
                _handleErrorsGeneric(errors, entity);
            }

            @Override
            public void entityReceived(ApiEntity entity) {
                // TODO Auto-generated method stub
                DeviceMessageList messageList = (DeviceMessageList) entity;
                if (messageList.size() < 1) {
                    if (System.currentTimeMillis() >= retryTill) {
                        // timeout error
                        showErrorAlert(String.format(getString(R.string.file_list_timeout_error), currentNickname));
                    } else {
                        waitForFileListReply(messageId, retryTill);
                    }
                } else {
                    DeviceMessage replyMsg = messageList.getMessages().get(0);
                    _handleFileListMessage(replyMsg, replyMsg.getSender().getNickname(), replyMsg.getMetaFilePath());
                }
            }
        });

        messageReceiver.poll(messageId);
    }

    protected void doFileDownLoad(final String path) {
        final Dialog destDialog = SelectRootDialog.createDialog(0, FileListActivity.this, "Select Save Location",
                new CallbackBundle() {
                    @Override
                    public void callback(Bundle bundle) {
                        String saveLocation = bundle.getString("path");
                        lastSaveLocation = saveLocation;
                        _sendGetFileRequest(path, saveLocation);
                    }
                }, application.getImageIds(), lastSaveLocation);
        destDialog.show();
    }

    private void _sendGetFileRequest(final String path, final String saveLocation) {
        GetFile fileGetter = new GetFile(application);
        Device receiver = new Device(currentNickname, null);
        // compute location
        String tmp[] = path.split("/");
        final String baseFilename = tmp[tmp.length - 1];
        DLog.i("Save Location: "+saveLocation+" Path: "+path);
        fileGetter.setResponseHandler(new ActivityApiResponseHandlerAbstract(this) {

            @Override
            public void handleError(ApiError[] errors, ApiEntity entity) {
                _handleErrorsGeneric(errors, entity);
            }

            @Override
            public void entityReceived(ApiEntity entity) {
                DeviceMessage sentMessage = (DeviceMessage) entity;
                // create record for file download
                FileDownloadRecord fileDl = new FileDownloadRecord(saveLocation, baseFilename, path, currentNickname);
                // get the request message ID, if the ID is 0,the the stuff is
                // cached and
                // a copy was found on the server
                long messageID = sentMessage.getMessageID() > 0 ? sentMessage.getMessageID() : System
                        .currentTimeMillis();

                application.queueFileDownload(messageID, fileDl);
                if (sentMessage.getMetaFileID() > 0) {
                    application.startFileDownload(messageID, sentMessage.getMetaFileID());
                } else if (sentMessage.getMetaErrorMessage() != null && sentMessage.getMetaErrorMessage().length() > 0) {
                    application.fileDownloadHasError(messageID, sentMessage.getMetaErrorMessage());
                }

            }
        });

        fileGetter.get(receiver, path);
    }

    private void showErrorAlert(String message) {
        if (this.isFinishing()) {
            return;
        }
        showProgress(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle(R.string.error_dialog_title);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void updateFileListView() {
        showProgress(false);
        setListAdapter(new FileListAdapter(this));
    }

    protected void initViews() {
        ((TextView) findViewById(R.id.titlebar_nickname)).setText(currentNickname);
        ((TextView) findViewById(R.id.titlebar_path)).setText(currentPath);
        ImageButton upBtn = (ImageButton) findViewById(R.id.titlebar_folderup);
        if (currentPath.equals("/")) {
            upBtn.setVisibility(View.GONE);
        } else {
            upBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    FileListActivity.this.finish();
                }
            });
        }
        fileListLoadingView = findViewById(R.id.flist_status);
        loadingMessage = (TextView) findViewById(R.id.loading_status_message);
        folderEmptyView = (TextView) findViewById(R.id.empty_folder_text);
        fileListView = findViewById(android.R.id.list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.file_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            fileListLoadingView.setVisibility(View.VISIBLE);
            fileListLoadingView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            fileListLoadingView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            fileListView.setVisibility(View.VISIBLE);
            fileListView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            fileListView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            fileListLoadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            fileListView.setVisibility(show ? View.GONE : View.VISIBLE);
        }

        // folderEmptyView.setVisibility(View.GONE);
    }

    public final class CellHolder {
        public TextView fileName;
        public TextView lastModifiedDate;
        public Button optionsButton;
        public TextView fileSize;
        public ImageView fileIcon;
        public LinearLayout metadataView;
    }

    public class FileListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private Map<String, Integer> imagemap = null;

        public FileListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
            imagemap = application.getImageIds();
        }

        private int getIconResource(String filename) {
            String suffixType = OpenFileDialog.getSfType(OpenFileDialog.FileSelectView.getSuffix(filename)
                    .toLowerCase());
            if (imagemap == null) {
                return 0;
            } else if (imagemap.containsKey(suffixType)) {
                return imagemap.get(suffixType);
            } else if (imagemap.containsKey(OpenFileDialog.sEmpty)) {
                return imagemap.get(OpenFileDialog.sEmpty);
            } else {
                return 0;
            }
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if (listOfFiles == null) {
                return 0;
            }
            return listOfFiles.length();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            if (listOfFiles == null) {
                return null;
            }

            try {
                return listOfFiles.getString(position);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                return null;
            }
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            CellHolder holder = null;

            if (convertView == null) {
                holder = new CellHolder();
                convertView = mInflater.inflate(R.layout.file_list_cell, null);
                holder.fileName = (TextView) convertView.findViewById(R.id.flist_filename);
                holder.fileIcon = (ImageView) convertView.findViewById(R.id.flist_icon);
                holder.lastModifiedDate = (TextView) convertView.findViewById(R.id.flist_lastmodified);
                holder.fileSize = (TextView) convertView.findViewById(R.id.flist_filesize);
                holder.metadataView = (LinearLayout) convertView.findViewById(R.id.flist_cell);
                convertView.setTag(holder);
            } else {
                holder = (CellHolder) convertView.getTag();
            }

            try {
                final JSONObject file = listOfFiles.getJSONObject(position);
                if (holder.fileName == null) {
                    DLog.i("File is Null");
                }
                holder.fileName.setText(file.getString("name"));
                if (file.getString("type").equals("folder")) {
                    holder.fileSize.setVisibility(View.GONE);
                    holder.fileIcon.setImageResource(R.drawable.filedialog_folder);
                } else {
                    holder.fileSize.setText(CommonUtils.normalizeFileSize(file.getLong("size")));
                    holder.fileIcon.setImageResource(getIconResource(file.getString("name")));
                }
                holder.lastModifiedDate.setText("Modified: "
                        + RelativeDate.getRelativeDate(file.getLong("last_modified")));

                holder.metadataView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        try {
                            DLog.i("Getting: " + file.getString("path"));
                            if (file.getString("type").equals("folder")) {
                                Intent fileListIntent = new Intent(FileListActivity.this, FileListActivity.class);
                                fileListIntent.putExtra(EXTRA_NICKNAME, currentNickname);
                                fileListIntent.putExtra(EXTRA_PATH, file.getString("path"));
                                startActivity(fileListIntent);
                            } else {
                                FileListActivity.this.doFileDownLoad(file.getString("path"));
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return convertView;
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        isFinished = true;
    }
}
