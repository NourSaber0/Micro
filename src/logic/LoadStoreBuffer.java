package logic;

public class LoadStoreBuffer {
	String address;
	String value;
	String tag;
	String q;
	InstructionType operation;
	boolean busy;

	int executionStartCycle = 0;
	int executionEndCycle = 0;

	public LoadStoreBuffer(String tag) {
		this.busy = false;
		this.tag = tag;
	}

	public void reset() {
		busy = false;
		operation = null;
		address = value = tag = q = null;
		executionStartCycle = executionEndCycle = 0;
	}

	public String toString() {
		return tag + " " + operation + " " + address + " " + value + " " + q + " " + executionStartCycle + " " + executionEndCycle + " " + busy;
	}
}
