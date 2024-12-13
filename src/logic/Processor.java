package logic;

import java.util.ArrayList;
import java.util.List;

import static logic.InstructionType.*;

public class Processor {
	private final List<CycleState> cycleStates = new ArrayList<>();
	private final List<Instruction> instructionQueue;
	private final List<Instruction> instructions;
	private final List<ReservationStationGroup> reservationStations;
	private final List<LoadStoreBufferGroup> loadStoreBuffers;
	private final ReservationStation branchBuffer = new ReservationStation("branch");
	private final RegisterFile registerFile; // Example: 32 registers
	private final CommonDataBus cdb = new CommonDataBus();
	private final Data data;
	List<ReservationStation> readyStations = new ArrayList<>();
	List<LoadStoreBuffer> readyBuffers = new ArrayList<>();
	private boolean isBranchExecuting = false;
	private int cycle = 0;

	public Processor(List<ReservationStationGroup> reservationStations, List<LoadStoreBufferGroup> loadStoreBuffers, Data data, List<Instruction> instructions, int registerFileSize) {
		this.reservationStations = reservationStations;
		this.loadStoreBuffers = loadStoreBuffers;
		this.data = data;
		this.instructionQueue = new ArrayList<>(instructions);
		this.instructions = new ArrayList<>(instructions);
		registerFile = new RegisterFile(registerFileSize);


		cycleStates.add(getCurrentCycleState());
	}

	public static void main(String[] args) {
		// Create reservation stations
		List<ReservationStationGroup> reservationStations = new ArrayList<>();
		reservationStations.add(new ReservationStationGroup(3, InstructionType.SUB_D));
		reservationStations.add(new ReservationStationGroup(3, InstructionType.ADD_D));
		reservationStations.add(new ReservationStationGroup(3, InstructionType.MUL_D));
		reservationStations.add(new ReservationStationGroup(3, InstructionType.DIV_D));
		reservationStations.add(new ReservationStationGroup(3, InstructionType.DADDI));
		reservationStations.add(new ReservationStationGroup(3, InstructionType.DSUBI));

		List<LoadStoreBufferGroup> loadStoreBuffers = new ArrayList<>();
		loadStoreBuffers.add(new LoadStoreBufferGroup(3, InstructionType.L_D));
		loadStoreBuffers.add(new LoadStoreBufferGroup(3, InstructionType.S_D));

		Data data = new Data(5, 2, 2, 10, 100);

		ArrayList<Instruction> instructions = new ArrayList<>();
		instructions.add(new Instruction("SUB_D", "F1", "F1", "F1"));
		instructions.add(new Instruction("DADDI", "F2", "F1", "5"));
		instructions.add(new Instruction("DSUBI", "F2", "F2", "1"));
		instructions.add(new Instruction("BNE", "F1", "F2", "3"));

// Create processor
		Processor processor = new Processor(reservationStations, loadStoreBuffers, data, instructions, 32);

// Set latencies
		InstructionType.setLatency(InstructionType.DADDI, 1);
		InstructionType.setLatency(InstructionType.DSUBI, 1);
		InstructionType.setLatency(InstructionType.ADD_D, 2);
		InstructionType.setLatency(InstructionType.SUB_D, 2);
		InstructionType.setLatency(InstructionType.MUL_D, 10);
		InstructionType.setLatency(InstructionType.DIV_D, 10);


		processor.simulateAll();
		System.out.println(data);
	}

	public CycleState getCycleState(int cycle) {
		if (cycle >= 0 && cycle < cycleStates.size()) {
			return cycleStates.get(cycle);
		} else {
			throw new IndexOutOfBoundsException("Cycle out of range: " + cycle);
		}
	}

	public void addInstruction(Instruction instruction) {
		instructionQueue.add(instruction);
		instructions.add(instruction);
	}

	private ReservationStation getFreeStation(InstructionType operation) {
		for (ReservationStationGroup group : reservationStations) {
			if (group.operation == operation) {
				for (ReservationStation station : group.stations) {
					if (!station.busy) {
						return station;
					}
				}
			}
		}
		return null;
	}

