package logic;

public class ReservationStation {
	String tag;
	boolean busy;
	InstructionType  operation;
	String Vj, Vk; // Values of operands
	String Qj, Qk; // Tags of operands if waiting

	String resultValue;
	int executionStartCycle = 0, executionEndCycle = 0;


	public ReservationStation(String tag) {
		this.tag = tag;
		this.busy = false;
	}
	public boolean isReadyToExecute() {
		return Qj.equals("0") && Qk.equals("0");
	}
	public void reset() {
		busy = false;
		operation = null;
		Vj = Vk = Qj = Qk = null;
		resultValue = null;
		executionStartCycle = executionEndCycle = 0;
	}

	public String toString() {
		return tag + " " + operation + " " + Vj + " " + Vk + " " + Qj + " " + Qk + " " + resultValue + " " + executionStartCycle + " " + executionEndCycle + " " + busy;
	}
}

