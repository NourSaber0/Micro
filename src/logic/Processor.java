package logic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static logic.InstructionType.getLatency;

public class Processor {
	private List<Instruction> instructionQueue = new ArrayList<>();
	private List<ReservationStationGroup> reservationStations = new ArrayList<>();
	public RegisterFile registerFile = new RegisterFile(32); // Example: 32 registers
	private CommonDataBus cdb = new CommonDataBus();
	private Cache dataCache;
	private Map<Integer, String> memory = new HashMap<>(); // Simulated main memory
	private int cycle = 0;

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
		Instruction instruction = instructionQueue.get(0);
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
			dest.setReady(false);

			// Update instruction metadata
			instruction.issueCycle = cycle;

			// Remove instruction from the queue
			instructionQueue.remove(0);

			System.out.println("Instruction issued: " + instruction);

		} catch (NumberFormatException | NullPointerException e) {
			System.err.println("Error issuing instruction: " + instruction + " - " + e.getMessage());
		}
	}

	private void checkBus(ReservationStation station) {
		if (cdb.tag.equals(station.Qj)) {
			station.Vj = cdb.value;
			station.Qj = "0";
		}
		if (cdb.tag.equals(station.Qk)) {
			station.Vk = cdb.value;
			station.Qk = "0";
		}
	}
	private void checkBusRegisterFile() {
		for (Register register : registerFile.registers) {
			if (cdb.tag.equals(register.tag)) {
				register.setValue(cdb.value);
				register.setReady(true);
				register.setTag("0");
			}
		}
	}
	private void executeStage() {
		for (ReservationStationGroup group : reservationStations) {
			for (ReservationStation station : group.stations) {
				if (station.busy && station.isReadyToExecute()) {
					if (station.executionStartCycle == 0) {
						station.executionStartCycle = cycle;
						station.executionEndCycle = cycle + getLatency(station.operation) -1;
						System.out.println("Executing instruction: " + station);
					} else if (station.executionEndCycle == cycle) {
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
								station.resultValue = String.valueOf(Integer.parseInt(station.Vj) + Integer.parseInt(station.Vk));
								break;
							// Add cases for LOAD, STORE, and others as needed
						}
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
				if (station.executionEndCycle + 1 == cycle) {
					readyStations.add(station); // Add ready station
				}
				checkBus(station);
			}
		}
		if (!readyStations.isEmpty()) {
			// Find the most prioritized station
			ReservationStation prioritizedStation = readyStations.get(0);
			int maxDependencies = -1;

			for (ReservationStation station : readyStations) {
				int dependencies = countDependencies(station);
				if (dependencies > maxDependencies) {
					maxDependencies = dependencies;
					prioritizedStation = station;
				}
			}

			// Write result to destination register
			Register dest = registerFile.getRegister(Integer.parseInt(prioritizedStation.tag.substring(1)));
			dest.setValue(prioritizedStation.resultValue);
			dest.setReady(true);
			dest.setTag("0");

			// Broadcast result to CDB
			cdb.tag = prioritizedStation.tag;
			cdb.value = prioritizedStation.resultValue;

			// Reset the prioritized reservation station
			prioritizedStation.reset();

			System.out.println("Writing result from station: " + prioritizedStation);
		}
		checkBusRegisterFile();
	}

	public void simulate(){
		while (!instructionQueue.isEmpty() || !reservationStations.isEmpty()) {
			System.out.println("Cycle: " + cycle);
			issueStage();
			executeStage();
			writeResultStage();
			cycle++;
		}
	}
}