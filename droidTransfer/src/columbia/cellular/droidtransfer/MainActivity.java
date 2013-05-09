package columbia.cellular.droidtransfer;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
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
import columbia.cellular.Utils.RelativeDate;
import columbia.cellular.api.apicalls.ActivityApiResponseHandlerAbstract;
import columbia.cellular.api.apicalls.PairList;
import columbia.cellular.api.apicalls.PairResponse;
import columbia.cellular.api.apicalls.PairWith;
import columbia.cellular.api.entities.Device;
import columbia.cellular.api.entities.DeviceList;
import columbia.cellular.api.service.ApiEntity;
import columbia.cellular.api.service.ApiError;
import columbia.cellular.droidtransfer.droidService.droidServiceBinder;
import columbia.cellular.file.CallbackBundle;
import columbia.cellular.file.OpenFileDialog;

public class MainActivity extends ListActivity {

	private TextView settingsTab;
	private TextView peersTab;

	private View pairListView;
	private View loadPairStatusView;
	private TextView loadStatusMessageView;
	private TextView pairListStatus;

	public DroidApp app;
	public droidService myService;
	AsyncTask<Void, Void, Void> mRegisterTask;

	static private int listfileDialogId = 0;
	MyAdapter adapter;
	protected DeviceList listOfPairs;
	private static MainActivity instance;

