package logic;

public class Register {
	String name;
	String value;
	String tag;

	public Register(String name) {
		this.name = name;
		//place random value
		this.value = String.valueOf((int)(Math.random() * 10000000) / 1000.0);
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

	public boolean getReady() {
		return tag == null;
	}

	public String toString() {
		return name + " " + value + " " + tag;
	}

}
