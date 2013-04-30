package columbia.cellular.api.service;

import java.util.ArrayList;

import org.apache.http.client.methods.HttpPost;

@SuppressWarnings("rawtypes")
public class ApiRequestWrapper {
	
	
	protected ArrayList<ApiParam> params;
	protected ApiRequestListener listener;
	protected String rpcUri;
	protected boolean isMultipart = false;
	

	protected String requestMethod = HttpPost.METHOD_NAME;
	
	public ApiRequestWrapper(String uri){
		rpcUri = uri;
		params = new ArrayList<ApiParam>();
	}
	
	public ApiRequestWrapper(String uri, ArrayList<ApiParam> params){
		this(uri);
		this.params = params;
	}
	
	public ApiRequestWrapper(String uri, ApiRequestListener listener){
		this(uri);
		this.listener = listener; 
	}
	
	public ApiRequestWrapper(String uri, ArrayList<ApiParam> params, ApiRequestListener listener){
		this(uri, params);
		this.listener = listener; 
	}
	public ArrayList<ApiParam> getParams() {
		return params;
	}

	public void setParams(ArrayList<ApiParam> params) {
		this.params = params;
	}

	public ApiRequestListener getListener() {
		return listener;
	}

	public void setListener(ApiRequestListener listener) {
		this.listener = listener;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getRpcUri() {
		return rpcUri;
	}

	public void setRpcUri(String rpcUri) {
		this.rpcUri = rpcUri;
	}

	public boolean isMultipart() {
		return isMultipart;
	}

	public void setMultipart(boolean isMultipart) {
		this.isMultipart = isMultipart;
	}	
	
	public ApiRequestWrapper addParam(ApiParam param){
		params.add(param);
		return this;
	}
	
}
