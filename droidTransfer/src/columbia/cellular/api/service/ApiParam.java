package columbia.cellular.api.service;

public class ApiParam<E> {
	public static final String TYPE_INT = "int";
	public static final String TYPE_BIGINT = "bigint";
	public static final String TYPE_FILE = "file";
	public static final String TYPE_BOOL = "bool";
	public static final String TYPE_STRING = "string";
	public static final String TYPE_JSON = "json";
	public static final String TYPE_EMAIL = "email";
	
	
	private String name;
	private String type;
	private E value;
	
	public ApiParam(String name, E value) {
		this(name, value, TYPE_STRING);
	}

	public ApiParam(String name, E value, String type) {
		this.value = value;
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public E getValue() {
		return value;
	}

	public void setValue(E value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	
	
}
