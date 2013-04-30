package columbia.cellular.api.service;

public class ApiError {
	private String errorCode;
	private String errorMessage;
	
	public ApiError(String code, String msg) {
		errorCode = code;
		errorMessage = msg;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	
}
