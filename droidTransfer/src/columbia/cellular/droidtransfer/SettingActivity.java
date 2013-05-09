package columbia.cellular.droidtransfer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import columbia.cellular.file.CallbackBundle;
import columbia.cellular.file.SelectRootDialog;

public class SettingActivity extends Activity {
	DroidApp app;

	TextView uploadTab;
	TextView peersTab;
	String rootPath;

	private int selectPathDialogId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		rootPath = ((DroidApp) getApplication()).getSetting(
				DroidApp.PREF_ROOT_PATH, "");
		// DLog.i("Setup Activity Start  id: " + userId + "  email: " + email);
		app = (DroidApp) getApplication();
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

		if (id == selectPathDialogId) {
			final Dialog dialog = SelectRootDialog.createDialog(id, this,
					"Select Root Path", new CallbackBundle() {
						@Override
						public void callback(Bundle bundle) {
							rootPath = bundle.getString("path");
							setTitle(rootPath);
							updatePath(rootPath);
						}
					}, app.getImageIds(), rootPath);
			return dialog;
		}
		return null;
	}

	private void updatePath(String path) {
		TextView text = (TextView) findViewById(R.id.rootpath);
		text.setText(path);
		((DroidApp) getApplication())
				.saveSetting(DroidApp.PREF_ROOT_PATH, path);
		rootPath = path;
	}


	public void initViews() {
		uploadTab = (TextView) findViewById(R.id.upload_tab1);
		peersTab = (TextView) findViewById(R.id.list_tab1);
		TextView text = (TextView) findViewById(R.id.rootpath);

		text.setText(rootPath);

		// By default we select peers tab
		uploadTab
				.setBackgroundResource(R.drawable.main_tab_selected_background);

		peersTab.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(SettingActivity.this, MainActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});

		findViewById(R.id.selectPathButton).setOnClickListener(
				new OnClickListener() {
					@SuppressWarnings("deprecation")
					@Override
					public void onClick(View arg0) {
						showDialog(selectPathDialogId);
					}
				});

	}

}
