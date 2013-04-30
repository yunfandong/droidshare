package columbia.cellular.file;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import columbia.cellular.Utils.DLog;

public class FileListGen {
	
	//current path
	String path;
	
	//root path
	String root;
	
	/**
	 * 
	 * typical filelist json:
	{
	files:
	 [

				{
					name: "File Name",
					type: "folder",
					size: 901919	(in bytes),
					last_modified: date (string),
					path: "/" path relative to home folder
				},
				{
					name: "File Name",
					type: "file",
					size: 901919	(in bytes),
					last_modified: date (string),
					path: "/" path relative to home folder
				},

	]
   }
	 */
	
	public FileListGen(String root,String path){
		this.path = path;
		this.root = root;
	}
	
	
	
	
	JSONObject json_to_send = new JSONObject();
	JSONArray  list_of_files = new JSONArray();
	
	
	
	public JSONObject getListJSON() throws JSONException{
	
	File curFile = new File(path);

	File[] files = curFile.listFiles();
	
	for(File file:files){
		
		String type = file.isDirectory()?"folder":"file";
		
		
        
		String relative_path = file.getPath().substring(file.getPath().lastIndexOf(root));
	
		
	//	DLog.i(file.getPath()+"   :   "+root+"   :   "+relative_path);
		
		JSONObject file_item = new JSONObject();
		file_item.put("name", file.getName());
		file_item.put("type", type);
		file_item.put("size", file.length());
		file_item.put("last_modified", file.lastModified());
		file_item.put("path", relative_path);
		
		list_of_files.put(file_item);	

	}
	
	json_to_send.put("files", list_of_files);
	
	return json_to_send;
	
	}
	
	
	


}
