package logic;

public class ReservationStation {
	String tag;
	boolean busy;
	InstructionType operation;

	String vJ;
	String vK; // Values of operands

	String qJ;
	String qK; // Tags of operands if waiting

	String resultValue;
	int executionStartCycle = 0;
	int executionEndCycle = 0;

	public ReservationStation(String tag) {
		this.tag = tag;
		this.busy = false;
	}

	public int getExecutionEndCycle() {
		return executionEndCycle;
	}

	public int getExecutionStartCycle() {
		return executionStartCycle;
	}

	public String getResultValue() {
		return resultValue;
	}

	public String getQK() {
		return qK;
	}

	public String getQJ() {
		return qJ;
	}

	public String getVK() {
		return vK;
	}

	public String getVJ() {
		return vJ;
	}

	public InstructionType getOperation() {
		return operation;
	}

	public boolean isBusy() {
		return busy;
	}

	public String getTag() {
		return tag;
	}

	public boolean isReadyToExecute() {
		return qJ.equals("0") && qK.equals("0");
	}

	public void reset() {
		busy = false;
		operation = null;
		vJ = vK = qJ = qK = null;
		resultValue = null;
		executionStartCycle = executionEndCycle = 0;
	}


	public String toString() {
		return tag + " " + operation + " " + vJ + " " + vK + " " + qJ + " " + qK + " " + resultValue + " " + executionStartCycle + " " + executionEndCycle + " " + busy;
	}
}

