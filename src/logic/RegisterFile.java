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
}
