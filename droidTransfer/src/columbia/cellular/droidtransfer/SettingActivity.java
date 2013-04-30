package columbia.cellular.droidtransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import columbia.cellular.Utils.DLog;
import columbia.cellular.file.CallbackBundle;
import columbia.cellular.file.OpenFileDialog;
import columbia.cellular.file.SelectRootDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SettingActivity extends Activity {
	String userId;
	String email;
	String imei;
	droidApp app;
	
	TextView uploadTab;
	TextView peersTab;
	String rootPath;
	
	private int selectPathDialogId = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		
		Intent i = getIntent();
	    app = (droidApp)getApplication();
		
		userId = app.getNickName();
		email = app.getEmail();
		
		if(app.getRootPath().length()>1) rootPath = app.getRootPath();
		else rootPath = Environment.getExternalStorageDirectory().getPath();
		
		
		DLog.i("Setup Activity Start  id: "+userId+"  email: "+email);		
		
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
		
		if(id==selectPathDialogId){
			Map<String, Integer> images = getImageIds();
			final Dialog dialog = SelectRootDialog.createDialog(id, this, "Select File", new CallbackBundle() {
				@Override
				public void callback(Bundle bundle) {
					
				    rootPath = bundle.getString("path");

					
					setTitle(rootPath);
					updatePath(rootPath);
					
					DLog.i("Root Path changed to: "+ rootPath);
				}
			}, 
			images);
			return dialog;
		}
		return null;
	}
	
	private void updatePath(String path){
		 TextView text = (TextView)findViewById(R.id.rootpath);
		 text.setText(path);
		 app.setRootPath(path);
		
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
	
	public void initViews(){
		uploadTab = (TextView)findViewById(R.id.upload_tab1);
		peersTab  = (TextView)findViewById(R.id.list_tab1);
		TextView text = (TextView)findViewById(R.id.rootpath);
		
		text.setText(rootPath);
		
		//By default we select peers tab
		uploadTab.setBackgroundResource(R.drawable.main_tab_selected_background);
  //      uploadTab.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_tab_tradition_selected) , null, null);
		
		peersTab.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent(SettingActivity.this,MainActivity.class);
            	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	startActivity(i);
                
            }
        });
		
		 findViewById(R.id.selectPathButton).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					showDialog(selectPathDialogId);
					DLog.i("Selecting Path");
				}
			});
		
	}

}
