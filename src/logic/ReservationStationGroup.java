package logic;


public class ReservationStationGroup {
	ReservationStation[] stations;
	InstructionType operation;

	public ReservationStationGroup(int size, InstructionType operation) {
		this.operation = operation;
		stations = new ReservationStation[size];
		for (int i = 0; i < size; i++) {
			stations[i] = new ReservationStation(operation.name() + i);
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(operation + ": \n");
		for (ReservationStation station : stations) {
			sb.append(station.toString()).append("\n");
		}
		return sb.toString();
	}
}
