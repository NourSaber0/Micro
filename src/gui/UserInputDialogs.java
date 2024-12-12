package gui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.*;

import java.util.ArrayList;
import java.util.List;

public class UserInputDialogs {

	public static List<Instruction> getUserInstructions() {
		// Implement dialog to get user instructions
		// Return list of instructions
		return new ArrayList<>();
	}

	public static List<ReservationStationGroup> getUserReservationStations() {
		// Implement dialog to get user reservation station sizes
		// Return list of reservation station groups
		return new ArrayList<>();
	}

	public static List<LoadStoreBufferGroup> getUserLoadStoreBuffers() {
		// Implement dialog to get user load/store buffer sizes
		// Return list of load/store buffer groups
		return new ArrayList<>();
	}

	public static Data getUserCacheData() {
		// Implement dialog to get user cache and memory sizes
		// Return Data object
		return new Data(5, 2, 2, 10, 100);
	}

	public static void setUserInstructionLatencies() {
		// Implement dialog to get user instruction latencies
		// Set latencies for each instruction type
	}
}