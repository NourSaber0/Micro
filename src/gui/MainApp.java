package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.*;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {
	private static final String TITLE = "JavaFX Application";
	private List<CycleState> cycleStates;

	public static void main(String[] args) {
		launch(args);
	}

	private Processor initializeProcessor() {
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
		Data data = new Data(5, 2, 2, 10, 100);
		// Create processor
		Processor processor = new Processor(reservationStations, loadStoreBuffers, data);

		// Add instructions to the processor
		processor.addInstruction(new Instruction("L_D", "F0", "0", null));
		processor.addInstruction(new Instruction("ADD_D", "F2", "F0", "F4"));
		processor.addInstruction(new Instruction("SUB_D", "F6", "F2", "F8"));
		processor.addInstruction(new Instruction("MUL_D", "F10", "F6", "F12"));
		processor.addInstruction(new Instruction("DIV_D", "F14", "F10", "F16"));
		processor.addInstruction(new Instruction("DADDI", "F14", "F1", "100"));
		processor.addInstruction(new Instruction("S_D", "F14", "0", null));

		// Provide Latency for each instruction
		InstructionType.setLatency(InstructionType.ADD_D, 2);
		InstructionType.setLatency(InstructionType.SUB_D, 2);
		InstructionType.setLatency(InstructionType.MUL_D, 10);
		InstructionType.setLatency(InstructionType.DIV_D, 40);
		InstructionType.setLatency(InstructionType.DADDI, 1);

		return processor;
	}

	@Override
	public void start(Stage primaryStage) {
		Processor processor = initializeProcessor();
		processor.simulateAll();
		cycleStates = processor.getCycleStates();

		ListView<String> listView = new ListView<>();
		for (CycleState cycleState : cycleStates) {
			listView.getItems().add("Cycle " + cycleState.cycle + ": \n" + cycleState.toString());
		}

		VBox vbox = new VBox(listView);
		Scene scene = new Scene(vbox, 800, 600);
		primaryStage.setScene(scene);
		primaryStage.setTitle(TITLE);
		primaryStage.show();
	}
}