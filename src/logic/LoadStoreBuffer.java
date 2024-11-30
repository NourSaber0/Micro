package logic;

public class LoadStoreBuffer {
	String address;
	String value;
	boolean busy;

	public LoadStoreBuffer() {
		this.busy = false;
	}
}