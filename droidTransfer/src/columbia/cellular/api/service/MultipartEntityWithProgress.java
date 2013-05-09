package columbia.cellular.api.service;
/**
 * 
 * 
 * @author intelWorX
 * Adapted from code on StackOverlow for file upload progress
 * http://stackoverflow.com/questions/7057342/how-to-get-a-progress-bar-for-a-file-upload-with-apache-httpclient-4
 *
 */

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

public class MultipartEntityWithProgress extends MultipartEntity {
    private OutputStreamProgress outstream;
	private ApiRequestListener requestListener;

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        this.outstream = new OutputStreamProgress(outstream, requestListener);
        this.outstream.setContentLength(getContentLength());
        super.writeTo(this.outstream);
    }

    public MultipartEntityWithProgress(ApiRequestListener writeListener){
        super();
        this.requestListener = writeListener;
    }
    
    public MultipartEntityWithProgress(HttpMultipartMode mode, ApiRequestListener writeListener){
        super(mode);
        this.requestListener = writeListener;
    }
    public MultipartEntityWithProgress(HttpMultipartMode mode, String boundary, Charset charset, ApiRequestListener writeListener){
        super(mode, boundary, charset);
        this.requestListener = writeListener;
    }
    
}
