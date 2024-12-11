package logic;

public class Instruction {
	InstructionType operation;
	String dest; // Destination register
	String src1;
	String src2; // Source registers or memory

	public Instruction(String operation, String dest, String src1, String src2) {
		this.operation = InstructionType.valueOf(operation.toUpperCase());
		this.dest = dest;
		this.src1 = src1;
		this.src2 = src2;
	}

	public String toString() {
		return operation + " " + dest + ", " + src1 + ", " + src2;
	}
}