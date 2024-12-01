package logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static logic.InstructionType.getLatency;

public class Processor {
	private final List<Instruction> instructionQueue = new ArrayList<>();
	private final List<ReservationStationGroup> reservationStations;
	private final RegisterFile registerFile = new RegisterFile(32); // Example: 32 registers
	private final CommonDataBus cdb = new CommonDataBus();
	private final Cache dataCache;
	private final Map<Integer, String> memory = new HashMap<>(); // Simulated main memory
	private int cycle = 1;

	public Processor(List<ReservationStationGroup> reservationStations, Cache dataCache) {
		this.reservationStations = reservationStations;
		this.dataCache = dataCache;
	}

	public void addInstruction(Instruction instruction) {
		instructionQueue.add(instruction);
	}

	private ReservationStation getFreeStation(InstructionType operation) {
		for (ReservationStationGroup group : reservationStations) {
			if (group.operation == operation) {
				for (ReservationStation station : group.stations) {
					if (station.busy == false) {
						return station;
					}
				}
			}
		}
		return null;
	}

	private void issueStage() {
		if (instructionQueue.isEmpty()) return;
		Instruction instruction = instructionQueue.getFirst();
		ReservationStation station = getFreeStation(instruction.operation);
		if (station == null) {
			System.out.println("No free reservation station for operation: " + instruction.operation);
			return;
		}

		// Issue instruction to reservation station
		station.busy = true;
		station.operation = instruction.operation;

		// Fetch and validate source registers
		try {

			Register src1 = registerFile.getRegister(Integer.parseInt(instruction.src1.substring(1)));
			Register src2 = registerFile.getRegister(Integer.parseInt(instruction.src2.substring(1)));

			// Set source operand values or dependency tags
			if (src1.getReady()) {
				station.Vj = src1.getValue();
				station.Qj = "0";
			} else {
				station.Qj = src1.getTag();
			}
			if (src2.getReady()) {
				station.Vk = src2.getValue();
				station.Qk = "0";
			} else {
				station.Qk = src2.getTag();
			}

			// Update destination register
			Register dest = registerFile.getRegister(Integer.parseInt(instruction.dest.substring(1)));
			dest.setTag(station.tag);

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
						station.executionEndCycle = cycle + getLatency(station.operation);
						continue;
					}
					if (station.executionStartCycle <= cycle && station.executionEndCycle > cycle) {
						System.out.println("Executing instruction: " + station);
					}

					if (station.executionEndCycle == cycle) {
						switch (InstructionType.valueOf(station.operation.toString())) {
							case ADD_D:
							case ADD_S:
								station.resultValue = String.valueOf(Double.parseDouble(station.Vj) + Double.parseDouble(station.Vk));
								break;
							case SUB_D:
							case SUB_S:
								station.resultValue = String.valueOf(Double.parseDouble(station.Vj) - Double.parseDouble(station.Vk));
								break;
							case MUL_D:
							case MUL_S:
								station.resultValue = String.valueOf(Double.parseDouble(station.Vj) * Double.parseDouble(station.Vk));
								break;
							case DIV_D:
							case DIV_S:
								station.resultValue = String.valueOf(Double.parseDouble(station.Vj) / Double.parseDouble(station.Vk));
								break;
							case DADDI:
							case DSUBI:
								station.resultValue = String.valueOf(Double.parseDouble(station.Vj) + Double.parseDouble(station.Vk));
								break;
							// Add cases for LOAD, STORE, and others as needed
							default:
								break;
						}
						System.out.println("Execution finished for station: " + station);
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

	private void writeResultStage() {
		List<ReservationStation> readyStations = new ArrayList<>();

		// Collect all stations ready to write their results
		for (ReservationStationGroup group : reservationStations) {
			for (ReservationStation station : group.stations) {
				if (station.executionEndCycle == cycle - 1 && station.busy) {
					readyStations.add(station); // Add ready station
				}
			}
		}
		if (!readyStations.isEmpty()) {
			// Find the most prioritized station
			ReservationStation prioritizedStation = readyStations.getFirst();
			int maxDependencies = -1;

			for (ReservationStation station : readyStations) {
				int dependencies = countDependencies(station);
				if (dependencies > maxDependencies) {
					maxDependencies = dependencies;
					prioritizedStation = station;
				}
			}

			// Broadcast result to CDB
			cdb.tag = prioritizedStation.tag;
			cdb.value = prioritizedStation.resultValue;

			System.out.println("Writing result from station: " + prioritizedStation);

			// Reset the prioritized reservation station
			prioritizedStation.reset();
		}
		checkBusRegisterFile();
		checkBusReservationStations();
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
		return false;
	}

	public void simulate() {
		while (!instructionQueue.isEmpty() || cycle < 100) {
			System.out.println("Cycle: " + cycle);
			if (canIssue() && !instructionQueue.isEmpty()) {
				issueStage();
			}
			executeStage();
			writeResultStage();
			cycle++;
			System.out.println("Register File: \n" + registerFile);
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

		// Create data cache
		Cache dataCache = new Cache(1024, 64, 1, 10);

		// Create processor
		Processor processor = new Processor(reservationStations, dataCache);

		// Add instructions to the processor
		processor.addInstruction(new Instruction("ADD_D", "F2", "F0", "F4"));
		processor.addInstruction(new Instruction("SUB_D", "F6", "F2", "F8"));
		processor.addInstruction(new Instruction("MUL_D", "F10", "F6", "F12"));
		processor.addInstruction(new Instruction("DIV_D", "F14", "F10", "F16"));
		processor.addInstruction(new Instruction("DADDI", "F14", "F1", "100"));

		// Provide Latency for each instruction
		InstructionType.setLatency(InstructionType.ADD_D, 2);
		InstructionType.setLatency(InstructionType.SUB_D, 2);
		InstructionType.setLatency(InstructionType.MUL_D, 10);
		InstructionType.setLatency(InstructionType.DIV_D, 40);
		InstructionType.setLatency(InstructionType.DADDI, 1);

		// Simulate the processor
		processor.simulate();
	}
}