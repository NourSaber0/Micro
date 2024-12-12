package logic;

public class Register {
	String name;
	String value;
	String tag;

	public Register(String name) {
		this.name = name;
		//place random value
		this.value = String.valueOf((int) (Math.random() * 10000000) / 1000.0);
	}

	public Register(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public Register(Register register) {
		this.name = register.name;
		this.value = register.value;
		this.tag = register.tag;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public boolean getReady() {
		return tag == null;
	}

	public void reset() {
		tag = null;
	}

	public String toString() {
		return name + " " + value + " " + tag;
	}

}
