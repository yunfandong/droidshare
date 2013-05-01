package columbia.cellular.droidtransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import columbia.cellular.Utils.DLog;
import columbia.cellular.api.apicalls.PairList;
import columbia.cellular.api.apicalls.PairWith;
import columbia.cellular.api.entities.Device;
import columbia.cellular.api.entities.DeviceList;
import columbia.cellular.api.entities.DeviceMessage;
import columbia.cellular.api.service.ApiEntity;
import columbia.cellular.api.service.ApiError;
import columbia.cellular.droidtransfer.droidService.droidServiceBinder;
import columbia.cellular.file.CallbackBundle;
import columbia.cellular.file.OpenFileDialog;

public class MainActivity extends FtDroidActivity {

	private TextView uploadTab;
	private TextView peersTab;

	private View pairListView;
	private View loadPairStatusView;
	private TextView loadStatusMessageView;
	private TextView pairListStatus;

	public droidApp app;
	public droidService myService;
	AsyncTask<Void, Void, Void> mRegisterTask;

	static private int listfileDialogId = 0;
	MyAdapter adapter;
	protected DeviceList listOfPairs;
	private static MainActivity instance;
	private static boolean suppressErrors = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		instance = this;
		app = (droidApp) getApplication();
		// init
		initViews();
		initMainHandler();
		// initGCM();
		loadStatusMessageView.setText(R.string.loading);
		Device regDev = getRegisteredDevice();
		app.showToast("Welcome  " + regDev.getNickname());
		// DLog.i("Device info:"+regDev.toString());
		// get peerList
		// peerList = initList();
		// if (peerList.size() > 0) {
		// adapter = new MyAdapter(this);
		// setListAdapter(adapter);
		// }
		loadPairList();
	}

	protected void loadPairList() {
		// try local else
		showProgress(true);
		PairList pairListLoader = new PairList(this);
		pairListLoader.getPairList();
	}

	protected void updateListView() {
		pairListStatus.setVisibility(View.GONE);
		adapter = new MyAdapter(this);
		setListAdapter(adapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		bindMyService();
	}

	// register for google cloud messaging

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			DLog.i("ServiceConnection is Connected");
			myService = ((droidServiceBinder) service).getService();

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			DLog.i("ServiceConnection is Disconnected");
			myService = null;
		}
	};

	private void bindMyService() {
		Intent serviceIntent = new Intent(this, droidService.class);
		bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
		app.setServiceState(true);

	}

	public void initMainHandler() {
		app.setMainHandler(new MainHandler());
	}

	private static class MainHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == droidApp.RESPONSE_MESSAGE) {
				Bundle bundle = msg.getData();
				String data = bundle.getString("data");
				DLog.i("MainActivity Receives Response :" + data);
			}
		}
	}

	/***************************************************************
	 * Basic (views, userinfos,dialogs) initialization part yd2238
	 *************************************************************** */

	protected Dialog onCreateDialog(int id) {

		if (id == listfileDialogId) {
			Map<String, Integer> images = getImageIds();

			final Dialog dialog = OpenFileDialog.createDialog(id, this,
					"Select File", new CallbackBundle() {
						@Override
						public void callback(Bundle bundle) {

							String filepath = bundle.getString("path");
							String filename = bundle.getString("name");

							setTitle(filepath);

							DLog.i("Selected File:" + filename + "   path: "
									+ filepath);
						}
					}, "", images);
			return dialog;
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		menu.add("add device").setIcon(R.drawable.maintab_users_selected);
		menu.add("setting").setIcon(R.drawable.maintab_setting_selected);

		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("add device"))
			showAddDeviceInfo();
		return true;
	}

	/***************************************************************
	 * Functions & Listview Adaptation part yd2238
	 *************************************************************** */

	private void initViews() {

		pairListView = (LinearLayout) findViewById(R.id.pairListLayout);
		loadPairStatusView = (LinearLayout) findViewById(R.id.login_status);
		loadStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		pairListStatus = (TextView) findViewById(R.id.pair_list_empty);
		pairListStatus.setVisibility(View.GONE);

		uploadTab = (TextView) findViewById(R.id.upload_tab);
		peersTab = (TextView) findViewById(R.id.list_tab);

		// By default we select peers tab
		peersTab.setBackgroundResource(R.drawable.main_tab_selected_background);

		uploadTab.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, SettingActivity.class);

				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});
		
		peersTab.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showPairingInfo();
			}
		});
		// allow MultiThreaded

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

	}

	public final class ListCellHolder {
		public TextView nickname;
		public TextView lastSeen;
		public Button connectButton;
		public TextView email;
	}

	public void showInfo() {
		new AlertDialog.Builder(this)
				.setTitle("Warning")
				.setMessage("Currently no paired devices")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setNegativeButton("Add Device",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								DLog.i("Add Device!!");
								showAddDeviceInfo();
							}
						}).show();

		pairListStatus.setVisibility(View.VISIBLE);
	}
	
	public void showPairingInfo() {
		new AlertDialog.Builder(this)
				.setTitle("Pairing")
				.setMessage("Dong Yunfan wants to pair with you, do you agree?")
				.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setNegativeButton("Decline",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								DLog.i("Add Device!!");
							//	showAddDeviceInfo();
							}
						}).show();

		pairListStatus.setVisibility(View.VISIBLE);
	}

	public void showAddDeviceInfo() {

		final Dialog dialog = new Dialog(this);
		dialog.setTitle("Add a device");

		dialog.setContentView(R.layout.pair_dialog);
		final EditText nickname = (EditText) dialog
				.findViewById(R.id.label_nickname);
		Button pair = (Button) dialog.findViewById(R.id.pair_button);

		pair.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (nickname.getText().equals("")) {
					app.showToast("please input a nickname ^_^");
				} else {
					doPairWith(nickname.getText().toString());
					dialog.dismiss();
				}
			}
		});

		dialog.show();

	}

	private Map<String, Integer> getImageIds() {

		Map<String, Integer> images = new HashMap<String, Integer>();
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

		return images;

	}

	public class MyAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private ArrayList<Device> deviceList;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
			deviceList = listOfPairs.getDevices();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return deviceList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ListCellHolder holder = null;

			if (convertView == null) {

				holder = new ListCellHolder();
				convertView = mInflater.inflate(R.layout.listcell, null);

				holder.email = (TextView) convertView
						.findViewById(R.id.label_pairemail);
				holder.nickname = (TextView) convertView
						.findViewById(R.id.label_nickname);
				holder.lastSeen = (TextView) convertView
						.findViewById(R.id.label_last_seen);
				holder.connectButton = (Button) convertView
						.findViewById(R.id.connectButton);
				// holder.state = (ImageView)
				// convertView.findViewById(R.id.state);

				convertView.setTag(holder);

			} else {
				holder = (ListCellHolder) convertView.getTag();
				DLog.i("Using... getTag()");
			}

			Device currentPair = deviceList.get(position);

			holder.nickname.setText(currentPair.getNickname());
			holder.lastSeen.setText(currentPair.getLastSeen());
			holder.email.setText(currentPair.getEmail());

			holder.connectButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// open current Device
					showDialog(listfileDialogId);
				}
			});

			return convertView;
		}

	}

	public void doPairWith(String device) {
		PairWith pairWith = new PairWith(this);
		pairWith.pairWith(new Device(device, ""));
		DLog.i("Pair Request Sent :" + device);
		// app.showToast("Pair Request Sent (" + device + ")");
	}

	@Override
	public void entityReceived(ApiEntity entity) {
		showProgress(false);
		if (entity.getClass() == DeviceList.class) {
			listOfPairs = (DeviceList) entity;
			if (listOfPairs.size() == 0) {
				showInfo();
			} else {
				pairListStatus.setVisibility(View.GONE);
				updateListView();
			}
		} else if (entity.getClass() == DeviceMessage.class) {
			((droidApp) getApplication())
					.showToast("Pairing request has been sent.");
		} else {
			((droidApp) getApplication()).showToast("Unknown message.");
		}

	}

	@Override
	public void handleError(ApiError[] errors, ApiEntity entity) {
		// TODO Auto-generated method stub
		showProgress(false);
		_handleErrorsGeneric(errors, entity);
	}

	protected void showProgress(final boolean show) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			loadPairStatusView.setVisibility(View.VISIBLE);
			loadPairStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							loadPairStatusView
									.setVisibility(show ? View.VISIBLE
											: View.GONE);
						}
					});

			pairListView.setVisibility(View.VISIBLE);
			pairListView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							pairListView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			loadPairStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			pairListView.setVisibility(show ? View.GONE : View.VISIBLE);
		}

	}

	public void setSuppressErrors(boolean b) {
		suppressErrors = b;
	}

	public static MainActivity getInstance(){
		return instance;
	}
}
