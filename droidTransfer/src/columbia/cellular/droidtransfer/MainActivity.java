package columbia.cellular.droidtransfer;






import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.google.android.gcm.GCMRegistrar;

import columbia.cellular.api.apicalls.PairList;
import columbia.cellular.api.apicalls.PairWith;
import columbia.cellular.api.entities.Device;
import columbia.cellular.api.entities.DeviceList;
import columbia.cellular.api.entities.DeviceMessage;
import columbia.cellular.api.entities.FtDroidActivity;
import columbia.cellular.api.service.ApiEntity;
import columbia.cellular.api.service.ApiError;
import columbia.cellular.api.service.ApiLog;
import columbia.cellular.droidtransfer.droidService.droidServiceBinder;
import columbia.cellular.Utils.DLog;
import columbia.cellular.Utils.ServerUtilities;
import columbia.cellular.Utils.UrlUtils;
import columbia.cellular.file.CallbackBundle;
import columbia.cellular.file.ListFileDialog;
import columbia.cellular.file.OpenFileDialog;
import columbia.cellular.json.JSONException;
import columbia.cellular.json.JSONObject;
import columbia.cellular.droidtransfer.GCMIntentService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FtDroidActivity {
	
	List<Map<String,Object>> peerList ;
	private TextView uploadTab;
	private TextView peersTab;
	private String userId;
	private String email;
	private String imei;
	
	private String testing="pairlist";
	
	private String rootPath;
	public droidApp app;
	public droidService myService;
	AsyncTask<Void, Void, Void> mRegisterTask;
	
	private Map<String, Object> map;
	static private int listfileDialogId = 0;
	MyAdapter adapter;
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		app = (droidApp)getApplication();
		updateUserInfo();
		
		//init 
		initViews();
		initMainHandler();
//		initGCM();
		
		Device regDev = getRegisteredDevice();
		app.showToastLong("Welcome  "+regDev.getNickname()+" , email : "+regDev.getEmail());
		
		DLog.i("Device info:"+regDev.toString());
		
		
	
		
		
		//get peerList
		peerList=initList();
		//peerList = UrlUtils.getPeerList(userId, ip);
		
		//set adapter
		adapter = new MyAdapter(this);
		setListAdapter(adapter);
		
		testPairList();
		   
		
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
		bindMyService();		
	     		
	}
	
	
	
	/***************************************************************
	 * Service Connection,Handler & BroadcastReceiver initialization part
	 *                                              yd2238
	 ***************************************************************  */
	
	//register for google cloud messaging
	
	private void initGCM(){
		
	     // Make sure the device has the proper dependencies.
	     GCMRegistrar.checkDevice(this);
	     // Make sure the manifest was properly set - comment out this line
	    // while developing the app, then uncomment it when it's ready.
	     GCMRegistrar.checkManifest(this);
	     
	     final String regId = GCMRegistrar.getRegistrationId(this);
	     DLog.i("registration ID: "+regId);
	     DLog.i("ID length:"+regId.length());
	     
	     if (regId.equals("")) {
	            // Automatically registers application on startup.
	            GCMRegistrar.register(this, app.SENDER_ID);
	        } else {
	            // Device is already registered on GCM, check server.
	            if (GCMRegistrar.isRegisteredOnServer(this)) {
	                // Skips registration.
	               // mDisplay.append(getString(R.string.already_registered) + "\n");
	            } else {
	                // Try to register again, but not in the UI thread.
	                // It's also necessary to cancel the thread onDestroy(),
	                // hence the use of AsyncTask instead of a raw thread.
	                final Context context = this;
	                mRegisterTask = new AsyncTask<Void, Void, Void>() {

	                    @Override
	                    protected Void doInBackground(Void... params) {
	                        boolean registered =ServerUtilities.register(context, regId);
	                    //	boolean registered=true;
	                        
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
	private static void toBroadcast(Context context, String action,
			String jsonData) {
		Intent intent = new Intent(action);
		intent.putExtra("json", jsonData);
		context.sendBroadcast(intent);
	}
	
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
			
			Intent serviceIntent = new Intent(this,droidService.class);
			bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
			app.setServiceState(true);
		
	}
	
	public void initMainHandler(){
		app.setMainHandler(new MainHandler());
	}
	
	private class MainHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
		if (msg.what == app.RESPONSE_MESSAGE) {
				Bundle bundle = msg.getData();
				String data = bundle.getString("data");
				DLog.i("MainActivity Receives Response :"+data);
			} 
	
		}

	}
	
	
	
	
	/***************************************************************
	 *Basic (views, userinfos,dialogs) initialization part
	 *                                              yd2238
	 ***************************************************************  */
	
	
	
	protected Dialog onCreateDialog(int id) {
		
		if(id==listfileDialogId){
	Map<String, Integer> images = getImageIds();
			
			final Dialog dialog = OpenFileDialog.createDialog(id, this, "Select File", new CallbackBundle() {
				@Override
				public void callback(Bundle bundle) {
					
					String filepath = bundle.getString("path");
					String filename = bundle.getString("name");
					
					setTitle(filepath);
					
					DLog.i("Selected File:"+filename+"   path: "+filepath);
				}
			}, 
			"",
			images);
			return dialog;
		}
		return null;
	}
	
	
	
	public void updateUserInfo(){
		
		Intent i = getIntent();
		if(!app.whetherNameSet()){
	    userId = i.getStringExtra("nickname");
	    email = i.getStringExtra("email");
	    imei = i.getStringExtra("imei");
	    rootPath=i.getStringExtra("rootpath");
	    
	    if(rootPath==null) rootPath="";
	    
	    app.setNickName(userId);
	    app.setEmail(email);
	    app.setIMEI(imei);
	    app.setRootPath(rootPath);
	    
	    app.SetNameFlag(true);
	    
	    DLog.i("Update of User Info!");
		}
		else {
		userId = app.getNickName();
		email = app.getEmail();
		rootPath = app.getRootPath();
		}
	    
		DLog.i("Main Activity Start  nickname: "+userId+"  email: "+email+" rootpath: "+rootPath);
		
	}
	
	
	
	/*Temporary: init a faked peer list to debug*/
	public List<Map<String,Object>> initList(){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		Map<String,Object> map;
		
		map = new HashMap<String,Object>();
		map.put("userId", "Joseph");
		map.put("state", "online");
		
		list.add(map);
		
		map = new HashMap<String,Object>();
		map.put("userId", "Yunfan");
		map.put("state", "online");
		
		list.add(map);
		
		map = new HashMap<String,Object>();
		map.put("userId", "Jeorge");
		map.put("state", "offline");
		
		list.add(map);
		
		map = new HashMap<String,Object>();
		map.put("userId", "Jeorgia");
		map.put("state", "offline");
		
		list.add(map);
		
		map = new HashMap<String,Object>();
		map.put("userId", "Bush");
		map.put("state", "online");
		
		list.add(map);
		
		
		map = new HashMap<String,Object>();
		map.put("userId", "Bush");
		map.put("state", "online");
		
		list.add(map);
		
		map = new HashMap<String,Object>();
		map.put("userId", "Bush");
		map.put("state", "online");
		
		list.add(map);
		
		map = new HashMap<String,Object>();
		map.put("userId", "Bush");
		map.put("state", "offline");
		
		list.add(map);

		
		
		return list;
	}
	
	public List<String> initFiles(){
		List<String> list = new ArrayList<String>();
		list.add("test.ppt");
		list.add("test1.pdf");
		list.add("test2.html");
		list.add("test3.111");
		list.add("test4.txt");
		return list;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		menu.add("add device").setIcon(R.drawable.maintab_users_selected);
		menu.add("setting").setIcon(R.drawable.maintab_setting_selected);
		
		return true;
	}
	public boolean onPrepareOptionsMenu(Menu menu){
     
        return true;
    }
     
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getTitle().equals("add device")) showAddDeviceInfo();
        return true;
    }
	
	
	/***************************************************************
	 *Functions & Listview Adaptation part
	 *                                              yd2238
	 ***************************************************************  */
	
	private void initViews(){
		
		uploadTab = (TextView)findViewById(R.id.upload_tab);
		peersTab  = (TextView)findViewById(R.id.list_tab);
		
		//By default we select peers tab
		peersTab.setBackgroundResource(R.drawable.main_tab_selected_background);
     //   peersTab.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.maintab_users_selected) , null, null);

		
		uploadTab.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent(MainActivity.this,SettingActivity.class);
            	
            	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	startActivity(i);
            }
        });
		
		
		peersTab.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
            	toBroadcast(MainActivity.this,app.ACTION_SEND_REQUEST,app.getLoginJSON());
            	GCMIntentService.generateNotification(MainActivity.this, "trial");
            	
            }
        });
		
		
		
		//allow MultiThreaded
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
	}
	
	
	

	
	
	
	public final class ViewHolder{
        public TextView userId;
        public TextView userState;
        public ImageView state;
        public Button connectButton;
    }
	
	public void showInfo(){
        new AlertDialog.Builder(this)
        .setTitle("Warning")
        .setMessage("Currently no paired devices")
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })
        .setNegativeButton("Add Device", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              DLog.i("Add Device!!");
              showAddDeviceInfo();
            }
        })
        .show();
         
    }
	
	public void showAddDeviceInfo(){
		
		
		final Dialog dialog = new Dialog(this);
		dialog.setTitle("Add a device");
		
		dialog.setContentView(R.layout.pair_dialog);
		final EditText nickname = (EditText)dialog.findViewById(R.id.pair_nickname);
		Button pair = (Button)dialog.findViewById(R.id.pair_button);
		
		pair.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	
	            	if (nickname.getText().equals("")){
	            	app.showToast("please input a nickname ^_^");
	            	}
	            	else{
	            	testPairWith(nickname.getText().toString());
	            	dialog.dismiss();
	            	}
	            }});
	            
		dialog.show();
        
        
         
    }
	
	private Map<String,Integer> getImageIds() {
		
		Map<String, Integer> images = new HashMap<String, Integer>();
		// file type adapter 
		images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);	// root
		images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);	//back
		images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);	//folder
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
	
	
	public class MyAdapter extends BaseAdapter{
		 
        private LayoutInflater mInflater;
         
         
        public MyAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return peerList.size();
        }
 
        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }
 
        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }
 
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
        	
        	final String id ;
        	final String state;
             
            ViewHolder holder = null;
            
            if (convertView == null) {
                 
                holder=new ViewHolder();  
                 
                convertView = mInflater.inflate(R.layout.listcell, null);
                
                holder.userId = (TextView)convertView.findViewById(R.id.userId);
                holder.userState = (TextView)convertView.findViewById(R.id.userState);
                holder.connectButton = (Button)convertView.findViewById(R.id.connectButton);
                holder.state = (ImageView)convertView.findViewById(R.id.state);
                
                convertView.setTag(holder);
                 
            }else {
                 
                holder = (ViewHolder)convertView.getTag();
            }
             
             id = peerList.get(position).get("userId").toString();
             state = peerList.get(position).get("state").toString();
             
	         holder.userId.setText(id);
             holder.userState.setText(state);
             
             if(!state.equals("online")){
            	 holder.state.setImageResource(R.drawable.circle_red);
             }
                        
            holder.connectButton.setOnClickListener(new View.OnClickListener() {
                 
                @Override
                public void onClick(View v) {
                //    showInfo(peerList.get(position)); 
                	DLog.i("Connect to "+peerList.get(position));
                	if(state.equals("offline")){
                		showInfo();
                	}
                	else showDialog(listfileDialogId);
                }
            });
      
             
            return convertView;
        }
         
    }


	public void testPairList(){
		testing = "pairlist";
		(new PairList(this)).getPairList();
	}
	
	public void testPairWith(String device){
		testing = "pair-with";
		PairWith pairWith = new PairWith(this);
		pairWith.pairWith(new Device(device,""));
		DLog.i("Pair Request Sent :"+device);
		app.showToast("Pair Request Sent ("+device+")");
	}


	@Override
	public void entityReceived(ApiEntity entity) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
				if(testing.equals("register")){
					// TODO Auto-generated method stub
					Device device = (Device) entity;
					Toast.makeText(this, "Device registered : token: " + device.getToken(),
							Toast.LENGTH_LONG).show();
					
					}else if(testing.equals("pair-with") || testing.equals("pair-response") || testing.equals("pair-delete")){
						DeviceMessage message = (DeviceMessage) entity;
						Toast.makeText(this, "Pairing Message ID: " + message.getMessageID()
								+"Pairing ID " 
								+ message.getMetaPairingID()
								+"\n Sender: " 
								+ message.getSender(),
								Toast.LENGTH_LONG).show();
						ApiLog.i("Message: "+message.toString());
					}
					else if(testing.equals("pairlist")){
						DeviceList deviceList = (DeviceList) entity;
						
						if(deviceList.size()==0){
							showInfo();
						}
						else {
							
						}
						
						ApiLog.i("Message: "+deviceList.toString());	
						
						
					}else{
						ApiLog.w("Testing unknown: ["+testing+"]");
					}
		
	}


	@Override
	public void handleError(ApiError[] errors, ApiEntity entity) {
		// TODO Auto-generated method stub
		
	}

}