	private LoadStoreBuffer getFreeLoadStoreBuffer(InstructionType operation) {
		for (LoadStoreBufferGroup group : loadStoreBuffers) {
			if (group.operation == operation) {
				for (LoadStoreBuffer buffer : group.loadStoreBuffers) {
					if (!buffer.busy) {
						return buffer;
					}
				}
			}
		}
		return null;
	}

	private void issueStage() {
		System.out.println("Instruction Queue: " + instructionQueue);
		if (instructionQueue.isEmpty()) return;
		Instruction instruction = instructionQueue.getFirst();
		System.out.println("Instruction to issue: " + instruction);
		try {
			if (instruction.operation == InstructionType.BNE || instruction.operation == InstructionType.BEQ) {
				branchBuffer.busy = true;
				branchBuffer.operation = instruction.operation;
				branchBuffer.resultValue = instruction.src2;
				Register dest = registerFile.getRegister(Integer.parseInt(instruction.dest.substring(1)));
				if (dest.getReady()) {
					branchBuffer.qJ = "0";
					branchBuffer.vJ = dest.getValue();
				} else {
					branchBuffer.qJ = dest.getTag();
				}
				Register src1 = registerFile.getRegister(Integer.parseInt(instruction.src1.substring(1)));
				if (src1.getReady()) {
					branchBuffer.qK = "0";
					branchBuffer.vK = src1.getValue();
				} else {
					branchBuffer.qK = src1.getTag();
				}
				isBranchExecuting = true;
				instructionQueue.removeFirst();
				System.out.println("Instruction issued: " + instruction);
				System.out.println("Branch buffer: " + branchBuffer);
				return;
			}
			if (isMemoryOperation(instruction.operation)) {
				LoadStoreBuffer buffer = getFreeLoadStoreBuffer(instruction.operation);
				if (buffer == null) {
					System.out.println("No free load store buffer for operation: " + instruction.operation);
					return;
				}
				buffer.busy = true;
				buffer.operation = instruction.operation;
				buffer.address = instruction.src1;

				Register dest = registerFile.getRegister(Integer.parseInt(instruction.dest.substring(1)));
				if (dest.getReady()) {
					buffer.q = "0";
					if (instruction.operation == InstructionType.S_D) {
						buffer.value = dest.getValue();
					}
				} else {
					buffer.q = dest.getTag();
				}
				if (instruction.operation == InstructionType.L_D || instruction.operation == InstructionType.L_S || instruction.operation == InstructionType.LW || instruction.operation == InstructionType.LD)  {
					dest.setTag(buffer.tag);
				}
			} else {
				ReservationStation station = getFreeStation(instruction.operation);
				if (station == null) {
					System.out.println("No free reservation station for operation: " + instruction.operation);
					return;
				}
				// Issue instruction to reservation station
				station.busy = true;
				station.operation = instruction.operation;
				// Fetch and validate source registers
					Register src1 = registerFile.getRegister(Integer.parseInt(instruction.src1.substring(1)));
					if (src1.getReady()) {
						station.vJ = src1.getValue();
						station.qJ = "0";
					} else {
						station.qJ = src1.getTag();
					}
					Register src2;
					if (instruction.operation != InstructionType.DADDI && instruction.operation != InstructionType.DSUBI) {
						src2 = registerFile.getRegister(Integer.parseInt(instruction.src2.substring(1)));
						if (src2.getReady()) {
							station.vK = src2.getValue();
							station.qK = "0";
						} else {
							station.qK = src2.getTag();
						}
					} else {
						station.vK = instruction.src2;
						station.qK = "0";
					}
					Register dest = registerFile.getRegister(Integer.parseInt(instruction.dest.substring(1)));
					dest.setTag(station.tag);
			}

			// Remove instruction from the queue
			instructionQueue.removeFirst();

			System.out.println("Instruction issued: " + instruction);

		} catch (NumberFormatException | NullPointerException e) {
			System.err.println("Error issuing instruction: " + instruction + " - " + e.getMessage());
		}
	}

