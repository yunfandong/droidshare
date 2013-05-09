package columbia.cellular.api.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

public abstract class ApiRequestHandler implements ApiRequestListener {

	protected ApiRequestWrapper request;
	protected HttpUriRequest httpUriRequest;
	protected long totalLength;
	protected long totalDone;
	protected Exception lastException;

	@Override
	public void handleException(Exception e) {
		// TODO Auto-generated method stub
		ApiLog.e("Error occured..." + e.getMessage(), e);
		lastException = e;
	}

	@Override
	public ApiResponse handleResponse(HttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		try {
			if (statusCode == HttpStatus.SC_OK) {
				// ok. process message...
				Header header = response.getLastHeader("Content-Type");
				String contentType = header == null ? "" : response
						.getLastHeader("Content-Type").getValue();
				//ApiLog.i("Content-Type: " + contentType);
				return new ApiResponse(getStringFromResponse(response),
						contentType);
			} else {
				return new ApiResponse(getStringFromResponse(response),
						statusCode);
			}
		} catch (Exception e) {
			handleException(e);
			return null;
		}
	}

	protected  String getStringFromResponse(HttpResponse response)
			throws IllegalStateException, IOException {
		String responseString = "";
		if (response != null) {
			HttpEntity responseEntity = response.getEntity();
			InputStream responseStream = null;

			if (responseEntity != null) {
				responseStream = responseEntity.getContent();
				if (responseStream != null) {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(responseStream));
					String responseLine = br.readLine();
					String tempResponseString = "";
					while (responseLine != null) {
						tempResponseString = tempResponseString + responseLine
								+ System.getProperty("line.separator");
						responseLine = br.readLine();
					}
					br.close();
					if (tempResponseString.length() > 0) {
						responseString = tempResponseString;
					}
				}
			}
		}

		return responseString;
	}

	@Override
	public void requestStarting(ApiRequestWrapper request,
			HttpUriRequest httpUriRequest) {
		// TODO Auto-generated method stub
		this.request = request;
		this.httpUriRequest = httpUriRequest;
	}

	@Override
	public void updateProgress(long written, long total){
		this.totalDone = written;
		this.totalLength = total;
	}

	public long getTotalLength() {
		return totalLength;
	}

	public void setTotalLength(long totalLength) {
		this.totalLength = totalLength;
	}

	public long getTotalDone() {
		return totalDone;
	}

	public void setTotalDone(long totalDone) {
		this.totalDone = totalDone;
	}

	public Exception getLastException(){
		return lastException;
	}
	
}