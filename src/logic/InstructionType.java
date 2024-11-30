package logic;

public enum InstructionType {
	DADDI,
	DSUBI,
	ADD_D,
	ADD_S,
	SUB_D,
	SUB_S,
	MUL_D,
	MUL_S,
	DIV_D,
	DIV_S,
	LW,
	LD,
	L_S,
	L_D,
	SW,
	SD,
	S_S,
	S_D,
	BNE,
	BEQ;

	public static int getLatency(InstructionType type) {
		switch (type) {
			case ADD_D: case ADD_S: return 2;
			case MUL_D: case MUL_S: return 10;
			case DIV_D: case DIV_S: return 40;
			case LW: case LD: return 2;
			case SW: case SD: return 2;
			default: return 1; // Default latency
		}
	}
	public static boolean isFloatingPointOperation(InstructionType type) {
		return type.name().contains("_D") || type.name().contains("_S");
	}

	public static boolean isMemoryOperation(InstructionType type) {
		return type == LW || type == LD || type == SW || type == SD || type == L_S || type == L_D;
	}

	public boolean startsWith(String s) {
		return this.name().startsWith(s);
	}
}
