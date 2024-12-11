//package logic;
//
//import javafx.application.Application;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.geometry.Insets;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.stage.Stage;
//
//public class TomasuloSimulatorApp extends Application {
//
//	private Processor processor;
//	private TableView<ReservationStation> reservationStationTable;
//	private TableView<LoadStoreBuffer> loadStoreBufferTable;
//	private TableView<Register> registerTable;
//	private TextArea logArea;
//
//	@Override
//	public void start(Stage primaryStage) {
//		primaryStage.setTitle("Tomasulo Algorithm Simulator");
//
//		// Initialize components
//		VBox root = new VBox(10);
//		root.setPadding(new Insets(10));
//
//		// Input Panel
//		HBox inputPanel = createInputPanel();
//		root.getChildren().add(inputPanel);
//
//		// Tables
//		reservationStationTable = createReservationStationTable();
//		loadStoreBufferTable = createLoadStoreBufferTable();
//		registerTable = createRegisterTable();
//		root.getChildren().addAll(
//				new Label("Reservation Stations"), reservationStationTable,
//				new Label("Load/Store Buffers"), loadStoreBufferTable,
//				new Label("Registers"), registerTable
//		);
//
//		// Log Area
//		logArea = new TextArea();
//		logArea.setEditable(false);
//		logArea.setPrefHeight(200);
//		root.getChildren().add(new Label("Execution Log"));
//		root.getChildren().add(logArea);
//
//		// Initialize Processor
//		initializeProcessor();
//
//		// Scene
//		Scene scene = new Scene(root, 1000, 800);
//		primaryStage.setScene(scene);
//		primaryStage.show();
//	}
//
//	private HBox createInputPanel() {
//		HBox panel = new HBox(10);
//
//		// Start Simulation Button
//		Button startSimulationButton = new Button("Start Simulation");
//		startSimulationButton.setOnAction(e -> startSimulation());
//
//		// Cycle Button
//		Button nextCycleButton = new Button("Next Cycle");
//		nextCycleButton.setOnAction(e -> nextCycle());
//
//		panel.getChildren().addAll(startSimulationButton, nextCycleButton);
//		return panel;
//	}
//
//	private TableView<ReservationStation> createReservationStationTable() {
//		TableView<ReservationStation> table = new TableView<>();
//		TableColumn<ReservationStation, String> opColumn = new TableColumn<>("Operation");
//		opColumn.setCellValueFactory(data -> data.getValue().operationProperty());
//		TableColumn<ReservationStation, Boolean> busyColumn = new TableColumn<>("Busy");
//		busyColumn.setCellValueFactory(data -> data.getValue().busyProperty());
//		TableColumn<ReservationStation, String> vjColumn = new TableColumn<>("Vj");
//		vjColumn.setCellValueFactory(data -> data.getValue().vjProperty());
//		TableColumn<ReservationStation, String> vkColumn = new TableColumn<>("Vk");
//		vkColumn.setCellValueFactory(data -> data.getValue().vkProperty());
//		TableColumn<ReservationStation, String> qjColumn = new TableColumn<>("Qj");
//		qjColumn.setCellValueFactory(data -> data.getValue().qjProperty());
//		TableColumn<ReservationStation, String> qkColumn = new TableColumn<>("Qk");
//		qkColumn.setCellValueFactory(data -> data.getValue().qkProperty());
//
//		table.getColumns().addAll(opColumn, busyColumn, vjColumn, vkColumn, qjColumn, qkColumn);
//		return table;
//	}
//
//	private TableView<LoadStoreBuffer> createLoadStoreBufferTable() {
//		TableView<LoadStoreBuffer> table = new TableView<>();
//		TableColumn<LoadStoreBuffer, String> opColumn = new TableColumn<>("Operation");
//		opColumn.setCellValueFactory(data -> data.getValue().operationProperty());
//		TableColumn<LoadStoreBuffer, Boolean> busyColumn = new TableColumn<>("Busy");
//		busyColumn.setCellValueFactory(data -> data.getValue().busyProperty());
//		TableColumn<LoadStoreBuffer, String> addressColumn = new TableColumn<>("Address");
//		addressColumn.setCellValueFactory(data -> data.getValue().addressProperty());
//		TableColumn<LoadStoreBuffer, String> valueColumn = new TableColumn<>("Value");
//		valueColumn.setCellValueFactory(data -> data.getValue().valueProperty());
//
//		table.getColumns().addAll(opColumn, busyColumn, addressColumn, valueColumn);
//		return table;
//	}
//
//	private TableView<Register> createRegisterTable() {
//		TableView<Register> table = new TableView<>();
//		TableColumn<Register, String> regColumn = new TableColumn<>("Register");
//		regColumn.setCellValueFactory(data -> data.getValue().nameProperty());
//		TableColumn<Register, String> valueColumn = new TableColumn<>("Value");
//		valueColumn.setCellValueFactory(data -> data.getValue().valueProperty());
//		TableColumn<Register, String> tagColumn = new TableColumn<>("Tag");
//		tagColumn.setCellValueFactory(data -> data.getValue().tagProperty());
//		table.getColumns().addAll(regColumn, valueColumn, tagColumn);
//		return table;
//	}
//
//	private void initializeProcessor() {
//		// Create Reservation Stations
//		List<ReservationStationGroup> reservationStations = new ArrayList<>();
//		reservationStations.add(new ReservationStationGroup(3, InstructionType.ADD_D));
//		reservationStations.add(new ReservationStationGroup(2, InstructionType.SUB_D));
//		reservationStations.add(new ReservationStationGroup(2, InstructionType.MUL_D));
//		reservationStations.add(new ReservationStationGroup(2, InstructionType.DIV_D));
//		reservationStations.add(new ReservationStationGroup(1, InstructionType.DADDI));
//
//		// Create Load/Store Buffers
//		List<LoadStoreBufferGroup> loadStoreBuffers = new ArrayList<>();
//		loadStoreBuffers.add(new LoadStoreBufferGroup(2, InstructionType.L_D));
//		loadStoreBuffers.add(new LoadStoreBufferGroup(2, InstructionType.S_D));
//
//		// Create Data
//		Data data = new Data(5, 2, 2, 10, 100);
//
//		// Initialize Processor
//		processor = new Processor(reservationStations, loadStoreBuffers, data);
//
//		// Add Instructions
//		processor.addInstruction(new Instruction("L_D", "F0", "0", null));
//		processor.addInstruction(new Instruction("ADD_D", "F2", "F0", "F4"));
//		processor.addInstruction(new Instruction("SUB_D", "F6", "F2", "F8"));
//		processor.addInstruction(new Instruction("MUL_D", "F10", "F6", "F12"));
//		processor.addInstruction(new Instruction("DIV_D", "F14", "F10", "F16"));
//		processor.addInstruction(new Instruction("DADDI", "F14", "F1", "100"));
//		processor.addInstruction(new Instruction("S_D", "F14", "12", null));
//
//		// Set Latency for Instructions
//		InstructionType.setLatency(InstructionType.ADD_D, 2);
//		InstructionType.setLatency(InstructionType.SUB_D, 2);
//		InstructionType.setLatency(InstructionType.MUL_D, 10);
//		InstructionType.setLatency(InstructionType.DIV_D, 40);
//		InstructionType.setLatency(InstructionType.DADDI, 1);
//
//		updateTables();
//	}
//
//	private void updateTables() {
//		reservationStationTable.setItems(FXCollections.observableArrayList(processor.getReservationStations()));
//		loadStoreBufferTable.setItems(FXCollections.observableArrayList(processor.getLoadStoreBuffers()));
//		registerTable.setItems(FXCollections.observableArrayList(processor.getRegisterFile().getRegisters()));
//	}
//
//	private void startSimulation() {
//		while (!processor.isSimulationComplete()) {
//			processor.simulate();
//			updateTables();
//			logArea.appendText("Cycle " + processor.getCycle() + " complete.\n");
//		}
//	}
//
//	private void nextCycle() {
//		processor.simulate();
//		updateTables();
//		logArea.appendText("Cycle " + processor.getCycle() + " complete.\n");
//	}
//
//	public static void main(String[] args) {
//		launch(args);
//	}
//}