	private void checkBusReservationStations() {
		if (branchBuffer.busy && cdb.tag != null && cdb.tag.equals(branchBuffer.qJ)) {
			branchBuffer.qJ = "0";
			branchBuffer.vJ = cdb.value;
		}
		if (branchBuffer.busy && cdb.tag != null && cdb.tag.equals(branchBuffer.qK)) {
			branchBuffer.qK = "0";
			branchBuffer.vK = cdb.value;
		}
		if (branchBuffer.isBusy() && branchBuffer.qJ.equals("0") && branchBuffer.qK.equals("0")) {
			branchBuffer.executionStartCycle = cycle + 1;
		}
		for (ReservationStationGroup group : reservationStations) {
			for (ReservationStation station : group.stations) {
				if (!station.busy) {
					continue;
				}

				if (station.qJ.equals("0") && station.qK.equals("0")) {
					return;
				}

				if (cdb.tag == null) {
					return;
				}

				if (cdb.tag.equals(station.qJ)) {
					station.vJ = cdb.value;
					station.qJ = "0";
				}
				if (cdb.tag.equals(station.qK)) {
					station.vK = cdb.value;
					station.qK = "0";
				}

				if (station.qJ.equals("0") && station.qK.equals("0")) {
					station.executionStartCycle = cycle + 1;
					station.executionEndCycle = cycle + getLatency(station.operation);
				}
			}
		}
	}

	private void checkBusLoadStoreBuffer() {

		for (LoadStoreBufferGroup group : loadStoreBuffers) {
			for (LoadStoreBuffer buffer : group.loadStoreBuffers) {
				if (!buffer.busy) {
					continue;
				}

				if (buffer.q.equals("0")) {
					continue;
				}

				if (cdb.tag == null) {
					continue;
				}

				//reset bus
				if (cdb.tag.equals(buffer.q)) {
					// only for stores
					if (buffer.operation == InstructionType.S_D) {
						buffer.value = cdb.value;
					}
					// for both
					buffer.q = "0";
				}

				if (buffer.q.equals("0")) {
					buffer.executionStartCycle = cycle + 1;
					buffer.executionEndCycle = cycle + getLatency(buffer.operation) + data.getLatency(Integer.parseInt(buffer.address));
				}
			}
		}
	}

	private void checkBusRegisterFile() {
		if (cdb.tag == null) {
			return;
		}

		for (Register register : registerFile.registers) {
			if (cdb.tag.equals(register.tag)) {
				register.setValue(cdb.value);
				register.setTag(null);
				System.out.println("Broadcasting result to register: " + register.getName());
			}
		}
	}

