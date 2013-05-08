package columbia.cellular.droidtransfer;


import columbia.cellular.Utils.DLog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;



public class droidService extends Service{
	private DroidApp app;
	private droidServiceBinder mBinder;
	
	
	public class droidServiceBinder extends Binder{
		public droidService getService(){
			return droidService.this;
		}
	}
	
	@Override
	public void onCreate() {
		DLog.i("Service is onCreate");
//		Toast.makeText(getApplicationContext(), "HAHAHAworking!!!", Toast.LENGTH_LONG).show();
		
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO bindService出来的服务不会调用这个
		super.onStart(intent, startId);
	}
	
	@Override
	public void onDestroy() {
		DLog.i("Service Stopped");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		app = (DroidApp) getApplication();

		if (app == null) {
			stopSelf();
		} else {
			//register broadcast receiver
			doRegisterReceiver();
			
		}
		return mBinder;
	}
	
	public void doRegisterReceiver(){
		IntentFilter filter = new IntentFilter(app.ACTION_SEND_REQUEST);
		registerReceiver(sendMessageReceiver, filter);
		
	}
	
	
	private BroadcastReceiver sendMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(app.ACTION_SEND_REQUEST)) {
				
				String jsonString = intent.getStringExtra("json");
				// send the request
				DLog.i("send request:  "+ jsonString);
				
				//response 
				String response = "helloworld response";
				sendResponse(response , app.RESPONSE_MESSAGE);
				
			}
		}
	};
	
	private void sendResponse(String object, int type, int... args) {
		
		Bundle bundle = new Bundle();
		bundle.putString("data", object);
//		bundle.putParcelable(key, value);
		
		Message msg = new Message();
		msg.setData(bundle);
		msg.what = type;
		if (args.length != 0) {
			msg.arg1 = args[0];
		}
		app.sendMessageToHandler(msg);
		
	}

}
