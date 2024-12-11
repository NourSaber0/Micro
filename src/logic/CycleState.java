package logic;

import java.util.List;

public class CycleState {
	public List<ReservationStationGroup> reservationStations;
	public List<LoadStoreBufferGroup> loadStoreBuffers;
	public CommonDataBus commonDataBus;
	public List<Instruction> instructions;
	public int cycle;
	public boolean isFinished;

	public CycleState(List<ReservationStationGroup> reservationStations, List<LoadStoreBufferGroup> loadStoreBuffers, CommonDataBus commonDataBus, List<Instruction> instructions, int cycle, boolean isFinished) {
		this.reservationStations = reservationStations;
		this.loadStoreBuffers = loadStoreBuffers;
		this.commonDataBus = commonDataBus;
		this.instructions = instructions;
		this.cycle = cycle;
		this.isFinished = isFinished;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Reservation Station Groups: \n");
		for (ReservationStationGroup reservationStationGroup : reservationStations) {
			sb.append(reservationStationGroup.toString());
			sb.append("\n");
		}
		sb.append("Load Store Buffers: \n");
		for (LoadStoreBufferGroup loadStoreBufferGroups : loadStoreBuffers) {
			sb.append(loadStoreBufferGroups.toString());
			sb.append("\n");
		}
		sb.append("Common Data Bus: ");
		sb.append(commonDataBus.toString());
		sb.append("\n");
		sb.append("Instructions: ");
		for (Instruction instruction : instructions) {
			sb.append(instruction.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}