	private void executeStage() {
		// at end of execution update destination register (ASK ALY)
		System.out.println("Branch buffer: " + branchBuffer);
		if (branchBuffer.busy && branchBuffer.isReadyToExecute()) {
			if (branchBuffer.operation == InstructionType.BNE) {
				if (Double.parseDouble(branchBuffer.vJ) - Double.parseDouble(branchBuffer.vK) != 0) {
					// put instructions from array to queue
					instructionQueue.clear();
					// put in instruction queue from instruction starting from branchBuffer.address
					for (int i = Integer.parseInt(branchBuffer.resultValue) - 1; i < instructions.size(); i++) {
						instructionQueue.add(instructions.get(i));
					}
				}
			} else if (branchBuffer.operation == InstructionType.BEQ) {
				if (Double.parseDouble(branchBuffer.vJ) - Double.parseDouble(branchBuffer.vK) == 0) {
					// put instructions from array to queue
					instructionQueue.clear();
					// put in instruction queue from instruction starting from branchBuffer.address
					for (int i = Integer.parseInt(branchBuffer.resultValue) - 1; i < instructions.size(); i++) {
						instructionQueue.add(instructions.get(i));
					}
				}
			}
			isBranchExecuting = false;
			System.out.println("Executing Branch buffer: " + branchBuffer);
			branchBuffer.reset();
			return;
		}
		for (ReservationStationGroup group : reservationStations) {
			for (ReservationStation station : group.stations) {
				if (station.busy && station.isReadyToExecute()) {
					if (station.executionStartCycle == 0) {
						station.executionStartCycle = cycle + 1;
						station.executionEndCycle = cycle + getLatency(station.operation);
						continue;
					}
					if (station.executionStartCycle <= cycle && station.executionEndCycle > cycle) {
						System.out.println("Executing instruction: " + station);
					}
					if (station.executionEndCycle == cycle) {
						switch (InstructionType.valueOf(station.operation.toString())) {
							case ADD_D, ADD_S:
								station.resultValue = String.valueOf(Double.parseDouble(station.vJ) + Double.parseDouble(station.vK));
								break;
							case SUB_D, SUB_S, DSUBI:
								station.resultValue = String.valueOf(Double.parseDouble(station.vJ) - Double.parseDouble(station.vK));
								break;
							case MUL_D, MUL_S:
								station.resultValue = String.valueOf(Double.parseDouble(station.vJ) * Double.parseDouble(station.vK));
								break;
							case DIV_D, DIV_S:
								station.resultValue = String.valueOf(Double.parseDouble(station.vJ) / Double.parseDouble(station.vK));
								break;
							case DADDI:
								station.resultValue = String.valueOf(Double.parseDouble(station.vJ) + Double.parseDouble(station.vK));
								break;

							default:
								break;
						}
						System.out.println("Execution finished for station: " + station);
					}
				}
			}
		}

		for (LoadStoreBufferGroup group : loadStoreBuffers) {
			for (LoadStoreBuffer buffer : group.loadStoreBuffers) {
				if (buffer.busy && buffer.q.equals("0")) {
					System.out.println("Buffer: " + buffer);
					if (buffer.executionStartCycle == 0) {
						buffer.executionStartCycle = cycle + 1;
						buffer.executionEndCycle = cycle + getLatency(buffer.getOperation()) + data.getLatency(Integer.parseInt(buffer.address));
						continue;
					}
					if (buffer.executionStartCycle <= cycle && buffer.executionEndCycle > cycle) {
						System.out.println("Executing instruction: " + buffer);
					}
					if (buffer.executionEndCycle == cycle) {
						switch (InstructionType.valueOf(buffer.operation.toString())) {
							case L_D, L_S, LW, LD:
								buffer.value = data.read(Integer.parseInt(buffer.address));
								break;
							case S_D, S_S, SW, SD:
								data.write(Integer.parseInt(buffer.address), buffer.value);
								break;
							default:
								break;
						}
						System.out.println("Execution finished for buffer: " + buffer);
					}
				}
			}
		}
	}

	private int countDependencies(ReservationStation station) {
		int dependencies = 0;

		// Check other reservation stations
		for (ReservationStationGroup group : reservationStations) {
			for (ReservationStation s : group.stations) {
				if (s.qJ != null && s.qJ.equals(station.tag)) {
					dependencies++;
				}
				if (s.qK != null && s.qK.equals(station.tag)) {
					dependencies++;
				}
			}
		}
		// check load store buffers
		for (LoadStoreBufferGroup group : loadStoreBuffers) {
			for (LoadStoreBuffer buffer : group.loadStoreBuffers) {
				if (buffer.q != null && buffer.q.equals(station.tag)) {
					dependencies++;
				}
			}
		}

		// Check registers
		for (int i = 0; i < registerFile.getSize(); i++) {
			Register reg = registerFile.getRegister(i);
			if (reg.getTag() != null && reg.getTag().equals(station.tag)) {
				dependencies++;
			}
		}

		return dependencies;
	}

	private int countDependencies(LoadStoreBuffer buffer) {
		int dependencies = 0;

		// Check other reservation stations
		for (ReservationStationGroup group : reservationStations) {
			for (ReservationStation s : group.stations) {
				if (s.qJ != null && s.qJ.equals(buffer.tag)) {
					dependencies++;
				}
				if (s.qK != null && s.qK.equals(buffer.tag)) {
					dependencies++;
				}
			}
		}
		// check load store buffers
		for (LoadStoreBufferGroup group : loadStoreBuffers) {
			for (LoadStoreBuffer b : group.loadStoreBuffers) {
				if (b.q != null && b.q.equals(buffer.tag)) {
					dependencies++;
				}
			}
		}

		// Check registers
		for (int i = 0; i <registerFile.getSize(); i++) {
			Register reg = registerFile.getRegister(i);
			if (reg.getTag() != null && reg.getTag().equals(buffer.tag)) {
				dependencies++;
			}
		}

		return dependencies;
	}

