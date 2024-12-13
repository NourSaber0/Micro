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

	public String getAddress() {
		return address;
	}

	public String getValue() {
		return value;
	}

	public String getTag() {
		return tag;
	}

	public String getQ() {
		return q;
	}

	public InstructionType getOperation() {
		return operation;
	}

	public boolean isBusy() {
		return busy;
	}

	public int getExecutionStartCycle() {
		return executionStartCycle;
	}

	public int getExecutionEndCycle() {
		return executionEndCycle;
	}

	public void reset() {
		busy = false;
		operation = null;
		address = value = q = null;
		executionStartCycle = executionEndCycle = 0;
	}

	public String toString() {
		return tag + " " + operation + " " + address + " " + value + " " + q + " " + executionStartCycle + " " + executionEndCycle + " " + busy;
	}
}
