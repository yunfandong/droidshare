package columbia.cellular.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import columbia.cellular.json.JSONArray;
import columbia.cellular.json.JSONObject;

import android.os.Handler;


public class UrlUtils  {
	
	//input serverurl here
	static String ServerURL = "";
	static String registerRT = "register"; //decice_
	static String pairRT="pair-with";
	
	
	
	// Login Step: get Peer List
	
public	static List<String> getPeerList(final String id,final String ip){
		
		final List<String> list = new ArrayList<String>();
		
		/**use an independent thread to fetch json */
		
		final Handler handler = new Handler();
		final Runnable r = new Runnable()
		{
		    public void run() 
		    {
		
		    	
				try{		
					//build up query key words
					
					
					//build connection and fetch results
					URL login = new URL(ServerURL+"myid="+id+"&myip="+ip);	
							
					URLConnection connection = login.openConnection();
					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					
					
					
					String inputLine;
					StringBuffer output=new StringBuffer("");
					
					while ((inputLine = in.readLine()) != null)
					output.append((inputLine));
					
					in.close();
					
					String json = output.toString();
					
					//peer info
					String peer_id;
					
					//build json objcet
					JSONObject myjson = new JSONObject(json);
					
			       /* **************************************************
			        * typical login json response:
			        * 
			        * {
			        * 
			        * "peers": [
			        * {
			        * "id":"Jack",
			        * "state":"online"
			        * },
			        * {
			        * "id":"Jones",
			        * "state":"online"
			        * }
			        * ...
			        * ]
			        * 
			        * }
			        * 
			        * ****************************************************/
					
					JSONArray the_json_array = myjson.getJSONArray("peers");
					
					int size = the_json_array.length();
					
					for(int i=0 ; i<size; i++){
						
						JSONObject object = the_json_array.getJSONObject(i);
						
						peer_id = object.get("id").toString();

						list.add(peer_id);
						
					}
					
				} catch (Exception e){}
				
		    }
		};

		handler.postDelayed(r, 50);
		
		
		
		return list;
	}

public static List<String> getPeerFiles(String peername){
	List<String> list = new ArrayList<String>();
	
	return list;
}

}