	private void writeResultStage() {
		// Collect all stations ready to write their results
		for (ReservationStationGroup group : reservationStations) {
			for (ReservationStation station : group.stations) {
				if (station.executionEndCycle == cycle - 1 && station.busy) {
					readyStations.add(station); // Add ready station
				}
			}
		}

		// only for loads
		for (LoadStoreBufferGroup group : loadStoreBuffers) {
			for (LoadStoreBuffer buffer : group.loadStoreBuffers) {
				if (buffer.executionEndCycle == cycle - 1 && buffer.busy) {
					if (buffer.operation == InstructionType.L_D || buffer.operation == InstructionType.L_S || buffer.operation == InstructionType.LW || buffer.operation == InstructionType.LD) {
						readyBuffers.add(buffer); // Add ready station
					}
				}
				if (buffer.executionEndCycle == cycle  && buffer.busy) {
					if (buffer.operation == InstructionType.S_D || buffer.operation == InstructionType.S_S || buffer.operation == InstructionType.SW || buffer.operation == InstructionType.SD) {
						buffer.reset();
					}
				}
			}
		}

		if (!readyStations.isEmpty() || !readyBuffers.isEmpty()) {
			ReservationStation prioritizedStation = null;
			LoadStoreBuffer prioritizedBuffer = null;
			int maxDependenciesStation = -1;
			int maxDependenciesBuffer = -1;

			if (!readyStations.isEmpty()) {
				prioritizedStation = readyStations.getFirst();
				for (ReservationStation station : readyStations) {
					int dependencies = countDependencies(station);
					if (dependencies > maxDependenciesStation) {
						maxDependenciesStation = dependencies;
						prioritizedStation = station;
					}
				}
			}

			if (!readyBuffers.isEmpty()) {
				prioritizedBuffer = readyBuffers.getFirst();
				for (LoadStoreBuffer buffer : readyBuffers) {
					int dependencies = countDependencies(buffer);
					if (dependencies > maxDependenciesBuffer) {
						maxDependenciesBuffer = dependencies;
						prioritizedBuffer = buffer;
					}
				}
			}

			if (prioritizedBuffer != null && (prioritizedStation == null || maxDependenciesBuffer > maxDependenciesStation)) {
				readyBuffers.remove(prioritizedBuffer);
				cdb.tag = prioritizedBuffer.tag;
				cdb.value = prioritizedBuffer.value;

				System.out.println("Writing result from buffer: " + prioritizedBuffer);

				prioritizedBuffer.reset();
			} else if (prioritizedStation != null) {
				readyStations.remove(prioritizedStation);
				cdb.tag = prioritizedStation.tag;
				cdb.value = prioritizedStation.resultValue;

				System.out.println("Writing result from station: " + prioritizedStation);

				prioritizedStation.reset();
			}
		}
		checkBusRegisterFile();
		checkBusReservationStations();
		checkBusLoadStoreBuffer();
		cdb.reset();
	}

