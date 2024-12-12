package logic;

public class ReservationStation {
	String tag;
	boolean busy;
	InstructionType operation;

	String vj;
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

	public boolean isReadyToExecute() {
		return qJ.equals("0") && qK.equals("0");
	}

	public void reset() {
		busy = false;
		operation = null;
		vj = vK = qJ = qK = null;
		resultValue = null;
		executionStartCycle = executionEndCycle = 0;
	}

	public String toString() {
		return tag + " " + operation + " " + vj + " " + vK + " " + qJ + " " + qK + " " + resultValue + " " + executionStartCycle + " " + executionEndCycle + " " + busy;
	}
}

