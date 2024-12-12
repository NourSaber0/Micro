package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.*;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {
	private static final String TITLE = "Tomasulo Simulator";
	private List<CycleState> cycleStates;
	private Processor processor;

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
		VBox instructionQueueTable = createTableView("Instruction Queue");
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
		List<Instruction> instructions = UserInputDialogs.getUserInstructions();
		List<ReservationStationGroup> reservationStations = UserInputDialogs.getUserReservationStations();
		List<LoadStoreBufferGroup> loadStoreBuffers = UserInputDialogs.getUserLoadStoreBuffers();
		Data data = UserInputDialogs.getUserCacheData();

		// Create processor
		Processor processor = new Processor(reservationStations, loadStoreBuffers, data);

		// Add instructions to the processor
		for (Instruction instruction : instructions) {
			processor.addInstruction(instruction);
		}

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