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

import java.util.List;

public class MainApp extends Application {
	private static final String TITLE = "Tomasulo Simulator";
	private List<CycleState> cycleStates;
	private Processor processor;
	private List<Instruction> initialInstructions;
	private List<ReservationStationGroup> initialReservationStations;
	private List<LoadStoreBufferGroup> initialLoadStoreBuffers;
	private Data initialData;
	private int currentCycle = 0;

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
		VBox reservationStationsTable = createTableView("Reservation Stations");
		// Load Store Buffers
		VBox loadStoreBuffersTable = createTableView("Load Store Buffers");
		// Register File
		VBox registerFileTable = createTableView("Register File");
		// Memory
		VBox memoryTable = createTableView("Memory");
		// Instruction Queue
		VBox instructionQueueTable = createInstructionTableView();

		updateTables(reservationStationsTable, loadStoreBuffersTable, registerFileTable, memoryTable, instructionQueueTable);

		// Display Tables in one tab
		Tab tablesTab = new Tab("Tables");
		tablesTab.setContent(new VBox(10, reservationStationsTable, loadStoreBuffersTable, registerFileTable, memoryTable, instructionQueueTable));
		tabPane.getTabs().add(tablesTab);

		// Control Panel
		HBox controlPanel = createControlPanel();

		// Menu Bar
		MenuBar menuBar = createMenuBar();

		// Main Layout
		BorderPane mainLayout = new BorderPane();
		mainLayout.setTop(menuBar);
		mainLayout.setCenter(tabPane);
		mainLayout.setBottom(controlPanel);

		// Scene
		Scene scene = new Scene(mainLayout, 800, 600);
		//scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
		primaryStage.setTitle(TITLE);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private Processor initializeProcessor() {
		// Get user inputs
		initialInstructions = UserInputDialogs.getUserInstructions();
		initialReservationStations = UserInputDialogs.getUserReservationStations();
		initialLoadStoreBuffers = UserInputDialogs.getUserLoadStoreBuffers();
		initialData = UserInputDialogs.getUserCacheData();

		// Create processor
		Processor processor = new Processor(initialReservationStations, initialLoadStoreBuffers, initialData, initialInstructions);

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

		TableColumn<ReservationStation, String> TagColumn = new TableColumn<>("Tag");
		TagColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));
		TableColumn<ReservationStation, String> busyColumn = new TableColumn<>("Busy");
		busyColumn.setCellValueFactory(new PropertyValueFactory<>("busy"));
		TableColumn<ReservationStation, String> operationColumn = new TableColumn<>("Operation");
		operationColumn.setCellValueFactory(new PropertyValueFactory<>("operation"));
		TableColumn<ReservationStation, String> vJColumn = new TableColumn<>("Vj");
		vJColumn.setCellValueFactory(new PropertyValueFactory<>("vj"));
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

		tableView.getColumns().addAll(TagColumn, busyColumn, operationColumn, vJColumn, vKColumn, qJColumn, qKColumn, resultColumn, startColumn, endColumn);
//		tableView.getItems().addAll(cycleStates.getFirst().reservationStations.stream().map(ReservationStationGroup::getStations).collect(Collectors.toList()));
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

	private void updateTables(VBox reservationStationsTable, VBox loadStoreBuffersTable, VBox registerFileTable, VBox memoryTable, VBox instructionQueueTable) {
		// Update Reservation Stations
		((TableView<ReservationStationGroup>) reservationStationsTable.getChildren().get(1)).getItems().setAll(cycleStates.get(currentCycle).reservationStations);

		// Update Load Store Buffers
		((TableView<LoadStoreBufferGroup>) loadStoreBuffersTable.getChildren().get(1)).getItems().setAll(cycleStates.get(currentCycle).loadStoreBuffers);

		// Update Register File
		((TableView<RegisterFile>) registerFileTable.getChildren().get(1)).getItems().setAll(cycleStates.get(currentCycle).registerFile);

		// Update Memory
		((TableView<Data>) memoryTable.getChildren().get(1)).getItems().setAll(cycleStates.get(currentCycle).data);

		// Update Instruction Queue
		((TableView<Instruction>) instructionQueueTable.getChildren().get(1)).getItems().setAll(cycleStates.get(currentCycle).instructions);
	}

	private HBox createControlPanel() {
		Button startButton = new Button("Start");
		startButton.setTooltip(new Tooltip("Start the simulation"));
		Button nextCycleButton = new Button("Next Cycle");
		nextCycleButton.setTooltip(new Tooltip("Proceed to the next cycle"));
		Button resetButton = new Button("Reset");
		resetButton.setTooltip(new Tooltip("Reset the simulation"));
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