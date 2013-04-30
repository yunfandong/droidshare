package columbia.cellular.api.service;

import java.io.IOException;
import java.io.OutputStream;
/**
 * 
 * 
 * @author intelWorX
 * Adapted from code on StackOverlow for file upload progress
 * http://stackoverflow.com/questions/7057342/how-to-get-a-progress-bar-for-a-file-upload-with-apache-httpclient-4
 *
 */

public class OutputStreamProgress extends OutputStream {

    private final OutputStream outstream;
    private long bytesWritten=0;
    private long contentLength = 0;
	private final ApiRequestListener requestListener;
	
    public OutputStreamProgress(OutputStream outstream, ApiRequestListener writeListener) {
        this.outstream = outstream;
        this.requestListener = writeListener;
    }

    @Override
    public void write(int b) throws IOException {
        outstream.write(b);
        bytesWritten++;
        requestListener.updateProgress(bytesWritten, contentLength);
    }

    @Override
    public void write(byte[] b) throws IOException {
        outstream.write(b);
        bytesWritten += b.length;
        requestListener.updateProgress(bytesWritten, contentLength);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outstream.write(b, off, len);
        bytesWritten += len;
        requestListener.updateProgress(bytesWritten, contentLength);
    }

    @Override
    public void flush() throws IOException {
        outstream.flush();
    }

    @Override
    public void close() throws IOException {
        outstream.close();
    }
    
    public void setContentLength(long length) {
		this.contentLength = length;
	}
}
