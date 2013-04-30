package columbia.cellular.file;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileListGen {
	// current path
	String path;
	// root path
	String root;
	String fullPath;
	String errorMessage;
	JSONObject json_to_send = new JSONObject();
	JSONArray list_of_files = new JSONArray();

	public FileListGen(String root, String path) {
		this.path = path;
		this.root = root;
		fullPath = root + path;
	}

	public JSONObject getListJSON() throws JSONException {

		File curFile = new File(fullPath);
		if (!curFile.exists()) {
			errorMessage = "The path specified does not exist";
			return null;
		}

		if (!curFile.isDirectory()) {
			errorMessage = "The path specified is not a directory";
			return null;
		}

		if (!curFile.canRead()) {
			errorMessage = "Cannot read the specified path.";
			return null;
		}

		File[] files = curFile.listFiles();
		for (File file : files) {
			String type = file.isDirectory() ? "folder" : "file";
			String relative_path = file.getPath().substring(root.length());
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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
