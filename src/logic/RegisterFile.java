package logic;

public class RegisterFile {
	Register[] registers;

	public RegisterFile(int size) {
		registers = new Register[size];
		for (int i = 0; i < size; i++) {
			registers[i] = new Register("F" + i);
		}
	}

	public Register getRegister(int index) {
		return registers[index];
	}
	public void reset() {
		for (Register r : registers) {
			r.reset();
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Register r : registers) {
			sb.append(r + "\n");
		}
		return sb.toString();
	}
}
