package columbia.cellular.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import columbia.cellular.Utils.DLog;
import columbia.cellular.droidtransfer.R;

public class SelectRootDialog {

	public static String tag = "SelectRootDialog";

	static final public String sRoot = "/";
	static final public String sParent = "..";
	static final public String sFolder = ".";
	static final public String sEmpty = "";
	static final private String sOnErrorMsg = "No rights to access!";
	static String curPath = sRoot;

	// parameters
	// context:context
	// dialogid: ID of the dialog
	// title: title of the dialog
	// callback: an interface to pass bundle parameters

	// suffix: file types e.g if we only want .mp3 and .wav files suffix should
	// be: .mp3;.wav;
	// images:image IDs for different types of files & folders
	// root is sRoot;
	// parent is sParent;
	// folder is sFolder;
	// default is sEmpty;
	// other files depend on the file type

	public static Dialog createDialog(int id, Context context, String title,
			final CallbackBundle callback, Map<String, Integer> images, String startPath) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(new FileSelectView(context, images, startPath));

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Bundle bundle = new Bundle();
				bundle.putString("path", curPath);
				// �����������õĻص�����
				callback.callback(bundle);
			}
		});
		Dialog dialog = builder.create();

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setTitle(title);

		return dialog;
	}

	// adapt files to proper types

	public static String getSfType(String suffix) {
		String type;

		// Music
		if (suffix.equals("wav"))
			type = "music";
		else if (suffix.equals("mp3"))
			type = "music";
		else if (suffix.equals("ap2"))
			type = "music";
		// Movies
		else if (suffix.equals("mp4"))
			type = "film";
		else if (suffix.equals("rmvb"))
			type = "film";
		else if (suffix.equals("flv"))
			type = "film";
		// Docs
		else if (suffix.equals("pdf"))
			type = "pdf";
		else if (suffix.equals("ppt"))
			type = "ppt";
		else if (suffix.equals("db"))
			type = "db";
		else if (suffix.equals("zip"))
			type = "zip";
		else if (suffix.equals("rar"))
			type = "zip";
		else if (suffix.equals("xls"))
			type = "xls";
		else if (suffix.equals("java"))
			type = "java";
		else if (suffix.equals("apk"))
			type = "zip";
		else if (suffix.equals("txt"))
			type = "txt";
		else if (suffix.equals("html"))
			type = "html";
		else if (suffix.equals("c"))
			type = "code";
		else if (suffix.equals("xml"))
			type = "code";
		// Pics
		else if (suffix.equals("jpg"))
			type = "picture";
		else if (suffix.equals("jpeg"))
			type = "picture";
		else if (suffix.equals("png"))
			type = "picture";
		else if (suffix.equals("gif"))
			type = "picture";
		// Application
		else if (suffix.equals("exe"))
			type = "application";
		else if (suffix.equals("jar"))
			type = "application";

		else
			type = "other";
		return type;
	}

	static class FileSelectView extends ListView implements OnItemClickListener {

		private String path = sRoot;
		private List<Map<String, Object>> list = null;

		private String suffix = null;

		private Map<String, Integer> imagemap = null;

		public FileSelectView(Context context, Map<String, Integer> images, String startPath) {
			super(context);
			this.imagemap = images;
			this.suffix = suffix == null ? "" : suffix.toLowerCase();
			this.setOnItemClickListener(this);
			this.path = startPath;
			refreshFileList();
		}

		private String getSuffix(String filename) {
			int dix = filename.lastIndexOf('.');
			if (dix < 0) {
				return "";
			} else {
				return filename.substring(dix + 1);
			}
		}

		private int getImageId(String s) {
			if (imagemap == null) {
				return 0;
			} else if (imagemap.containsKey(s)) {
				return imagemap.get(s);
			} else if (imagemap.containsKey(sEmpty)) {
				return imagemap.get(sEmpty);
			} else {
				return 0;
			}
		}

		private int refreshFileList() {
			// ˢ���ļ��б�
			File[] files = null;
			// String allfiles;

			try {
				files = new File(path).listFiles();
			} catch (Exception e) {
				files = null;
			}
			if (files == null) {
				// ���ʳ���
				Toast.makeText(getContext(), sOnErrorMsg, Toast.LENGTH_SHORT)
						.show();
				return -1;
			}
			if (list != null) {
				list.clear();
			} else {
				list = new ArrayList<Map<String, Object>>(files.length);
			}

			// �����ȱ����ļ��к��ļ��е������б�
			ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String, Object>>();
			ArrayList<Map<String, Object>> lfiles = new ArrayList<Map<String, Object>>();

			if (!this.path.equals(sRoot)) {
				// ��Ӹ�Ŀ¼ �� ��һ��Ŀ¼
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", sRoot);
				map.put("path", sRoot);
				map.put("img", getImageId(sRoot));
				list.add(map);

				map = new HashMap<String, Object>();
				map.put("name", sParent);
				map.put("path", path);
				map.put("img", getImageId(sParent));
				list.add(map);
			}

			for (File file : files) {

				if (file.isDirectory() && file.listFiles() != null) {
					// ����ļ���
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("name", file.getName());
					map.put("path", file.getPath());
					map.put("img", getImageId(sFolder));
					// DLog.i("Directory: "+file.getName());
					lfolders.add(map);
				} else if (file.isFile()) {
					// ����ļ�
					String sf = getSuffix(file.getName()).toLowerCase();

					// if(suffix == null || suffix.length()==0 || (sf.length()>0
					// && suffix.indexOf("."+sf+";")>=0)){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("name", file.getName());
					map.put("path", file.getPath());
					map.put("img", getImageId(getSfType(sf)));
					lfiles.add(map);
					//
				}
				// allfiles += file.getName();
			}

			list.addAll(lfolders); // Folders first
			// list.addAll(lfiles); // then Files

			SimpleAdapter adapter = new SimpleAdapter(
					getContext(),
					list,
					R.layout.filedialogitem,
					new String[] { "img", "name", "path" },
					new int[] { R.id.filedialogitem_img,
							R.id.filedialogitem_name, R.id.filedialogitem_path });
			this.setAdapter(adapter);
			return list.size();
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			// ��Ŀѡ��
			String pt = (String) list.get(position).get("path");
			String fn = (String) list.get(position).get("name");
			int refreshFlag = 0;

			DLog.i("Folder Selected: " + fn + "   Path: " + pt);

			if (fn.equals(sRoot) || fn.equals(sParent)) {
				// ����Ǹ�Ŀ¼������һ��
				File fl = new File(pt);
				String ppt = fl.getParent();
				if (ppt != null) {
					// ������һ��
					path = ppt;
				} else {
					// ���ظ�Ŀ¼
					path = sRoot;
				}
			}

			else {
				File fl = new File(pt);
				if (fl.isFile()) {

					/*
					 * // ������ļ�
					 * ((Activity)getContext()).dismissDialog(this.dialogid); //
					 * ���ļ��жԻ�����ʧ
					 * 
					 * // ���ûص��ķ���ֵ Bundle bundle = new Bundle();
					 * bundle.putString("path", pt); bundle.putString("name",
					 * fn); bundle.putInt("img", imgid); //
					 * �����������õĻص����� this.callback.callback(bundle);
					 * return;
					 */
					refreshFlag = 1;
				} else if (fl.isDirectory()) {
					// ������ļ���
					// ��ô����ѡ�е��ļ���
					path = pt;
				}
			}
			curPath = path;

			if (refreshFlag == 0)
				this.refreshFileList();
		}
	}

}
