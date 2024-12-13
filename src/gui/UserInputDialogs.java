package gui;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import logic.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserInputDialogs {
	private static final String LINE_BREAK = "\\r?\\n";
	private static final boolean TESTING = true;

	private UserInputDialogs() {
	}

	public static List<Instruction> getUserInstructions() {
		List<Instruction> instructions = new ArrayList<>();
		TextInputDialog dialog = new TextInputDialog();
		TextArea textArea = new TextArea();
		dialog.setTitle("User Instructions");
		dialog.setHeaderText("Enter instructions, one per line, in the format: operation dest src1 src2");
		dialog.setContentText("Instructions:");
		dialog.getDialogPane().setContent(textArea);

		if (TESTING)
			textArea.setText("L_D F0 0 null\nADD_D F2 F0 F4\nSUB_D F6 F2 F2\nDADDI F6 F6 1\nDSUBI F4 F6 2\nBNE F4 3 null\nS_D F2 4 null\nMUL_D F10 F6 F12\nDIV_D F14 F10 F16\nDADDI F14 F1 100\nS_D F14 0 null");

		Optional<String> result = dialog.showAndWait().map(dialogButton -> textArea.getText());
		result.ifPresent(input -> {
			String[] lines = input.split(LINE_BREAK);
			for (String line : lines) {
				String[] parts = line.trim().split(" ");
				if (parts.length == 4) {
					instructions.add(new Instruction(parts[0], parts[1], parts[2], parts[3]));
				}
			}
		});

		return instructions;
	}

	public static List<ReservationStationGroup> getUserReservationStations() {
		List<ReservationStationGroup> reservationStations = new ArrayList<>();
		TextInputDialog dialog = new TextInputDialog();
		TextArea textArea = new TextArea();
		dialog.setTitle("Reservation Stations");
		dialog.setHeaderText("Enter reservation station operation type and size, one per line, in the format: operation size");
		dialog.setContentText("Reservation Stations:");
		dialog.getDialogPane().setContent(textArea);

		if (TESTING)
			textArea.setText("ADD_D 3\nSUB_D 2\nMUL_D 2\nDIV_D 2\nDADDI 1\nDSUBI 1");

		Optional<String> result = dialog.showAndWait().map(dialogButton -> textArea.getText());
		result.ifPresent(input -> {
			String[] lines = input.split(LINE_BREAK);
			for (String line : lines) {
				String[] parts = line.trim().split(" ");
				if (parts.length == 2) {
					InstructionType operation = InstructionType.valueOf(parts[0].toUpperCase());
					int size = Integer.parseInt(parts[1]);
					reservationStations.add(new ReservationStationGroup(size, operation));
				}
			}
		});

		return reservationStations;
	}

	public static List<LoadStoreBufferGroup> getUserLoadStoreBuffers() {
		List<LoadStoreBufferGroup> loadStoreBuffers = new ArrayList<>();
		TextInputDialog dialog = new TextInputDialog();
		TextArea textArea = new TextArea();
		dialog.setTitle("Load/Store Buffers");
		dialog.setHeaderText("Enter load/store buffer operation type and size, one per line, in the format: size operation");
		dialog.setContentText("Load/Store Buffers:");
		dialog.getDialogPane().setContent(textArea);

		if (TESTING)
			textArea.setText("L_D 2\nS_D 2");

		Optional<String> result = dialog.showAndWait().map(dialogButton -> textArea.getText());
		result.ifPresent(input -> {
			String[] lines = input.split(LINE_BREAK);
			for (String line : lines) {
				String[] parts = line.trim().split(" ");
				if (parts.length == 2) {
					InstructionType operation = InstructionType.valueOf(parts[0].toUpperCase());
					int size = Integer.parseInt(parts[1]);
					loadStoreBuffers.add(new LoadStoreBufferGroup(size, operation));
				}
			}
		});

		return loadStoreBuffers;
	}

	public static Data getUserCacheData() {
		TextInputDialog dialog = new TextInputDialog();
		TextArea textArea = new TextArea();
		dialog.setTitle("Cache Data");
		dialog.setHeaderText("Enter cache size, block size, hit latency, miss penalty, and memory size, one per line, in the format: cacheSize blockSize hitLatency missPenalty memorySize");
		dialog.setContentText("Cache Data:");
		dialog.getDialogPane().setContent(textArea);

		if (TESTING)
			textArea.setText("5 2 2 10 100");

		Optional<String> result = dialog.showAndWait().map(dialogButton -> textArea.getText());
		if (result.isPresent()) {
			String input = result.get();
			String[] parts = input.trim().split(" ");
			if (parts.length == 5) {
				int cacheSize = Integer.parseInt(parts[0]);
				int blockSize = Integer.parseInt(parts[1]);
				int hitLatency = Integer.parseInt(parts[2]);
				int missPenalty = Integer.parseInt(parts[3]);
				int memorySize = Integer.parseInt(parts[4]);
				return new Data(cacheSize, blockSize, hitLatency, missPenalty, memorySize);
			}
		}

		return null;
	}

	public static void setUserInstructionLatencies() {
		TextInputDialog dialog = new TextInputDialog();
		TextArea textArea = new TextArea();
		dialog.setTitle("Instruction Latencies");
		dialog.setHeaderText("Enter instruction type and latency, one per line, in the format: instructionType latency");
		dialog.setContentText("Instruction Latencies:");
		dialog.getDialogPane().setContent(textArea);

		if (TESTING)
			textArea.setText("ADD_D 2\nSUB_D 2\nMUL_D 10\nDIV_D 40\nDADDI 1\nDSUBI 1\nL_D 2\nS_D 2\nBNE 1");

		Optional<String> result = dialog.showAndWait().map(dialogButton -> textArea.getText());
		result.ifPresent(input -> {
			String[] lines = input.split(LINE_BREAK);
			for (String line : lines) {
				String[] parts = line.trim().split(" ");
				if (parts.length == 2) {
					InstructionType type = InstructionType.valueOf(parts[0].toUpperCase());
					int latency = Integer.parseInt(parts[1]);
					InstructionType.setLatency(type, latency);
				}
			}
		});
	}

	public static int getUserRegisterFileSize() {
		TextInputDialog dialog = new TextInputDialog();
		TextArea textArea = new TextArea();
		dialog.setTitle("Register File Size");
		dialog.setHeaderText("Enter the number of registers in the register file");
		dialog.setContentText("Register File Size:");
		dialog.getDialogPane().setContent(textArea);

		if (TESTING)
			textArea.setText("32");

		Optional<String> result = dialog.showAndWait().map(dialogButton -> textArea.getText());
		if (result.isPresent()) {
			String input = result.get();
			return Integer.parseInt(input.trim());
		}

		return 0;
	}
}