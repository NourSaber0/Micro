package logic;

import java.util.ArrayList;
import java.util.List;

import static logic.InstructionType.getLatency;

public class Processor {
	private final List<Instruction> instructionQueue = new ArrayList<>();
	private final List<ReservationStationGroup> reservationStations;
	private final List<LoadStoreBufferGroup> loadStoreBuffers;
	private final RegisterFile registerFile = new RegisterFile(32); // Example: 32 registers
	private final CommonDataBus cdb = new CommonDataBus();
	private final Data data;
	List<ReservationStation> readyStations = new ArrayList<>();
	List<LoadStoreBuffer> readyBuffers = new ArrayList<>();
	private int cycle = 1;

	public Processor(List<ReservationStationGroup> reservationStations, List<LoadStoreBufferGroup> loadStoreBuffers, Data data) {
		this.reservationStations = reservationStations;
		this.loadStoreBuffers = loadStoreBuffers;
		this.data = data;
	}

	public void addInstruction(Instruction instruction) {
		instructionQueue.add(instruction);
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
		if (instructionQueue.isEmpty()) return;
		Instruction instruction = instructionQueue.getFirst();
		try {
			if (instruction.operation == InstructionType.L_D || instruction.operation == InstructionType.S_D) {
				LoadStoreBuffer buffer = getFreeLoadStoreBuffer(instruction.operation);
				if (buffer == null) {
					System.out.println("No free load store buffer for operation: " + instruction.operation);
					return;
				}
				buffer.busy = true;
				buffer.operation = instruction.operation;
				buffer.address = instruction.src1;

				Register dest = registerFile.getRegister(Integer.parseInt(instruction.dest.substring(1)));
				if (instruction.operation == InstructionType.S_D) {
					if (dest.getReady()) {
						buffer.value = dest.getValue();
						buffer.q = "0";
					} else {
						buffer.q = dest.getTag();
					}
				} else {
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
					station.Vj = src1.getValue();
					station.Qj = "0";
				} else {
					station.Qj = src1.getTag();
				}
				Register src2;
				if (instruction.operation != InstructionType.DADDI && instruction.operation != InstructionType.DSUBI) {
					src2 = registerFile.getRegister(Integer.parseInt(instruction.src2.substring(1)));
					if (src2.getReady()) {
						station.Vk = src2.getValue();
						station.Qk = "0";
					} else {
						station.Qk = src2.getTag();
					}
				} else {
					station.Vk = instruction.src2;
					station.Qk = "0";
				}

				// Update destination register
				Register dest = registerFile.getRegister(Integer.parseInt(instruction.dest.substring(1)));
				dest.setTag(station.tag);
			}
			// Update instruction metadata
			instruction.issueCycle = cycle;

			// Remove instruction from the queue
			instructionQueue.removeFirst();

			System.out.println("Instruction issued: " + instruction);

		} catch (NumberFormatException | NullPointerException e) {
			System.err.println("Error issuing instruction: " + instruction + " - " + e.getMessage());
		}
	}

