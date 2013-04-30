package columbia.cellular.droidtransfer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import columbia.cellular.Utils.DLog;
import columbia.cellular.file.CallbackBundle;
import columbia.cellular.file.OpenFileDialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Environment;

public class UploadActivity extends ListActivity{
	private TextView uploadTab;
	private TextView peersTab;
	private String userId;
	private String email;
	
	private List<Map<String, Object>> list = null;
	private Map<String, Object> map;
	private String allfiles="";
	static private int openfileDialogId = 0;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload);
		
		list = new ArrayList<Map<String, Object>>();
		
		Intent i = getIntent();
		userId = i.getStringExtra("nickname");
		email = i.getStringExtra("email");
		
		
		DLog.i("Upload Activity Start  id: "+userId+"  email: "+email);
		
		initViews();	    
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		
		if(id==openfileDialogId){
			Map<String, Integer> images = getImageIds();
			
			final Dialog dialog = OpenFileDialog.createDialog(id, this, "Select File", new CallbackBundle() {
				@Override
				public void callback(Bundle bundle) {
					
					String filepath = bundle.getString("path");
					String filename = bundle.getString("name");
					int imgid = bundle.getInt("img");
					
					
					map = new HashMap<String, Object>();
					map.put("name", filename);
					map.put("path", filepath);
					map.put("img", imgid);
					
					list.add(map);
					allfiles+=filename+";";
					refreshList();
					
					setTitle(filepath);
					
					DLog.i("Selected File:"+filename+"   path: "+filepath+ " id: "+imgid);
				}
			}, 
			"",
			images);
			return dialog;
		}
		return null;
	}
	
	//refresh updated list
	private void refreshList(){
		SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.filedialogitem, new String[]{"img", "name", "path"}, new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path});
		setListAdapter(adapter);
	}
	
	//show uploading files
	public void showInfo(String name){
        new AlertDialog.Builder(this)
        .setTitle("Upload")
        .setMessage("Uploading: "+name)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })
        .show();
         
    }
	
	//connect file types with image resourceId
	
	private Map<String,Integer> getImageIds() {
		
		Map<String, Integer> images = new HashMap<String, Integer>();
		// 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
		images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);	// root
		images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);	//back
		images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);	//folder
		images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root); 
		images.put("music", R.drawable.filedialog_music);	
		images.put("film", R.drawable.filedialog_film);
		images.put("pdf", R.drawable.filedialog_pdf);
		images.put("ppt", R.drawable.filedialog_ppt);
		images.put("db", R.drawable.filedialog_db);
		images.put("zip", R.drawable.filedialog_zip);
		images.put("xls", R.drawable.filedialog_xls);
		images.put("java", R.drawable.filedialog_java);
		images.put("html", R.drawable.filedialog_html);
		images.put("code", R.drawable.filedialog_code);
		images.put("picture", R.drawable.filedialog_picture);
		images.put("application", R.drawable.filedialog_application);		
		images.put("other", R.drawable.filedialog_file);
		
		return images;
		
	}
	
	private void initViews(){
		uploadTab = (TextView)findViewById(R.id.upload_tab1);
		peersTab  = (TextView)findViewById(R.id.list_tab1);
		
		//By default we select peers tab
		uploadTab.setBackgroundResource(R.drawable.main_tab_selected_background);
  //      uploadTab.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_tab_tradition_selected) , null, null);
		
		peersTab.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent(UploadActivity.this,MainActivity.class)
            	.putExtra("nickname", userId)
            	.putExtra("email", email);
            	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	startActivity(i);
                
            }
        });
		
		 findViewById(R.id.selectFileButton).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					showDialog(openfileDialogId);
					DLog.i("Selecting File");
				}
			});
		 findViewById(R.id.uploadButton).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					showInfo(allfiles);
					DLog.i("Upload these files :"+allfiles);
				}
			});
		
		//allow MultiThreaded
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
	}

}