	public static final String EXTRA_PAIR_MESSAGE = "pairMsg";
	public static final String EXTRA_PAIR_MESSAGE_ID = "msgId";
	public static final String EXTRA_PAIR_LIST_REFRESH = "refreshList";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		instance = this;
		app = (DroidApp) getApplication();
		initViews();
		initMainHandler();
		loadStatusMessageView.setText(R.string.loading);
		Device regDev = app.getRegisteredDevice();
		app.showToast("Welcome  " + regDev.getNickname());
		loadPairList();
	}

	protected void loadPairList() {
		loadPairList(false);
	}

	protected void loadPairList(boolean forceReload) {
		final String regKey = "PairList";
		if (!forceReload && app.getFromRegistry(regKey, null) != null) {
			listOfPairs = (DeviceList) app.getFromRegistry(regKey, null);
			updateListView();
			return;
		}

		// try local else
		showProgress(true);
		PairList pairListLoader = new PairList(app);
		pairListLoader
				.setResponseHandler(new ActivityApiResponseHandlerAbstract(this) {

					@Override
					public void handleError(ApiError[] errors, ApiEntity entity) {
						showProgress(false);
						_handleErrorsGeneric(errors, entity);
					}

					@Override
					public void entityReceived(ApiEntity entity) {
						showProgress(false);
						listOfPairs = (DeviceList) entity;
						app.saveToRegistry(regKey, listOfPairs);
						if (listOfPairs.size() == 0) {
							showInfo();
							updateListView();
							pairListStatus.setVisibility(View.VISIBLE);
						} else {
							updateListView();
						}
					}
				});
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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		_checkIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		_checkIntent(intent);
	}

	protected void _checkIntent(Intent intent) {
		if (intent == null) {
			return;
		}

		// DLog.i("Extras: "+intent.getExtras());
		String pairMessage = intent.getStringExtra(EXTRA_PAIR_MESSAGE);
		String pairMessageId = intent.getStringExtra(EXTRA_PAIR_MESSAGE_ID);
		DLog.i("Message: " + pairMessage + " messageID: " + pairMessageId);
		if (pairMessage != null && pairMessageId != null) {
			showPairingInfo(pairMessage, pairMessageId);
		}

		if (intent.getBooleanExtra(EXTRA_PAIR_LIST_REFRESH, false)) {
			loadPairList(true);
		}

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
			if (msg.what == DroidApp.RESPONSE_MESSAGE) {
				Bundle bundle = msg.getData();
				String data = bundle.getString("data");
				DLog.i("MainActivity Receives Response :" + data);
			}
		}
	}

	protected Dialog onCreateDialog(int id) {

		if (id == listfileDialogId) {
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
					}, "", app.getImageIds());
			return dialog;
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		menu.add(getString(R.string.menu_add_device))
				.setIcon(R.drawable.maintab_users_selected)
				.setOnMenuItemClickListener(
						new MenuItem.OnMenuItemClickListener() {

							@Override
							public boolean onMenuItemClick(MenuItem item) {
								showAddDeviceInfo();
								return true;
							}
						});
		menu.add(getString(R.string.menu_refresh))
				.setIcon(R.drawable.maintab_setting_selected)
				.setOnMenuItemClickListener(
						new MenuItem.OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								loadPairList(true);
								return true;
							}
						});

		menu.add(getString(R.string.menu_settings))
				.setIcon(R.drawable.maintab_setting_selected)
				.setOnMenuItemClickListener(
						new MenuItem.OnMenuItemClickListener() {

							@Override
							public boolean onMenuItemClick(MenuItem item) {
								Intent i = new Intent(MainActivity.this,
										SettingActivity.class);
								i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(i);
								return true;
							}
						});
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
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

		settingsTab = (TextView) findViewById(R.id.upload_tab);
		peersTab = (TextView) findViewById(R.id.list_tab);

		// By default we select peers tab
		peersTab.setBackgroundResource(R.drawable.main_tab_selected_background);

		settingsTab.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, SettingActivity.class);

				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
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
	}

	public void showPairingInfo(String message, final String messageID) {
		new AlertDialog.Builder(this)
				.setTitle("Pairing Request")
				.setMessage(message)
				.setPositiveButton("Accept",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								_respondToPairingRequest(true, messageID);
							}
						})
				.setNegativeButton("Decline",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								_respondToPairingRequest(false, messageID);
							}
						}).show();

	}

	private void _respondToPairingRequest(final boolean response,
			String messageID) {
		PairResponse responder = new PairResponse(app);
		responder.setResponseHandler(new ActivityApiResponseHandlerAbstract(
				this) {

			@Override
			public void handleError(ApiError[] errors, ApiEntity entity) {
				// TODO Auto-generated method stub
				_handleErrorsGeneric(errors, entity);
			}

			@Override
			public void entityReceived(ApiEntity entity) {
				// TODO Auto-generated method stub
				app.showToast("Response has been successfully sent !");
				if (response) {
					loadPairList(true);
				}
			}
		});

		responder.sendResponse(response, Long.valueOf(messageID));
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
			}

			final Device currentPair = deviceList.get(position);

			holder.nickname.setText(currentPair.getNickname());
			holder.lastSeen.setText("Last seen "
					+ RelativeDate.getRelativeDate(currentPair.getLastSeen()));
			holder.email.setText(currentPair.getEmail());

			holder.connectButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent fileListIntent = new Intent(MainActivity.this,
							FileListActivity.class);
					fileListIntent.putExtra(FileListActivity.EXTRA_NICKNAME,
							currentPair.getNickname());
					fileListIntent.putExtra(FileListActivity.EXTRA_PATH, "/");
					startActivity(fileListIntent);
				}
			});

			return convertView;
		}

	}

	public void doPairWith(String device) {
		PairWith pairWith = new PairWith(app);
		pairWith.setResponseHandler(new ActivityApiResponseHandlerAbstract(this) {
			@Override
			public void handleError(ApiError[] errors, ApiEntity entity) {
				// TODO Auto-generated method stub
				showProgress(false);
				_handleErrorsGeneric(errors, entity);
			}

			@Override
			public void entityReceived(ApiEntity entity) {
				showProgress(false);
				app.showToast("Pairing request has been sent.");
			}
		});
		pairWith.pairWith(new Device(device, ""));
		DLog.i("Pair Request Sent :" + device);
		// app.showToast("Pair Request Sent (" + device + ")");
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

	public static MainActivity getInstance() {
		return instance;
	}
}