	private void checkBusReservationStations() {
		for (ReservationStationGroup group : reservationStations) {
			for (ReservationStation station : group.stations) {
				if (!station.busy) {
					continue;
				}

				if (station.Qj.equals("0") && station.Qk.equals("0")) {
					return;
				}

				if (cdb.tag == null) {
					return;
				}

				if (cdb.tag.equals(station.Qj)) {
					station.Vj = cdb.value;
					station.Qj = "0";
				}
				if (cdb.tag.equals(station.Qk)) {
					station.Vk = cdb.value;
					station.Qk = "0";
				}

				if (station.Qj.equals("0") && station.Qk.equals("0")) {
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
					return;
				}

				if (cdb.tag == null) {
					return;
				}

				if (cdb.tag.equals(buffer.q)) {
					buffer.value = cdb.value;
					buffer.q = "0";
				}

				if (buffer.q.equals("0")) {
					buffer.executionStartCycle = cycle + 1;
					buffer.executionEndCycle = cycle + getLatency(buffer.operation);
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
		for (ReservationStationGroup group : reservationStations) {
			for (ReservationStation station : group.stations) {
				if (station.busy && station.isReadyToExecute()) {
					if (station.executionStartCycle == 0) {

						station.executionStartCycle = cycle + 1;
						if (station.operation.startsWith("L_D") || station.operation.startsWith("S_D")) {
							// Load data from memory
							station.executionEndCycle = (data.getLatency(Integer.parseInt(station.Vj)) + cycle);
						}
						station.executionEndCycle = cycle + getLatency(station.operation);
						continue;
					}
					if (station.executionStartCycle <= cycle && station.executionEndCycle > cycle) {
						System.out.println("Executing instruction: " + station);
					}
					if (station.executionEndCycle == cycle) {
						switch (InstructionType.valueOf(station.operation.toString())) {
							case ADD_D, ADD_S:
								station.resultValue = String.valueOf(Double.parseDouble(station.Vj) + Double.parseDouble(station.Vk));
								break;
							case SUB_D, SUB_S:
								station.resultValue = String.valueOf(Double.parseDouble(station.Vj) - Double.parseDouble(station.Vk));
								break;
							case MUL_D, MUL_S:
								station.resultValue = String.valueOf(Double.parseDouble(station.Vj) * Double.parseDouble(station.Vk));
								break;
							case DIV_D, DIV_S:
								station.resultValue = String.valueOf(Double.parseDouble(station.Vj) / Double.parseDouble(station.Vk));
								break;
							case DADDI, DSUBI:
								station.resultValue = String.valueOf(Double.parseDouble(station.Vj) + Double.parseDouble(station.Vk));
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
					if (buffer.executionStartCycle == 0) {
						buffer.executionStartCycle = cycle + 1;
						buffer.executionEndCycle = cycle + getLatency(buffer.operation);
						continue;
					}
					if (buffer.executionStartCycle <= cycle && buffer.executionEndCycle > cycle) {
						System.out.println("Executing instruction: " + buffer);
					}
					if (buffer.executionEndCycle == cycle) {
						switch (InstructionType.valueOf(buffer.operation.toString())) {
							case L_D:
								buffer.value = data.read(Integer.parseInt(buffer.address));
								break;
							case S_D:
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

	private ReservationStation findStationByTag(String tag) {
		for (ReservationStationGroup group : reservationStations) {
			for (ReservationStation station : group.stations) {
				if (station.tag.equals(tag)) {
					return station;
				}
			}
		}
		return null;
	}

	private int countDependencies(ReservationStation station) {
		int dependencies = 0;

		// Check other reservation stations
		for (ReservationStationGroup group : reservationStations) {
			for (ReservationStation s : group.stations) {
				if (s.Qj != null && s.Qj.equals(station.tag)) {
					dependencies++;
				}
				if (s.Qk != null && s.Qk.equals(station.tag)) {
					dependencies++;
				}
			}
		}

		// Check registers
		for (int i = 0; i < 32; i++) {
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
				if (s.Qj != null && s.Qj.equals(buffer.tag)) {
					dependencies++;
				}
				if (s.Qk != null && s.Qk.equals(buffer.tag)) {
					dependencies++;
				}
			}
		}

		// Check registers
		for (int i = 0; i < 32; i++) {
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
		for (LoadStoreBufferGroup group : loadStoreBuffers) {
			for (LoadStoreBuffer buffer : group.loadStoreBuffers) {
				if (buffer.executionEndCycle == cycle - 1 && buffer.busy) {
					readyBuffers.add(buffer); // Add ready station
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
	}

	private boolean canIssue() {
		if (instructionQueue.isEmpty()) {
			return false;
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

	private boolean isSimulationComplete() {
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

		// Check if any registers are waiting for a result
		for (Register register : registerFile.registers) {
			if (register.tag != null) {
				return false;
			}
		}

		return true;
	}

	public void simulate() {
		while (!isSimulationComplete() && cycle < 100) {
			System.out.println("Cycle: " + cycle);
			if (canIssue() && !instructionQueue.isEmpty()) {
				issueStage();
			}
			executeStage();
			writeResultStage();
			cycle++;
			//System.out.println("Register File: \n" + registerFile);
		}
	}


	public static void main(String[] args) {
		// Create reservation stations
		List<ReservationStationGroup> reservationStations = new ArrayList<>();
		reservationStations.add(new ReservationStationGroup(3, InstructionType.ADD_D));
		reservationStations.add(new ReservationStationGroup(2, InstructionType.SUB_D));
		reservationStations.add(new ReservationStationGroup(2, InstructionType.MUL_D));
		reservationStations.add(new ReservationStationGroup(2, InstructionType.DIV_D));
		reservationStations.add(new ReservationStationGroup(1, InstructionType.DADDI));

		// Create LoadStoreBuffers
		List<LoadStoreBufferGroup> loadStoreBuffers = new ArrayList<>();
		loadStoreBuffers.add(new LoadStoreBufferGroup(2, InstructionType.L_D));
		loadStoreBuffers.add(new LoadStoreBufferGroup(2, InstructionType.S_D));

		// Create Data
		Data data = new Data(5, 2, 2, 20, 100);
		// Create processor
		Processor processor = new Processor(reservationStations, loadStoreBuffers, data);

		// Add instructions to the processor
		processor.addInstruction(new Instruction("L_D", "F0", "0", null));
		processor.addInstruction(new Instruction("ADD_D", "F2", "F0", "F4"));
		processor.addInstruction(new Instruction("SUB_D", "F6", "F2", "F8"));
		processor.addInstruction(new Instruction("MUL_D", "F10", "F6", "F12"));
		processor.addInstruction(new Instruction("DIV_D", "F14", "F10", "F16"));
		processor.addInstruction(new Instruction("DADDI", "F14", "F1", "100"));
		processor.addInstruction(new Instruction("S_D", "F14", "12", null));

		// Provide Latency for each instruction
		InstructionType.setLatency(InstructionType.ADD_D, 2);
		InstructionType.setLatency(InstructionType.SUB_D, 2);
		InstructionType.setLatency(InstructionType.MUL_D, 10);
		InstructionType.setLatency(InstructionType.DIV_D, 40);
		InstructionType.setLatency(InstructionType.DADDI, 1);

		// Simulate the processor
		processor.simulate();

		System.out.println(data);
	}
}