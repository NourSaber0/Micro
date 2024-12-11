package logic;

import java.util.EnumMap;
import java.util.Map;

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

	private static EnumMap<InstructionType, Integer> latencies = new EnumMap<>(InstructionType.class) {
		{
			put(DADDI, 1);
			put(DSUBI, 1);
			put(ADD_D, 2);
			put(ADD_S, 2);
			put(SUB_D, 2);
			put(SUB_S, 2);
			put(MUL_D, 10);
			put(MUL_S, 10);
			put(DIV_D, 40);
			put(DIV_S, 40);
			put(LW, 2);
			put(LD, 2);
			put(L_S, 2);
			put(L_D, 2);
			put(SW, 2);
			put(SD, 2);
			put(S_S, 2);
			put(S_D, 2);
			put(BNE, 1);
			put(BEQ, 1);
		}
	};

	public static void setLatency(InstructionType type, int latency) {
		latencies.put(type, latency);
	}

	public static void setLatencies(Map<InstructionType, Integer> latencies) {
		InstructionType.latencies = new EnumMap<>(latencies);
	}

	public static void resetLatencies() {
		latencies = new EnumMap<>(InstructionType.class) {
			{
				put(DADDI, 1);
				put(DSUBI, 1);
				put(ADD_D, 2);
				put(ADD_S, 2);
				put(SUB_D, 2);
				put(SUB_S, 2);
				put(MUL_D, 10);
				put(MUL_S, 10);
				put(DIV_D, 40);
				put(DIV_S, 40);
				put(LW, 2);
				put(LD, 2);
				put(L_S, 2);
				put(L_D, 2);
				put(SW, 2);
				put(SD, 2);
				put(S_S, 2);
				put(S_D, 2);
				put(BNE, 1);
				put(BEQ, 1);
			}
		};
	}

	public static int getLatency(InstructionType type) {
		return latencies.get(type);
	}

	public static boolean isFloatingPointOperation(InstructionType type) {
		return type.name().contains("_D") || type.name().contains("_S");
	}

	public static boolean isMemoryOperation(InstructionType type) {
		return type == LW || type == LD || type == SW || type == SD || type == L_S || type == L_D|| type == S_S || type == S_D;
	}

	public boolean startsWith(String s) {
		return this.name().startsWith(s);
	}
}
