package columbia.cellular.droidtransfer;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.google.android.gcm.GCMRegistrar;

import columbia.cellular.Utils.DLog;
import columbia.cellular.Utils.IpUtils;
import columbia.cellular.Utils.ServerUtilities;
import columbia.cellular.api.apicalls.PairList;
import columbia.cellular.api.apicalls.Register;
import columbia.cellular.api.entities.Device;
import columbia.cellular.api.entities.DeviceList;
import columbia.cellular.api.entities.DeviceMessage;
import columbia.cellular.api.entities.FtDroidActivity;
import columbia.cellular.api.service.ApiEntity;
import columbia.cellular.api.service.ApiError;
import columbia.cellular.api.service.ApiLog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends FtDroidActivity {

	Button button;
	EditText nickName;
	EditText Email;

	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	private String testing = "register";

	private String registrationId;
	public droidApp app;

	AsyncTask<Void, Void, Void> mRegisterTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		button = (Button) findViewById(R.id.sign_in_button);
		nickName = (EditText) findViewById(R.id.nickname);
		Email = (EditText) findViewById(R.id.email);

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		app = (droidApp) getApplication();

		final String ip = IpUtils.getIPAddress(false);
		; // true: IPv4 false:IPv6
		initGCM();

		if (isRegistered()) {
			// Toast.makeText(this,
			// "Device is already registered"+getRegisteredDevice().toString(),
			// Toast.LENGTH_LONG).show();
			// testPairWith();
			// testPairResponse();
			// testPairDelete();
			// testPairList();
			DLog.i("Registered!");
			startActivity(new Intent(LoginActivity.this, MainActivity.class));

		} else {
			attemptLogin();
			// testPairList();
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
			DLog.i("ID length:" + regId.length());
			registrationId = regId;
		}

		if (regId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(this, app.SENDER_ID);
			regId = GCMRegistrar.getRegistrationId(this);
			registrationId = regId;
			DLog.i("registration ID: " + regId);
			DLog.i("ID length:" + regId.length());
		} else {
			// Device is already registered on GCM, check server.
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.
				// mDisplay.append(getString(R.string.already_registered) +
				// "\n");
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				final Context context = this;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						// boolean registered =ServerUtilities.register(context,
						// regId);

						boolean registered = true;

						// At this point all attempts to register with the app
						// server failed, so we need to unregister the device
						// from GCM - the app will try to register again when
						// it is restarted. Note that GCM will send an
						// unregistered callback upon completion, but
						// GCMIntentService.onUnregistered() will ignore it.
						if (!registered) {
							GCMRegistrar.unregister(context);
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}

				};
				mRegisterTask.execute(null, null, null);
			}
		}

	}

	public void attemptLogin() {
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
					Register register = new Register(LoginActivity.this);

					register.registerDevice(device);

					/*
					 * startActivity(new
					 * Intent(LoginActivity.this,MainActivity.class)
					 * .putExtra("nickname",nickName.getText().toString())
					 * .putExtra("email",Email.getText().toString())
					 * .putExtra("imei", imei) );
					 */

				}

			}
		});

	}

	public void testPairList() {
		testing = "pairlist";
		(new PairList(this)).getPairList();
	}

	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
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
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public void entityReceived(ApiEntity entity) {
		// TODO Auto-generated method stub
		if (testing.equals("register")) {
			// TODO Auto-generated method stub
			Device device = (Device) entity;
			Toast.makeText(this,
					"Device registered : token: " + device.getToken(),
					Toast.LENGTH_LONG).show();

			startActivity(new Intent(LoginActivity.this, MainActivity.class));

			showProgress(false);
		} else if (testing.equals("pair-with")
				|| testing.equals("pair-response")
				|| testing.equals("pair-delete")) {
			DeviceMessage message = (DeviceMessage) entity;
			Toast.makeText(
					this,
					"Pairing Message ID: " + message.getMessageID()
							+ "Pairing ID " + message.getMetaPairingID()
							+ "\n Sender: " + message.getSender(),
					Toast.LENGTH_LONG).show();
			ApiLog.i("Message: " + message.toString());
		} else if (testing.equals("pairlist")) {
			DeviceList deviceList = (DeviceList) entity;

			Toast.makeText(this, "Successful", Toast.LENGTH_LONG).show();
			ApiLog.i("Message: " + deviceList.toString());
		} else {
			ApiLog.w("Testing unknown: [" + testing + "]");
		}

	}

	@Override
	public void handleError(ApiError[] errors, ApiEntity entity) {
		// TODO Auto-generated method stub
		showProgress(false);
		if (errors != null) {
			String errorMessages = "";
			for (ApiError e : errors) {
				errorMessages += "\n" + e.getErrorMessage();
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(errorMessages).setTitle("Errors Occured");
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

						}
					});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		// Toast.makeText(this, "An exception occured",
		// Toast.LENGTH_LONG).show();
	}

}
