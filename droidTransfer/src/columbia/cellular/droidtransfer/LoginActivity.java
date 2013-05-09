package columbia.cellular.droidtransfer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import columbia.cellular.Utils.DLog;
import columbia.cellular.api.apicalls.ActivityApiResponseHandlerAbstract;
import columbia.cellular.api.apicalls.Register;
import columbia.cellular.api.entities.Device;
import columbia.cellular.api.service.ApiEntity;
import columbia.cellular.api.service.ApiError;

import com.google.android.gcm.GCMRegistrar;

public class LoginActivity extends Activity {

	Button button;
	EditText nickName;
	EditText Email;

	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	protected String registrationId;
	public DroidApp app;

	AsyncTask<Void, Void, Void> mRegisterTask;

	protected static LoginActivity instance;

	public void registrationReceived(String registrationId) {
		this.registrationId = registrationId;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.login);

		button = (Button) findViewById(R.id.sign_in_button);
		nickName = (EditText) findViewById(R.id.nickname);
		Email = (EditText) findViewById(R.id.email);

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		app = (DroidApp) getApplication();

		registrationId = app.deviceProperty(DroidApp.PREF_GCM_REGISTRATION_ID, "");
		if (registrationId.length() < 1) {
			//DLog.i("Starting to register GCM...");
			initGCM();
		}


		if (((DroidApp) getApplication()).isRegistered()) {
			startActivity(new Intent(LoginActivity.this, MainActivity.class));
			finish();
		} else {
			registerDevice();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void initGCM() {
		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);
		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);
		String regId = GCMRegistrar.getRegistrationId(this);
		if (!regId.equals("")) {
			DLog.i("registration ID: " + regId);
			registrationId = regId;
		}

		if (regId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(this, DroidApp.SENDER_ID);
			regId = GCMRegistrar.getRegistrationId(this);
			registrationId = regId;
			DLog.i("->registration ID: " + regId);
			// DLog.i("ID length:" + regId.length());
		}

	}

	public void registerDevice() {
		// Show a progress spinner, and kick off a background task to
		// perform the user login attempt.
		DLog.i("attempting to log in ");

		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (nickName.getText().toString().isEmpty()) {
					Toast.makeText(getApplicationContext(),
							"Please Input NickName ^_^", Toast.LENGTH_SHORT)
							.show();
				} else if (Email.getText().toString().isEmpty()) {
					Toast.makeText(getApplicationContext(),
							"Please Input Email ^_^", Toast.LENGTH_SHORT)
							.show();
				}

				else {
					TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					final String imei = telephonyManager.getDeviceId();
					DLog.i("IMEI: " + imei);

					mLoginStatusMessageView
							.setText(R.string.login_progress_signing_in);
					showProgress(true);

					Device device = new Device(nickName.getText().toString(),
							Email.getText().toString(), imei, registrationId);

					DLog.i("Registering Device: "
							+ nickName.getText().toString() + " "
							+ registrationId);
					Register register = new Register((DroidApp) getApplication());
					register.setResponseHandler(new LoginResponseHandler(LoginActivity.this));
					register.registerDevice(device);
				}

			}
		});

	}

	protected void showProgress(final boolean show) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}



	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (((DroidApp) getApplication()).isRegistered()) {
			DLog.i("Registered!");
			startActivity(new Intent(LoginActivity.this, MainActivity.class));
		}
		
		
	}
	
	public class LoginResponseHandler extends ActivityApiResponseHandlerAbstract{

		public LoginResponseHandler(Activity activity) {
			super(activity);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void entityReceived(ApiEntity entity) {
			// TODO Auto-generated method stub
			Device device = (Device) entity;
			LoginActivity.this.app.showToastLong("Device registered : token: " + device.getToken());
			startActivity(new Intent(LoginActivity.this, MainActivity.class));
			finish();
			showProgress(false);
		}

		@Override
		public void handleError(ApiError[] errors, ApiEntity entity) {
			// TODO Auto-generated method stub
			showProgress(false);
			_handleErrorsGeneric(errors, entity);
		}
		
	}
	
}
