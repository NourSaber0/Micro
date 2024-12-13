package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainApp extends Application {
	private static final String TITLE = "Tomasulo Simulator";
	private List<CycleState> cycleStates;
	private Processor processor;
	private int currentCycle = 0;
	private Label currentCycleLabel;
	private TextField cycleInputField;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		processor = initializeProcessor();
		cycleStates = processor.getCycleStates();

		// Main Layout
		TabPane tabPane = new TabPane();
		// Reservation Stations
		VBox reservationStationsTable = createReservationStationTableView();
		// Load Store Buffers
		VBox loadStoreBuffersTable = createLoadStoreBufferTableView();
		// Register File
		VBox registerFileTable = createRegisterFileTableView();
		// Memory
		VBox memoryTable = createMemoryTableView();
		// Cache
		VBox cacheTable = createCacheTableView();

		// Instruction Queue
		VBox instructionQueueTable = createInstructionTableView();

		updateTables(reservationStationsTable, loadStoreBuffersTable, registerFileTable, memoryTable, cacheTable, instructionQueueTable);

		// Display Tables in one tab
		Tab tablesTab = new Tab("Tables");
		tablesTab.setContent(new VBox(10, reservationStationsTable, loadStoreBuffersTable, registerFileTable, memoryTable, cacheTable, instructionQueueTable));
		tabPane.getTabs().add(tablesTab);

		currentCycleLabel = new Label("Current Cycle: 0");
		cycleInputField = new TextField();
		cycleInputField.setPromptText("Enter cycle number");
		Button skipCycleButton = new Button("Skip to Cycle");
		skipCycleButton.setOnAction(e -> skipToCycle(reservationStationsTable, loadStoreBuffersTable, registerFileTable, memoryTable, cacheTable, instructionQueueTable));


		// Control Panel
		HBox controlPanel = createControlPanel(reservationStationsTable, loadStoreBuffersTable, registerFileTable, memoryTable, cacheTable, instructionQueueTable);
		controlPanel.getChildren().addAll(currentCycleLabel, cycleInputField, skipCycleButton);

		// Menu Bar
		MenuBar menuBar = createMenuBar();

		// Main Layout
		BorderPane mainLayout = new BorderPane();
		mainLayout.setTop(menuBar);
		mainLayout.setCenter(tabPane);
		mainLayout.setBottom(controlPanel);

		// Scene
		Scene scene = new Scene(mainLayout, 800, 600);
		primaryStage.setTitle(TITLE);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void skipToCycle(VBox reservationStationsTable, VBox loadStoreBuffersTable, VBox registerFileTable, VBox memoryTable, VBox cacheTable, VBox instructionQueueTable) {
		try {
			int cycle = Integer.parseInt(cycleInputField.getText());
			if (cycle >= 0 && cycle < cycleStates.size()) {
				currentCycle = cycle;
				currentCycleLabel.setText("Current Cycle: " + currentCycle);
				updateTables(reservationStationsTable, loadStoreBuffersTable, registerFileTable, memoryTable, cacheTable, instructionQueueTable);
			} else {
				showAlert("Invalid Cycle", "Please enter a valid cycle number.");
			}
		} catch (NumberFormatException e) {
			showAlert("Invalid Input", "Please enter a valid number.");
		}
	}

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private Processor initializeProcessor() {
		// Get user inputs
		List<Instruction> instructions = UserInputDialogs.getUserInstructions();
		List<ReservationStationGroup> reservationStations = UserInputDialogs.getUserReservationStations();
		List<LoadStoreBufferGroup> loadStoreBuffers = UserInputDialogs.getUserLoadStoreBuffers();
		Data data = UserInputDialogs.getUserCacheData();
		int registerFileSize = UserInputDialogs.getUserRegisterFileSize();

		// Create processor
		Processor processor = new Processor(reservationStations, loadStoreBuffers, data, instructions, registerFileSize);

		// Provide Latency for each instruction
		UserInputDialogs.setUserInstructionLatencies();

		return processor;
	}

	private VBox createTableView(String title) {
		TableView tableView = new TableView();
		Label label = new Label(title);
		VBox vBox = new VBox(10, label, tableView);
		vBox.setStyle("-fx-padding: 10; -fx-border-style: solid inside; -fx-border-width: 2; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: gray;");
		return vBox;
	}

	private VBox createReservationStationTableView() {
		TableView<ReservationStation> tableView = new TableView<>();
		Label label = new Label("Reservation Stations");
		VBox vBox = new VBox(10, label, tableView);
		vBox.setStyle("-fx-padding: 10; -fx-border-style: solid inside; -fx-border-width: 2; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: gray;");

		TableColumn<ReservationStation, String> tagColumn = new TableColumn<>("Tag");
		tagColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));
		TableColumn<ReservationStation, String> busyColumn = new TableColumn<>("Busy");
		busyColumn.setCellValueFactory(new PropertyValueFactory<>("busy"));
		TableColumn<ReservationStation, String> operationColumn = new TableColumn<>("Operation");
		operationColumn.setCellValueFactory(new PropertyValueFactory<>("operation"));
		TableColumn<ReservationStation, String> vJColumn = new TableColumn<>("Vj");
		vJColumn.setCellValueFactory(new PropertyValueFactory<>("vJ"));
		TableColumn<ReservationStation, String> vKColumn = new TableColumn<>("Vk");
		vKColumn.setCellValueFactory(new PropertyValueFactory<>("vK"));
		TableColumn<ReservationStation, String> qJColumn = new TableColumn<>("Qj");
		qJColumn.setCellValueFactory(new PropertyValueFactory<>("qJ"));
		TableColumn<ReservationStation, String> qKColumn = new TableColumn<>("Qk");
		qKColumn.setCellValueFactory(new PropertyValueFactory<>("qK"));
		TableColumn<ReservationStation, String> resultColumn = new TableColumn<>("Result");
		resultColumn.setCellValueFactory(new PropertyValueFactory<>("resultValue"));
		TableColumn<ReservationStation, String> startColumn = new TableColumn<>("Start Cycle");
		startColumn.setCellValueFactory(new PropertyValueFactory<>("executionStartCycle"));
		TableColumn<ReservationStation, String> endColumn = new TableColumn<>("End Cycle");
		endColumn.setCellValueFactory(new PropertyValueFactory<>("executionEndCycle"));

		tableView.getColumns().addAll(tagColumn, busyColumn, operationColumn, vJColumn, vKColumn, qJColumn, qKColumn, resultColumn, startColumn, endColumn);

		ArrayList<ReservationStation> stations = new ArrayList<>();
		for (ReservationStationGroup group : cycleStates.getFirst().reservationStations) {
			stations.addAll(Arrays.stream(group.getStations()).toList());
		}

		tableView.getItems().addAll(stations);
		return vBox;
	}

	private VBox createLoadStoreBufferTableView() {
		TableView<LoadStoreBuffer> tableView = new TableView<>();
		Label label = new Label("Load Store Buffers");
		VBox vBox = new VBox(10, label, tableView);
		vBox.setStyle("-fx-padding: 10; -fx-border-style: solid inside; -fx-border-width: 2; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: gray;");

		TableColumn<LoadStoreBuffer, String> addressColumn = new TableColumn<>("Address");
		addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
		TableColumn<LoadStoreBuffer, String> valueColumn = new TableColumn<>("Value");
		valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
		TableColumn<LoadStoreBuffer, String> tagColumn = new TableColumn<>("Tag");
		tagColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));
		TableColumn<LoadStoreBuffer, String> qColumn = new TableColumn<>("Q");
		qColumn.setCellValueFactory(new PropertyValueFactory<>("q"));
		TableColumn<LoadStoreBuffer, String> operationColumn = new TableColumn<>("Operation");
		operationColumn.setCellValueFactory(new PropertyValueFactory<>("operation"));
		TableColumn<LoadStoreBuffer, String> busyColumn = new TableColumn<>("Busy");
		busyColumn.setCellValueFactory(new PropertyValueFactory<>("busy"));
		TableColumn<LoadStoreBuffer, String> startColumn = new TableColumn<>("Start Cycle");
		startColumn.setCellValueFactory(new PropertyValueFactory<>("executionStartCycle"));
		TableColumn<LoadStoreBuffer, String> endColumn = new TableColumn<>("End Cycle");
		endColumn.setCellValueFactory(new PropertyValueFactory<>("executionEndCycle"));

		tableView.getColumns().addAll(tagColumn, busyColumn, addressColumn, valueColumn, qColumn, operationColumn, startColumn, endColumn);

		ArrayList<LoadStoreBuffer> buffers = new ArrayList<>();
		for (LoadStoreBufferGroup group : cycleStates.getFirst().loadStoreBuffers) {
			buffers.addAll(Arrays.stream(group.getBuffers()).toList());
		}

		tableView.getItems().addAll(buffers);
		return vBox;
	}

	private VBox createRegisterFileTableView() {
		TableView<Register> tableView = new TableView<>();
		Label label = new Label("Register File");
		VBox vBox = new VBox(10, label, tableView);
		vBox.setStyle("-fx-padding: 10; -fx-border-style: solid inside; -fx-border-width: 2; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: gray;");

		TableColumn<Register, String> nameColumn = new TableColumn<>("Name");
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		TableColumn<Register, String> valueColumn = new TableColumn<>("Value");
		valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
		TableColumn<Register, String> tagColumn = new TableColumn<>("Tag");
		tagColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));

		tableView.getColumns().addAll(nameColumn, valueColumn, tagColumn);
		tableView.getItems().addAll(cycleStates.getFirst().registerFile.getRegisters());

		return vBox;
	}

	private VBox createMemoryTableView() {
		TableView<Data.MemoryEntry> tableView = new TableView<>();
		Label label = new Label("Memory");
		VBox vBox = new VBox(10, label, tableView);
		vBox.setStyle("-fx-padding: 10; -fx-border-style: solid inside; -fx-border-width: 2; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: gray;");

		TableColumn<Data.MemoryEntry, Integer> indexColumn = new TableColumn<>("Address");
		indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
		TableColumn<Data.MemoryEntry, String> valueColumn = new TableColumn<>("Value");
		valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

		tableView.getColumns().addAll(indexColumn, valueColumn);

		// Populate the table with memory data
		List<Data.MemoryEntry> memoryEntries = cycleStates.getFirst().data.getMemoryEntries();
		tableView.getItems().addAll(memoryEntries);

		return vBox;
	}

	private VBox createCacheTableView() {
		TableView<Data.CacheEntry> tableView = new TableView<>();
		Label label = new Label("Cache");
		VBox vBox = new VBox(10, label, tableView);
		vBox.setStyle("-fx-padding: 10; -fx-border-style: solid inside; -fx-border-width: 2; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: gray;");

		TableColumn<Data.CacheEntry, Integer> indexColumn = new TableColumn<>("Address");
		indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
		TableColumn<Data.CacheEntry, String> tagColumn = new TableColumn<>("Tag");
		tagColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));
		TableColumn<Data.CacheEntry, String> dataColumn = new TableColumn<>("Data");
		dataColumn.setCellValueFactory(new PropertyValueFactory<>("data"));

		tableView.getColumns().addAll(indexColumn, tagColumn, dataColumn);

		// Populate the table with cache data
		List<Data.CacheEntry> cacheEntries = cycleStates.getFirst().data.getCacheEntries();
		tableView.getItems().addAll(cacheEntries);

		return vBox;
	}

	private VBox createInstructionTableView() {
		TableView<Instruction> tableView = new TableView<>();
		Label label = new Label("Instruction Queue");
		VBox vBox = new VBox(10, label, tableView);
		vBox.setStyle("-fx-padding: 10; -fx-border-style: solid inside; -fx-border-width: 2; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: gray;");

		TableColumn<Instruction, String> operationColumn = new TableColumn<>("Operation");
		operationColumn.setCellValueFactory(new PropertyValueFactory<>("operation"));
		TableColumn<Instruction, String> destColumn = new TableColumn<>("Destination");
		destColumn.setCellValueFactory(new PropertyValueFactory<>("dest"));
		TableColumn<Instruction, String> src1Column = new TableColumn<>("Source 1");
		src1Column.setCellValueFactory(new PropertyValueFactory<>("src1"));
		TableColumn<Instruction, String> src2Column = new TableColumn<>("Source 2");
		src2Column.setCellValueFactory(new PropertyValueFactory<>("src2"));

		tableView.getColumns().addAll(operationColumn, destColumn, src1Column, src2Column);
		tableView.getItems().addAll(cycleStates.getFirst().instructions);

		return vBox;
	}

	private void updateTables(VBox reservationStationsTable, VBox loadStoreBuffersTable, VBox registerFileTable, VBox memoryTable, VBox cacheTable, VBox instructionQueueTable) {
		// Update Reservation Stations
		List<ReservationStation> stations = new ArrayList<>();
		for (ReservationStationGroup group : cycleStates.get(currentCycle).reservationStations) {
			stations.addAll(Arrays.stream(group.getStations()).toList());
		}
		((TableView<ReservationStation>) reservationStationsTable.getChildren().get(1)).getItems().setAll(stations);

		// Update Load Store Buffers
		List<LoadStoreBuffer> buffers = new ArrayList<>();
		for (LoadStoreBufferGroup group : cycleStates.get(currentCycle).loadStoreBuffers) {
			buffers.addAll(Arrays.stream(group.getBuffers()).toList());
		}
		((TableView<LoadStoreBuffer>) loadStoreBuffersTable.getChildren().get(1)).getItems().setAll(buffers);

		// Update Register File
		((TableView<Register>) registerFileTable.getChildren().get(1)).getItems().setAll(cycleStates.get(currentCycle).registerFile.getRegisters());

		// Update Memory
		List<Data.MemoryEntry> memoryEntries = cycleStates.get(currentCycle).data.getMemoryEntries();
		((TableView<Data.MemoryEntry>) memoryTable.getChildren().get(1)).getItems().setAll(memoryEntries);

		// Update Cache
		List<Data.CacheEntry> cacheEntries = cycleStates.get(currentCycle).data.getCacheEntries();
		((TableView<Data.CacheEntry>) cacheTable.getChildren().get(1)).getItems().setAll(cacheEntries);

		// Update Instruction Queue
		((TableView<Instruction>) instructionQueueTable.getChildren().get(1)).getItems().setAll(cycleStates.get(currentCycle).instructions);
	}

	private void updateCycle(VBox reservationStationsTable, VBox loadStoreBuffersTable, VBox registerFileTable, VBox memoryTable, VBox cacheTable, VBox instructionQueueTable) {
		currentCycle++;
		if (currentCycle >= cycleStates.size()) {
			showAlert("Simulation Finished", "The simulation has finished.");
			currentCycle--;
		}
		currentCycleLabel.setText("Current Cycle: " + currentCycle);
		updateTables(reservationStationsTable, loadStoreBuffersTable, registerFileTable, memoryTable, cacheTable, instructionQueueTable);
	}

	private void reset(VBox reservationStationsTable, VBox loadStoreBuffersTable, VBox registerFileTable, VBox memoryTable, VBox cacheTable, VBox instructionQueueTable) {
		currentCycle = 0;
		currentCycleLabel.setText("Current Cycle: 0");
		updateTables(reservationStationsTable, loadStoreBuffersTable, registerFileTable, memoryTable, cacheTable, instructionQueueTable);
	}

	private void start() {
		processor.simulateAll();
		cycleStates = processor.getCycleStates();
	}

	private HBox createControlPanel(VBox reservationStationsTable, VBox loadStoreBuffersTable, VBox registerFileTable, VBox memoryTable, VBox cacheTable, VBox instructionQueueTable) {
		Button startButton = new Button("Start");
		startButton.setTooltip(new Tooltip("Start the simulation"));
		startButton.setOnAction(e -> start());
		Button nextCycleButton = new Button("Next Cycle");
		nextCycleButton.setTooltip(new Tooltip("Proceed to the next cycle"));
		nextCycleButton.setOnAction(e -> updateCycle(reservationStationsTable, loadStoreBuffersTable, registerFileTable, memoryTable, cacheTable, instructionQueueTable));
		Button resetButton = new Button("Reset");
		resetButton.setTooltip(new Tooltip("Reset the simulation"));
		resetButton.setOnAction(e -> reset(reservationStationsTable, loadStoreBuffersTable, registerFileTable, memoryTable, cacheTable, instructionQueueTable));
		HBox controlPanel = new HBox(10, startButton, nextCycleButton, resetButton);
		controlPanel.setStyle("-fx-padding: 10; -fx-alignment: center;");
		return controlPanel;
	}

	private MenuBar createMenuBar() {
		MenuBar menuBar = new MenuBar();
		Menu fileMenu = new Menu("File");
		MenuItem loadItem = new MenuItem("Load Instructions");
		MenuItem exitItem = new MenuItem("Exit");
		exitItem.setOnAction(e -> System.exit(0));
		fileMenu.getItems().addAll(loadItem, exitItem);

		Menu editMenu = new Menu("Edit");
		Menu helpMenu = new Menu("Help");

		menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);
		return menuBar;
	}


}