	private boolean canIssue() {
		if (instructionQueue.isEmpty()) {
			return false;
		}
		if (isBranchExecuting) {
			return false;
		}
		// check if two memory operations are accessing the same address
		if (isMemoryOperation(instructionQueue.getFirst().operation)) {
			for (LoadStoreBufferGroup group : loadStoreBuffers) {
				for (LoadStoreBuffer buffer : group.loadStoreBuffers) {
					if (buffer.busy && buffer.address.equals(instructionQueue.getFirst().src1)) {
						return false;
					}
				}
			}
		}
		if (instructionQueue.getFirst().operation == InstructionType.BNE || instructionQueue.getFirst().operation == InstructionType.BEQ) {
			return true;
		}

		for (ReservationStationGroup group : reservationStations) {
			if (group.operation == instructionQueue.getFirst().operation) {
				for (ReservationStation station : group.stations) {
					if (!station.busy) {
						return true;
					}
				}
			}
		}
		for (LoadStoreBufferGroup group : loadStoreBuffers) {
			if (group.operation == instructionQueue.getFirst().operation) {
				for (LoadStoreBuffer buffer : group.loadStoreBuffers) {
					if (!buffer.busy) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public boolean isSimulationComplete() {
		// Check if there are instructions still in the queue
		if (!instructionQueue.isEmpty()) {
			return false;
		}

		// Check if any reservation stations are busy
		for (ReservationStationGroup group : reservationStations) {
			for (ReservationStation station : group.stations) {
				if (station.busy) {
					return false;
				}
			}
		}

		for (LoadStoreBufferGroup group : loadStoreBuffers) {
			for (LoadStoreBuffer buffer : group.loadStoreBuffers) {
				if (buffer.busy) {
					System.out.println("Buffer busy: " + buffer);
					return false;
				}
			}
		}

		// Check if any registers are waiting for a result
		for (Register register : registerFile.registers) {
			if (register.tag != null) {
				return false;
			}
		}

		// Check if a branch is executing
		return !isBranchExecuting;
	}

	public void simulate() {
		cycle++;
		System.out.println("Cycle: " + cycle);
		if (canIssue() && !instructionQueue.isEmpty()) {
			issueStage();
		}
		executeStage();
		writeResultStage();

		// Save cycle state
		cycleStates.add(getCurrentCycleState());

		System.out.println("Register File: \n" + registerFile);
	}

	public void simulateAll() {
		while (!isSimulationComplete()) {
			simulate();
		}
	}

	public List<CycleState> getCycleStates() {
		return cycleStates;
	}

	private CycleState getCurrentCycleState() {
		List<ReservationStationGroup> reservationStationsCopy = new ArrayList<>();
		for (ReservationStationGroup group : reservationStations) {
			ReservationStationGroup groupCopy = new ReservationStationGroup(group.stations.length, group.operation);
			int i = 0;
			for (ReservationStation station : group.stations) {
				ReservationStation stationCopy = new ReservationStation(station.tag);
				stationCopy.busy = station.busy;
				stationCopy.operation = station.operation;
				stationCopy.vJ = station.vJ;
				stationCopy.vK = station.vK;
				stationCopy.qJ = station.qJ;
				stationCopy.qK = station.qK;
				stationCopy.resultValue = station.resultValue;
				stationCopy.executionStartCycle = station.executionStartCycle;
				stationCopy.executionEndCycle = station.executionEndCycle;
				groupCopy.stations[i++] = stationCopy;
			}
			reservationStationsCopy.add(groupCopy);
		}

		List<LoadStoreBufferGroup> loadStoreBuffersCopy = new ArrayList<>();
		for (LoadStoreBufferGroup group : loadStoreBuffers) {
			LoadStoreBufferGroup groupCopy = new LoadStoreBufferGroup(group.loadStoreBuffers.length, group.operation);
			int i = 0;
			for (LoadStoreBuffer buffer : group.loadStoreBuffers) {
				LoadStoreBuffer bufferCopy = new LoadStoreBuffer(buffer.tag);
				bufferCopy.busy = buffer.busy;
				bufferCopy.operation = buffer.operation;
				bufferCopy.address = buffer.address;
				bufferCopy.value = buffer.value;
				bufferCopy.q = buffer.q;
				bufferCopy.executionStartCycle = buffer.executionStartCycle;
				bufferCopy.executionEndCycle = buffer.executionEndCycle;
				groupCopy.loadStoreBuffers[i++] = bufferCopy;
			}
			loadStoreBuffersCopy.add(groupCopy);
		}

		List<Instruction> instructionQueueCopy = new ArrayList<>(instructionQueue);

		Data dataCopy = new Data(data);

		RegisterFile registerFileCopy = new RegisterFile(registerFile);

		return new CycleState(reservationStationsCopy, loadStoreBuffersCopy, cdb, instructionQueueCopy, registerFileCopy, dataCopy, cycle, isSimulationComplete());
	}

	public RegisterFile getRegisterFile() {
		return registerFile;
	}

	public void reset() {
		cycle = 1;
		cycleStates.clear();
		instructionQueue.clear();
		registerFile.reset();
		cdb.reset();
		for (ReservationStationGroup group : reservationStations) {
			for (ReservationStation station : group.stations) {
				station.reset();
			}
		}
		branchBuffer.reset();
		for (LoadStoreBufferGroup group : loadStoreBuffers) {
			for (LoadStoreBuffer buffer : group.loadStoreBuffers) {
				buffer.reset();
			}
		}
	}

}