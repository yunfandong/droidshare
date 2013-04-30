package columbia.cellular.api.service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;

public class ApiServerConnector {
	
	private static ApiServerConnector instance;
	public static String apiEndPoint = "http://ft.orilogbon.me/rpc";

	public static final String REQUEST_TYPE_FORM = "application/x-www-form-urlencoded";
	public static final String REQUEST_TYPE_MULTIPART = "multipart/form-data";
	
	public static final String API_URL_REGIGSTER = "/register";
	public static final String API_URL_PAIR_WITH = "/pair-with";
	public static final String API_URL_PAIR_RESPONSE = "/pair-response";
	public static final String API_URL_PAIR_DELETE = "/pair-delete";
	public static final String API_URL_PAIR_LIST = "/pair-list";
	public static final String API_URL_GET_FILE_LIST = "/get-file-list";
	public static final String API_URL_SEND_FILE_LIST = "/send-file-list";
	public static final String API_URL_GET_FILE = "/get-file";
	public static final String API_URL_SEND_FILE = "/send-file";
	public static final String API_URL_DOWNLOAD_FILE = "/download-file";
	public static final String API_URL_MESSAGES = "/messages";
	public static final String API_URL_GCM_UPDATE = "/gcm-update";
	
	
	
	private ApiServerConnector(){
	}
	
	public ApiResponse makeHttpRequest(ApiRequestWrapper request){
		HttpUriRequest httpUriRequest;
		if(request.getRequestMethod().equals(HttpPost.METHOD_NAME)){
			httpUriRequest  = getHttpPostRequest(request);
		}else{
			httpUriRequest  = getHttpGetRequest(request);
		}
		
		try {
			httpUriRequest.addHeader("xAuth", ApiAuthenticator.getPayload());
			
		} catch (UnsupportedEncodingException e) {			
		}		
		
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1); 
		try {
			if(request.getListener() != null){
				request.getListener().requestStarting(request, httpUriRequest);
				return client.execute(httpUriRequest, request.getListener());
			}else{
				client.execute(httpUriRequest);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(request.getListener() != null){
				request.getListener().handleException(e);
			}
		} finally{
			client.getConnectionManager().shutdown();
		}
		return null;
	}
	
	
	@SuppressWarnings("rawtypes")
	private HttpUriRequest getHttpPostRequest(ApiRequestWrapper request){
		HttpPost postRequest = new HttpPost(getNormalizedUrl(request.getRpcUri()));		
		if(request.getParams().size() > 0){
			//formparams.add(new BasicNameValuePair("param1", "value1"));
			if(!request.isMultipart()){
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				postRequest.addHeader("Content-Type", REQUEST_TYPE_FORM);
				for(ApiParam param : request.getParams()){
					params.add(new BasicNameValuePair(param.getName(), param.getValue().toString()));
				}
				try {
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
					postRequest.setEntity(entity);
				} catch (UnsupportedEncodingException e) {
					ApiLog.e("error occured", e);
				}
			}else{
				postRequest.addHeader("Content-Type", REQUEST_TYPE_MULTIPART);
		        try{
		        	MultipartEntity multiPartEntity;
		        	if(request.getListener() != null){
		        		multiPartEntity = new MultipartEntityWithProgress (HttpMultipartMode.BROWSER_COMPATIBLE, request.getListener()) ;
		        	}else{
		        		multiPartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		        	}
		            
					for(ApiParam param : request.getParams()){
						if(param.getType().equals(ApiParam.TYPE_FILE)){
							FileBody fileBody = new FileBody((File) param.getValue(), "application/octect-stream") ;
				            multiPartEntity.addPart(param.getName(), fileBody) ;
						}else{
							multiPartEntity.addPart(param.getName(), new StringBody(param.getValue().toString())) ;
						}
					}
		            //The usual form parameters can be added this way
		            postRequest.setEntity(multiPartEntity) ;
		            postRequest.setHeader("Content-Type", REQUEST_TYPE_MULTIPART);
		        }catch (UnsupportedEncodingException ex){
		            ApiLog.e("Error occured while uploading file: "+ex.getMessage(), ex);
		        }
			}
		}
		return postRequest;
	}
	
	
	
	@SuppressWarnings("rawtypes")
	private HttpUriRequest getHttpGetRequest(ApiRequestWrapper request){
		String rpcUrl = getNormalizedUrl(request.getRpcUri());
		HttpGet getRequest = new HttpGet(rpcUrl);
		try {
			//ApiLog.i("RPC URL: "+rpcUrl);
			URIBuilder builder = new URIBuilder(rpcUrl);
			for(ApiParam param : request.getParams()){
				builder.addParameter(param.getName(), param.getValue().toString());
			}
			getRequest.setURI(builder.build());
		} catch (URISyntaxException e) {
			ApiLog.w("Uri error: "+e.getMessage());
		}
		
		return getRequest;
	}	
	
	
	
	public static ApiServerConnector getInstance(){
		if(instance == null){
			instance = new ApiServerConnector();
		}
		return instance;
	}

	public static String getApiEndPoint() {
		return apiEndPoint;
	}

	public static void setApiEndPoint(String apiEndPoint) {
		ApiServerConnector.apiEndPoint = apiEndPoint;
	}

	public String getNormalizedUrl(String uri){
		if(uri.indexOf("http://") == 0 || uri.indexOf("https://") == 0){
			return uri;
		}
		
		String lrPattern = "(/$|^/)";
		return  apiEndPoint.replaceAll(lrPattern, "") + "/"+ uri.replaceAll(lrPattern, "") +"/";
	}
	
}
