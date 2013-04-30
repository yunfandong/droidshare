package columbia.cellular.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import columbia.cellular.Utils.DLog;
import columbia.cellular.droidtransfer.R;
import columbia.cellular.file.OpenFileDialog.FileSelectView;

public class ListFileDialog {
	public static String tag = "OpenFileDialog";
	
	// parameters
	// context:context
	// dialogid: ID of the dialog
	// title: title of the dialog
	// callback: an interface to pass bundle parameters
	
	// suffix:  file types e.g if we only want .mp3 and .wav files suffix should be: .mp3;.wav; 
	// images:image IDs for different types of files & folders
		//  root is sRoot;
		//	parent is sParent;
		//	folder is sFolder;
		//	default is sEmpty;
		//	other files depend on the file type
	
	public static Dialog createDialog(int id, Context context, String title, CallbackBundle callback, Map<String, Integer> images,List<String> files){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(new FileSelectView(context, id, callback, images,files));
		Dialog dialog = builder.create();
		//dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setTitle(title);
		return dialog;
	}
	
	
	//adapt files to proper types
	
	public static String getSfType(String suffix){
		String type;
		
		//Music
	    if (suffix.equals("wav")) type="music";
	    else if (suffix.equals("mp3")) type="music";
	    else if (suffix.equals("ap2")) type="music";
	    //Movies
	    else if (suffix.equals("mp4")) type="film";
	    else if (suffix.equals("rmvb")) type="film";
	    else if (suffix.equals("flv")) type="film";
	    //Docs
	    else if (suffix.equals("pdf")) type="pdf";
	    else if (suffix.equals("ppt")) type="ppt";
	    else if (suffix.equals("db")) type="db";
	    else if (suffix.equals("zip")) type="zip";
	    else if (suffix.equals("rar")) type="zip";
	    else if (suffix.equals("xls")) type="xls";
	    else if (suffix.equals("java")) type="java";
	    else if (suffix.equals("apk")) type="zip";
	    else if (suffix.equals("txt")) type="txt";
	    else if (suffix.equals("html")) type="html";
	    else if (suffix.equals("c")) type="code";
	    else if (suffix.equals("xml")) type="code";
	    //Pics
	    else if (suffix.equals("jpg")) type="picture";
	    else if (suffix.equals("jpeg")) type="picture";
	    else if (suffix.equals("png")) type="picture";
	    else if (suffix.equals("gif")) type="picture";
	    //Application
	    else if (suffix.equals("exe")) type="application";
	    else if (suffix.equals("jar")) type="application";
	    
	    else type="other";
		return type;
	}
	
	static class FileSelectView extends ListView implements OnItemClickListener{
		
		
		private CallbackBundle callback = null;
		private List<Map<String, Object>> list = null;
		private List<String> filelist;
		
		private int dialogid = 0;
		
		private String suffix = null;
		
		private Map<String, Integer> imagemap = null;
		
		public FileSelectView(Context context, int dialogid, CallbackBundle callback, Map<String, Integer> images,List<String> files) {
			super(context);
			this.imagemap = images;
			this.callback = callback;
			this.dialogid = dialogid;
			this.filelist = files;
			this.setOnItemClickListener(this);
			refreshFileList();
		}
		
		private String getSuffix(String filename){
			int dix = filename.lastIndexOf('.');
			if(dix<0){
				return "";
			}
			else{
				return filename.substring(dix+1);
			}
		}
		
		private int getImageId(String s){
			if(imagemap == null){
				return 0;
			}
			else if(imagemap.containsKey(s)){
				return imagemap.get(s);
			}
			else {
				return 0;
			}
		}
		
		private int refreshFileList()
		{
			if(list != null){
				list.clear();
			}
			else{
				list = new ArrayList<Map<String, Object>>(filelist.size());
			}
			
		   
		    for (String filename : filelist){
		    	String sf = getSuffix(filename).toLowerCase();
		    	Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", filename);
				map.put("img", getImageId(getSfType(sf)));
				
				list.add(map);
		    	
		    }
			
			
			SimpleAdapter adapter = new SimpleAdapter(getContext(), list, R.layout.listdialogitem, new String[]{"img", "name"}, new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name});
			this.setAdapter(adapter);
			
			return filelist.size();
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			// 条目选择
			String fn = (String) list.get(position).get("name");
			int imgid = (Integer)list.get(position).get("img");
			
			DLog.i("File Selected: "+fn);
			
			((Activity)getContext()).dismissDialog(this.dialogid); // 让文件夹对话框消失
			
			// 设置回调的返回值
			Bundle bundle = new Bundle();
			bundle.putString("name", fn);
			bundle.putInt("img", imgid);
			// 调用事先设置的回调函数
			this.callback.callback(bundle);
			
			this.refreshFileList();
			return;
					
		}
	}
}

