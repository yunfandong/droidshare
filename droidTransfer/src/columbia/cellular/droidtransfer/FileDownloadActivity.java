package columbia.cellular.droidtransfer;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import columbia.cellular.Utils.DLog;
import columbia.cellular.file.OpenFileDialog;

public class FileDownloadActivity extends ListActivity {

    protected DroidApp application;
    protected Timer timer = new Timer(); 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_file_download);
        application = (DroidApp) getApplication();
        if (application.getFiledownloadQueue().size() < 1) {
            findViewById(R.id.download_empty).setVisibility(View.VISIBLE);
        }
        doDownloadListUpdate();
        timer.scheduleAtFixedRate(new CellUpdateTask(), 2000, 2000);
    }

    private void doDownloadListUpdate() {
        setListAdapter(new FileDownloadListAdapter(this));
        DLog.i("Updating views");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.file_download, menu);
        return true;
    }

    private class CellUpdateTask extends TimerTask{
        @Override
        public void run() {        
            runOnUiThread(new Runnable() {              
                @Override
                public void run() {
                    doDownloadListUpdate();
                }
            });
        }       
    }
    public final class CellHolder {
        public TextView fileName;
        public TextView statusText;
        public TextView destinationText;
        public ImageView fileIcon;
        public LinearLayout downloadCell;
        public ProgressBar progressBar;
        private Map<String, Integer> imagemap = null;
        
        private int getIconResource(String filename) {
            if(imagemap == null){
                imagemap = application.getImageIds(); 
            }
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
        
        public void updateWith(FileDownloadRecord downloadRecord){
            fileName.setText(downloadRecord.getBaseFilename());
            fileIcon.setImageResource(getIconResource(downloadRecord.getRemotePath()));
            destinationText.setText("Downloading to " + downloadRecord.getDestination());
            
            String statusTextStr;
            if(downloadRecord.completed || downloadRecord.hasError){
                statusTextStr = downloadRecord.getStatusText();
                progressBar.setVisibility(View.GONE);
                destinationText.setText("Saved to " + downloadRecord.getDestination());
            }else{
                progressBar.setMax(100);
                progressBar.setProgress(downloadRecord.percentDone);
                statusTextStr= downloadRecord.percentDone + "% "+ downloadRecord.getStatusText();
                destinationText.setText("Downloading to " + downloadRecord.getDestination());
            }
            
            statusText.setText(statusTextStr);
        }
    }

    public class FileDownloadListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private Long[] downloadKeys;

        public FileDownloadListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
            downloadKeys = new Long[application.getFiledownloadQueue().size()];
            application.getFiledownloadQueue().keySet().toArray(downloadKeys);
            for(int i = 0; i < downloadKeys.length/2; i++)
            {
                Long temp = downloadKeys[i];
                downloadKeys[i] = downloadKeys[downloadKeys.length - i - 1];
                downloadKeys[downloadKeys.length - i - 1] = temp;
            }
        }

        @Override
        public int getCount() {
            return downloadKeys.length;
        }

        @Override
        public Object getItem(int position) {
            return application.getFiledownloadQueue().get(downloadKeys[position]);
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return downloadKeys[arg0];
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            CellHolder holder = null;

            if (convertView == null) {
                holder = new CellHolder();
                convertView = mInflater.inflate(R.layout.download_record_cell, null);
                holder.fileName = (TextView) convertView.findViewById(R.id.download_filename);
                holder.fileIcon = (ImageView) convertView.findViewById(R.id.download_fileicon);
                holder.destinationText = (TextView) convertView.findViewById(R.id.download_destination);
                holder.downloadCell = (LinearLayout) convertView.findViewById(R.id.download_cell);
                holder.progressBar = (ProgressBar) convertView.findViewById(R.id.download_progressbar);
                holder.statusText = (TextView) convertView.findViewById(R.id.download_status);
                convertView.setTag(holder);
            } else {
                holder = (CellHolder) convertView.getTag();
            }

            final FileDownloadRecord downloadRecord = (FileDownloadRecord) getItem(position);
            holder.updateWith(downloadRecord);
            return convertView;
        }

        
    }

}
