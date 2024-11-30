package logic;

public class Register {
	String name;
	String value;
	String tag;
	boolean Ready;


	public Register(String name) {
		this.name = name;
		//place random value
		this.value = toString().valueOf((int)(Math.random()*100));
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	public String getName() {
		return name;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getTag() {
		return tag;
	}
	public void setReady(boolean Ready) {
		this.Ready = Ready;
	}
	public boolean getReady() {
		return Ready;
	}

}
