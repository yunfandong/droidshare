package columbia.cellular.api.apicalls;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import columbia.cellular.Utils.DLog;
import columbia.cellular.api.service.ApiLog;
import columbia.cellular.api.service.ApiResponse;


public class FileDownloadRequestListerner extends DefaultRequestListener {
	
	protected File outFile;
	protected boolean cancel = false;
	
	public FileDownloadRequestListerner(File outFile) {
		this.outFile = outFile;
	}
	
	@Override
	public ApiResponse handleResponse(HttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		try {
			if (statusCode == HttpStatus.SC_OK) {
				Header header = response.getLastHeader("Content-Type");
				String contentType = header == null ? "" : response
						.getLastHeader("Content-Type").getValue();
				ApiLog.i("Content-Type: " + contentType);
				BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
				long contentLength = response.getEntity().getContentLength();
				long written = 0;
				int inByte;
				while((inByte = bis.read()) != -1 && !isCancel()) {
					bos.write(inByte);
					updateProgress(++written, contentLength);
				}
				bis.close();
				bos.close();
				DLog.i("Download completed in listener");
				return new ApiResponse("{\"success\": true}",contentType);
			} else {
				String rawResponse = getStringFromResponse(response);
				DLog.w("Raw response: "+rawResponse);
				return new ApiResponse(rawResponse,statusCode);
			}
		} catch (Exception e) {
			handleException(e);
			return null;
		}
	}

	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}
	
	
}
