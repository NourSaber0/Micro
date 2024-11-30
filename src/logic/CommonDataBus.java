package logic;

public class CommonDataBus {
	String tag;
	String value;

	public void broadcast(String tag, String value) {
		this.tag = tag;
		this.value = value;
	}
}