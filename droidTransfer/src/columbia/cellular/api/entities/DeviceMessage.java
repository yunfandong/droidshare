package columbia.cellular.api.entities;

import org.json.JSONException;
import org.json.JSONObject;

import columbia.cellular.api.service.ApiEntity;
import columbia.cellular.api.service.ApiLog;

public class DeviceMessage extends ApiEntity {

	protected Device sender;
	protected Device receiver;
	protected long messageID;
	protected long inReplyTo;
	protected long timestamp;
	protected String content;
	protected String rpcMethod;
	
	//meta properties of the message
	protected long metaPairingID = 0;
	protected long metaFileID = 0;
	protected String metaFilePath = "";
	protected JSONObject metaFileList = null;
	protected String metaErrorMessage = "";
	
	
	public DeviceMessage(JSONObject messageJson){
		try {
			sender = new Device(messageJson.getJSONObject("sender"));
			receiver = new Device(messageJson.getJSONObject("receiver"));
			messageID =  messageJson.getLong("message_id");
			inReplyTo = messageJson.isNull("in_reply_to") ? 0 :  messageJson.getLong("in_reply_to");
			timestamp = messageJson.getLong("timestamp");
			rpcMethod = messageJson.getString("method");
			content = messageJson.getString("content");
			
			if(messageJson.has("meta") && !messageJson.isNull("meta")){
				JSONObject metaData = messageJson.getJSONObject("meta");
				if(metaData.has("pairing_id")){
					metaPairingID = metaData.getLong("pairing_id");
				}
				
				if(metaData.has("file_id")){
					metaFileID = metaData.getLong("file_id");
				}
				
				if(metaData.has("path")){
					metaFilePath = metaData.getString("path");
				}				
				
				if(metaData.has("file-list")){
					metaFileList = metaData.getJSONObject("file-list");
				}
				
				if(metaData.has("error-message")){
					metaErrorMessage = metaData.getString("error-message");
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			ApiLog.e("JSON Error in message: "+e.getMessage(), e);
		}
	}


	public Device getSender() {
		return sender;
	}


	public Device getReceiver() {
		return receiver;
	}


	public long getMessageID() {
		return messageID;
	}


	public long getInReplyTo() {
		return inReplyTo;
	}


	public long getTimestamp() {
		return timestamp;
	}


	public String getContent() {
		return content;
	}


	public String getRpcMethod() {
		return rpcMethod;
	}


	public long getMetaPairingID() {
		return metaPairingID;
	}


	public long getMetaFileID() {
		return metaFileID;
	}


	public String getMetaFilePath() {
		return metaFilePath;
	}


	public JSONObject getMetaFileList() {
		return metaFileList;
	}


	public String getMetaErrorMessage() {
		return metaErrorMessage;
	}


	@Override
	public String toString() {
		return "DeviceMessage [sender=" + sender + ", receiver=" + receiver
				+ ", messageID=" + messageID + ", inReplyTo=" + inReplyTo
				+ ", timestamp=" + timestamp + ", content=" + content
				+ ", rpcMethod=" + rpcMethod + ", metaPairingID="
				+ metaPairingID + ", metaFileID=" + metaFileID
				+ ", metaFilePath=" + metaFilePath + ", metaFileList="
				+ metaFileList + ", metaErrorMessage=" + metaErrorMessage + "]";
	}
	
	
	
	
}
