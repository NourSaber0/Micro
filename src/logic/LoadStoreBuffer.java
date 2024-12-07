package logic;

public class LoadStoreBuffer {
	String address;
	String value;
	String tag;
	String Q;
	InstructionType operation;
	boolean busy;

	int executionStartCycle = 0, executionEndCycle = 0;

	public LoadStoreBuffer(String tag) {
		this.busy = false;
		this.tag= tag;
	}
	public void reset() {
		busy = false;
		operation = null;
		address = value = tag = Q = null;
		executionStartCycle = executionEndCycle = 0;
	}
}
