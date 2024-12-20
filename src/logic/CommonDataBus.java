package logic;

public class CommonDataBus {
	String tag;
	String value;

	public void broadcast(String tag, String value) {
		this.tag = tag;
		this.value = value;
	}

	public void reset() {
		tag = null;
		value = null;
	}

	public String toString() {
		return tag + " " + value;
	}
}