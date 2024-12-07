package logic;

public class LoadStoreBufferGroup {
	LoadStoreBuffer[] loadStoreBuffers;
	InstructionType operation;

	public LoadStoreBufferGroup(int size, InstructionType operation) {
		loadStoreBuffers = new LoadStoreBuffer[size];
		this.operation = operation;
		for (int i = 0; i < size; i++) {
			loadStoreBuffers[i] = new LoadStoreBuffer(operation.name() + i);
		}
	}

	public LoadStoreBuffer getLoadStoreBuffer(int index) {
		return loadStoreBuffers[index];
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (LoadStoreBuffer lsb : loadStoreBuffers) {
			sb.append(lsb + "\n");
		}
		return sb.toString();
	}
}
