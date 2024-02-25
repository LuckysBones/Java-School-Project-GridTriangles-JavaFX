/*-
 * This software is the work of Dr. Bruce K. Haddon (hereafter, "the Owner"), and
 * all rights and intellectual property remain the property of that person.
 *
 * Rights to view or use this software as source code, or for execution, are only
 * granted via one or more licences at the discretion the Owner. In any event, the
 * grant to the "Licensee" shall be for a non-exclusive, non-transferable license
 * to view or use this software version (hereafter, "the Software") according to
 * the terms of the licence and contract executed between the Licensee and the
 * Owner. Licensee agrees that the copyright notice and this statement will appear
 * on all copies of the Software, packaging, and documentation or portions thereof
 * made under the terms of the license and contract.
 *
 * Please refer to the your license and contract for further important copyright
 * and licensing information. If you are reading this, and you do not have a
 * signed, current license or confidentiality agreement executed with the Owner,
 * it is because someone has violated the terms of an agreement, an act to which
 * you may be held to be a party.
 *
 * The Owner makes no representations or warranties about the suitability of the
 * Software, either express or implied, including but not limited to the implied
 * warranties of merchantability, fitness for particular purposes, or
 * non-infringement, other than those contained in the Licensee's license and
 * contract documents. The Owner shall not be liable for any damages suffered by
 * the Licensee as a result of using, modifying or distributing this software or
 * its derivatives.
 *
 * Irrespective of the conditions above, permission is granted to the students and
 * staff of the Front Range Community College, Colorado, to use read and use this
 * class, but not to make amendments or to distributed this class to other parties.
 *
 * Copyright 2000-2019 Dr. Bruce K. Haddon
 */
package software.haddon.util;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import static java.lang.System.exit;
import static java.lang.System.lineSeparator;
import static java.util.Objects.nonNull;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.NONE;
import static javafx.scene.control.ButtonType.FINISH;

/**
 * This (static) class defines a number of methods for creating dialogs or realtime
 * windows, with controls to ensure that multiple messages do not display at the
 * same time. The dialog or window is always shown on the Platform thread. There is
 * no return until there has been a response (or, for a temporary message, the time
 * elapses, or for a window, an insertion has been made), unless (if there
 * is a {@code wait} parameter), in which case, if this is set to false, and there
 * is another message showing, return is immediate (with some defined default
 * result).
 *
 * @author		Dr. Bruce K. Haddon, Instructor
 * @version		8.0, 2021-03-29
 */
@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class FXMessage
{
/**
 * Response value if CANCELED is chosen.
 */
public static final int CANCELED = 0;

/**
 * Response value if NO is chosen.
 */
public static final int NO = -1;

/**
 * Response value if OK/YES is chosen.
 */
public static final int OK = +1;

/**
 * Queue used for button results. This is a static final because is can be.
 */
private static final ArrayBlockingQueue<Optional<ButtonType>> BUTTON =
		new ArrayBlockingQueue<>(1);

/**
 * Default height for the scrollable area in scrollDisplay
 */
private static final double DEFAULT_HEIGHT = 200.0;

/**
 * Default offset for messages relative to bottom of owner.
 */
private static final double DEFAULT_MARGIN = 0.0;

/**
 * Default width for the scrollable area in scrollDisplay
 */
private static final double DEFAULT_WIDTH = 415.0;

/**
 * Queue used for String results. This is a static final because is can be.
 */
private static final ArrayBlockingQueue<Optional<String>> ENTERED =
		new ArrayBlockingQueue<>(1);

/**
 * Semaphore for ensuring only one message is showing at a time.
 */
private static final Semaphore MESSAGE_SHOWING = new Semaphore(1);

/**
 * Semaphore for ensuring only scroll window is showing at a time.
 *
 */
private static final Semaphore SCROLL_WINDOW_IN_USE = new Semaphore(1);

/**
 * A JFXPanel to ensure that JavaFX is running. Initialized once if needed.
 */
private static JFXPanel jFXPanel = null;

/**
 * Offset for messages relative to bottom of owner.
 */
private static double margin = DEFAULT_MARGIN;

/**
 * Default location for the display of the message.
 */
private static Stage owner;

/**
 * x-position on the screen: not used until changed.
 */
private static double x = Double.NaN;

/**
 * y-position on the screen: not used until changed.
 */
private static double y = Double.NaN;

/**
 * Constructor: private to prevent external instantiation
 */
private FXMessage() {}

/**
 * Display a FileChooser dialog, which enables the selection of one file for saving.
 * If no file is chosen, null is returned, otherwise a File for the chosen file. The
 * dialog display waits till all other FXMessage dialogs have been dismissed.
 *
 * @param initialFolder	the folder at which to open the FileChooser, or null
 * @param title			title for the FileChooser
 * @param filters		array of ExtensionFilters, or empty list or null
 * @return				the selected file, or null
 */
public static File chooseFileSave(File initialFolder, String title,
		ExtensionFilter... filters)
{
	List<ExtensionFilter> list = null;
	if( filters != null && filters.length > 0 ) list = Arrays.asList(filters);
	return chooseFileSave(initialFolder, title, list);
}

/**
 * Display a FileChooser dialog, which enables the selection of one file for saving.
 * If no file is chosen, null is returned, otherwise a File for the chosen file. The
 * dialog display waits till all other FXMessage dialogs have been dismissed.
 *
 * @param initialFolder	the folder at which to open the FileChooser, or null
 * @param title			title for the FileChooser
 * @param filters		list of ExtensionFilters, or empty list or null
 * @return				the selected file, or null
 */
public static File chooseFileSave(File initialFolder, String title,
		List<ExtensionFilter> filters)
{
	ready(MESSAGE_SHOWING, null);		// with null, ready always returns true

	/* Blocking queue for Optional<List<File>> types. */
	final ArrayBlockingQueue<Optional<File>> FILE = new ArrayBlockingQueue<>(1);

	/* Instantiate a FileChooser, set the initial folder, the filters, and the
	   title. */
	FileChooser fileChooser = new FileChooser();
	fileChooser.setInitialDirectory(initialFolder);
	if( filters != null && !filters.isEmpty() )
		fileChooser.getExtensionFilters().addAll(filters);
	if( title != null ) fileChooser.setTitle(title);

	/* Show the FileChooser, and post the result to the queue. */
	runRunnable(() ->
	{
		File file = fileChooser.showSaveDialog(owner);
		FILE.add(Optional.ofNullable(file));
	});
	/* Wait for the optional result. */
	Optional<File> result = wait(FILE);
	/* When user dismisses the dialog, message showing is over. */
	readyAgain(MESSAGE_SHOWING);
	return result.isPresent() ? result.get() : null;
}

/**
 * Display a FileChooser dialog, which enables the selection of one or more files.
 * If no file is chosen, null is returned, otherwise a List of chosen files. The
 * dialog display waits till all other FXMessage dialogs have been dismissed.
 *
 * @param initialFolder	the folder at which to open the FileChooser, or null
 * @param title			title for the FileChooser
 * @param filters		array of ExtensionFilters, or empty array or null
 * @return				list of selected files, or null
 */
public static List<File> chooseFilesOpen(File initialFolder, String title,
		ExtensionFilter... filters)
{
	List<ExtensionFilter> list = null;
	if( filters != null && filters.length > 0 ) list = Arrays.asList(filters);
	return chooseFilesOpen(initialFolder, title, list);
}

/**
 * Display a FileChooser dialog, which enables the selection of one or more files.
 * If no file is chosen, null is returned, otherwise a List of chosen files. The
 * dialog display waits till all other FXMessage dialog windows have been dismissed.
 *
 * @param initialFolder	the folder at which to open the FileChooser, or null
 * @param title			title for the FileChooser
 * @param filters		list of ExtensionFilters, or empty list or null
 * @return				list of selected files, or null
 */
public static List<File> chooseFilesOpen(File initialFolder, String title,
		List<ExtensionFilter> filters)
{
	ready(MESSAGE_SHOWING, null);		// with null, ready always returns true

	/* Blocking queue for Optional<List<File>> types. */
	final ArrayBlockingQueue<Optional<List<File>>> FILES = new ArrayBlockingQueue<>(
			1);

	/* Instantiate a FileChooser, set the initial folder, the filters, and the
	   title. */
	FileChooser fileChooser = new FileChooser();
	fileChooser.setInitialDirectory(initialFolder);
	if( filters != null && !filters.isEmpty() )
		fileChooser.getExtensionFilters().addAll(filters);
	if( title != null )
		fileChooser.setTitle(title);

	/* Show the FileChooser, and post the result to the queue. */
	runRunnable(() ->
	{
		List<File> files = fileChooser.showOpenMultipleDialog(owner);
		FILES.add(Optional.ofNullable(files));
	});
	/* Wait for the optional result. */
	Optional<List<File>> result = wait(FILES);
	/* When user dismisses the dialog, message showing is over. */
	readyAgain(MESSAGE_SHOWING);
	return result.isPresent() ? result.get() : null;
}

/**
 * Display a DirectoryChooser dialog, which enables the selection of one folder. If
 * no folder is chosen, null is returned, otherwise the chosen folder. The dialog
 * display waits till all other FXMessage dialog windows have been dismissed.
 *
 * @param initialFolder	the folder at which to open the FileChooser, or null
 * @param title			title for the FileChooser
 * @return				list of selected files, or null
 */
public static File chooseFolder(File initialFolder, String title)
{
	ready(MESSAGE_SHOWING, null);		// with null, ready always returns true

	/* Blocking queue for Optional<List<File>> types. */
	final ArrayBlockingQueue<Optional<File>> FOLDER = new ArrayBlockingQueue<>(1);

	/* Instantiate a DirectoryChooser, set the initial folder, and the  title. */
	DirectoryChooser directoryChooser = new DirectoryChooser();
	directoryChooser.setInitialDirectory(initialFolder);
	if( title != null ) directoryChooser.setTitle(title);
	/* Show the DirectoryChooser, and post the result to the queue. */
	runRunnable(() ->
	{
		File folder = directoryChooser.showDialog(owner);
		FOLDER.add(Optional.ofNullable(folder));
	});
	/* Wait for the optional result. */
	Optional<File> result = wait(FOLDER);
	/* When user dismisses the dialog, message showing is over. */
	readyAgain(MESSAGE_SHOWING);
	return result.isPresent() ? result.get() : null;
}

/**
 * Called when an "OK/CANCEL" response is required from the user. If the dialog
 * cannot be displayed (perhaps due to another dialog already being shown), and wait
 * is false, the result returned will be the value false.
 *
 * @param title			title for the input dialog
 * @param message		to the user as to what input is required
 * @param wait			if the wait parameter is null, or of zero length, or has the
 *						(zeroth) value true, wait until any previous message is
 *						finished then display the message, otherwise if no other
 *						message is being displayed, display this message, otherwise
 *						no message is shown and	false is returned (see return)
 * @return				if message is displayed and confirmed, true, otherwise false
 */
public static boolean confirmation(final String title, final String message,
		boolean... wait)
{
	if( !ready(MESSAGE_SHOWING, wait) ) return false;
									// ready is false if other Message displayed

	/* Show the Alert, and post the result to the BUTTON queue. */
	runRunnable(() ->
	{
		/* Create and prepare the Alert. */
		Alert alert = new Alert(CONFIRMATION, message,
				ButtonType.OK, ButtonType.CANCEL);
		prepare(alert, title);
		BUTTON.add(alert.showAndWait());
	});
	Optional<ButtonType> optional = wait(BUTTON);
	/* When user dismisses the dialog, message showing is over. */
	readyAgain(MESSAGE_SHOWING);
	return optional.isPresent() ? optional.get() == ButtonType.OK : false;
}

/**
 * Called when a "OK/NO/CANCEL" response is required from the user. If the dialog
 * cannot be displayed (perhaps due to another dialog already being shown), and wait
 * is false, the result returned will be the value 0 (canceled).
 *
 * @param title			title for the input dialog
 * @param message		to the user as to what input is required
 * @param wait			if the wait parameter is null, or of zero length, or has the
 *						(zeroth) value true, wait until any previous message is
 *						finished then display the message, otherwise if no other
 *						message is being displayed, display this message, otherwise
 *						no message is shown and	CANCELED is returned (see return)
 * @return				if message is displayed and confirmed, return OK, if message
 *						is displayed not confirmed, return NO, otherwise displayed
 *						or not, CANCELED.
 */
public static int decision(final String title, final String message,
		boolean... wait)
{
	if( message == null )
		throw new IllegalArgumentException("Null message not valid here");

	if( !ready(MESSAGE_SHOWING, wait) ) return CANCELED;
									// ready is false if other Message displayed

	/* Show the Alert, and post the result to the BUTTON queue. */
	runRunnable(() ->
	{
		/* Create and prepare the Alert. */
		Alert alert = new Alert(CONFIRMATION, message,
				ButtonType.OK, ButtonType.NO, ButtonType.CANCEL);
		prepare(alert, title);
		BUTTON.add(alert.showAndWait());
	});
	Optional<ButtonType> optional = wait(BUTTON);
	/* When user dismisses the dialog, message showing is over. */
	readyAgain(MESSAGE_SHOWING);
	return optional.isPresent() ?
			(optional.get() == ButtonType.CANCEL ? CANCELED :
			optional.get() == ButtonType.OK ? OK : NO) : NO;
}

/**
 * Called when the view should display an message of a particular type. The message
 * will be displayed until the user acknowledges the message (by clicking the OK
 * button). If the dialog cannot be displayed (perhaps due to another dialog already
 * being shown), and wait is false, the result returned will be the value false.
 *
 * @param alertType		ERROR, INFORMATION, NONE, WARNING
 * @param title			title to appear with the message
 * @param message		the message to display
 * @param wait			if the wait parameter is null, or of zero length, or has the
 *						zeroth) value true, wait until any previous message is
 *						finished then display the message, otherwise if no other
 *						message is being displayed, display this message, otherwise
 *						no message is shown
 */
public static void display(AlertType alertType, String title, String message,
		boolean... wait)
{
	if( !ready(MESSAGE_SHOWING, wait) ) return;
	// ready is false if other Message displayed

	/* Show the Alert, and post a dummy result to the BUTTON queue. */
	runRunnable(() ->
	{
		/* Create the Alert, and prepare it for showing. */
		Alert alert;
		if( alertType == NONE )
			alert = new Alert(alertType, message, new ButtonType("WAIT)"));
		else
			alert = new Alert(alertType, message);
		prepare(alert, title);
		alert.showAndWait();
		BUTTON.add(Optional.empty());
	});
	/* Causes a wait until a result is posted. */
	wait(BUTTON);
	/* When user dismisses the dialog, message showing is over. */
	readyAgain(MESSAGE_SHOWING);
}

/**
 * Convenience method for when the view should display an error message. The message
 * will be displayed until the user acknowledges the message (by clicking the OK
 * button). If the dialog cannot be displayed (perhaps due to another dialog already
 * being shown), and wait is false, the result returned will be the value false.
 *
 * @param title			title to appear with the message
 * @param message		the message to display
 * @param wait			if the wait parameter is null, or of zero length, or has the
 *						(zeroth) value true, wait until any previous message is
 *						finished then display the message, otherwise if no other
 *						message is being displayed, display this message, otherwise
 *						no message is shown
 */
public static void error(final String title, final String message,
		boolean... wait)
{
	display(ERROR, title, message, wait);
}

/**
 * Sets the offset from the bottom of the owner to show messages. If set to zero,
 * the displayed message will be shown centered on the owner (if there is one).
 *
 * @param aMargin		a margin value to set
 */
public static void setMargin(double aMargin)
{
	margin = aMargin;
}

/**
 * Defines the place in which the messages will be shown. The given Stage will be
 * made the "owner," causing the dialog to be centered over that Stage if the margin
 * is set to zero. Otherwise, the alert will be offset from the left side of the
 * owner, and upward from the bottom of the Stage, by the margin amount. If the user
 * moves the alert, subsequent alerts will be in the same position.
 *
 * @param stage	the window for the application
 */
public static void setStage(Stage stage)
{
	owner = stage;
}

/**
 * Called when some input {@code String} is required from the user. The message will
 * be displayed until the user acknowledges the message (by clicking the OK or the
 * CANCEL button). At that time, the entered {@code String} will be returned (or
 * null if no {@code String} was entered or the dialog was canceled).
 *
 * @param title			title for the input dialog
 * @param message		to the user as to what input is required
 * @return				the input created by the user, or null if dialog is canceled
 */
public static String input(final String title, final String message)
{
	ready(MESSAGE_SHOWING, null);		// with null, ready always returns true

	/* Show the TextInputDialog, and post the result to the ENTERED queue. */
	runRunnable(() ->
	{
		/* Create the TextInputDialog, and prepare it for showing. */
		TextInputDialog input = new TextInputDialog();
		if( message != null )
			input.setHeaderText(message);
		prepare(input, title);
		ENTERED.add(input.showAndWait());
	});
	Optional<String> optional = wait(ENTERED);
	/* When user dismisses the dialog, message showing is over. */
	readyAgain(MESSAGE_SHOWING);
	return optional.isPresent() ? optional.get() : null;
}

/**
 * Optimization: allow for the short-circuit of some work if there is a message
 * showing that has not yet been dismissed by the user.
 *
 * @return				true if there is a message showing
 */
public static boolean isMessageShowing()
{
	return MESSAGE_SHOWING.availablePermits() == 0;
}

/**
 * Called when a selection is needed from a collection of options presented to the
 * user. The user will be shown a dropdown with the available options. If the dialog
 * cannot be displayed (perhaps due to another dialog already being shown), and wait
 * is false, the result returned will be the value null (see the param wait below).
 * <p>
 * The selected item from the dropdown will be returned when the user presses
 * ACCEPT. If the user presses NONE (or dismisses the dialog), a null will be
 * returned, meaning the user did not make a selection. </p>
 * <p>
 * If the wait parameter is an explicit null value (meaning wait), an additional
 * button FINISH is added to the buttons. Selecting FINISH will end the application
 * <em>via</em> a call to {@code System.exit(0)}.
 *
 * @param <T>			the type of items from which to choose
 * @param title			title for the input dialog
 * @param options		collection of the options from which to choose
 * @param wait			if the wait parameter is null, or of zero length, or has the
 *						(zeroth) value true, wait until any previous message is
 *						finished then display the message, otherwise if no other
 *						message is being displayed, display this message, otherwise
 *						no message is shown and null is returned (see return)
 * @return				an instance of type T, or null if no choice is made
 */
public static <T> T options(final String title, final Collection<T> options,
		boolean... wait)
{
	if( options == null || options.isEmpty() )
		throw new IllegalArgumentException("Null options not valid here");

	if( !ready(MESSAGE_SHOWING, wait) ) return null;
									// ready is false if other Message displayed

	/* Blocking queue for Optional<T> types. */
	final ArrayBlockingQueue<Optional<T>> RESULT = new ArrayBlockingQueue<>(1);

	/* Set the default on the selection to be the first item in the options
	   collection. */
	Iterator<T> iterator = options.iterator();
	T defaultChoice = iterator.next();

	/* Establish a GridPane to hold the dropdown for the options, and for the
	   buttons that will be shown. */
	GridPane grid = new GridPane();
	grid.setHgap(10);
	grid.setMaxWidth(Double.MAX_VALUE);
	grid.setAlignment(Pos.CENTER_LEFT);

	/* Show the dialog, and post the result. */
	runRunnable(() ->
	{
		/* Create a dialog which will be shown, and get the dialog pane
				   from that  dialog. */
		Dialog<Optional<T>> choice = new Dialog<>();
		DialogPane pane = choice.getDialogPane();

		/* Two buttons are needed, one to accept the selection, and one to
				   reject any selection. The CANCEL_CLOSE is the default if the
				   dialog is dismissed. */
		ButtonType accept =
				new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
		ButtonType none =
				new ButtonType("None", ButtonBar.ButtonData.CANCEL_CLOSE);
		pane.getButtonTypes().addAll(accept, none);
		/* If the wait parameter has the explicit value of null, then also
				   add a FINISH button, that will end the application. */
		if( wait == null ) pane.getButtonTypes().add(FINISH);
		/* A ComboBox to show the choices of the options. The default is
				   selected on initial showing. Put the ComboBox in the grid, and
				   the grid into the dialog pane. */
		ComboBox<T> comboBox = new ComboBox<>();
		comboBox.getItems().addAll(options);
		comboBox.getSelectionModel().select(defaultChoice);
		grid.add(comboBox, 1, 0);
		pane.setContent(grid);

		/* Do all the preparation. */
		prepare(choice, title);
		/* Set up the result converter for the dialog. */
		choice.setResultConverter((dialogButton) ->
		{
			/* The converter returns the Optional of the selected
							   item if ACCEPT, otherwise an empty Optional. */
			ButtonData data = dialogButton == null ? null :
					dialogButton.getButtonData();
			/* If FINISH was selected, exit the application. */
			if( data == ButtonData.FINISH ) exit(0);
			/* The converter returns an Optional<T> value. */
			return (data == ButtonData.OK_DONE ?
					Optional.of(
							comboBox.getSelectionModel().getSelectedItem()) :
					Optional.empty());
		});
		choice.showAndWait();
		/* The {@code getResult} call invokes the result converter, which
						   returns an Optional<T> object. */
		RESULT.add(choice.getResult());
	});
	Optional<T> result = wait(RESULT);
	/* When user dismisses the dialog, message showing is over. */
	readyAgain(MESSAGE_SHOWING);
	/* Return either the selection, or null if NONE was chosen (or the dialog
	   dismissed). */
	return result.isPresent() ? result.get() : null;
}

/**
 * Called to create a pop-up window, into which messages may be added that will
 * scroll by in real time. The Consumer object that is returned is used to add the
 * messages. The method of the Consumer is {@code accept}.
 *
 * @param title			title for the window
 * @param dimensions	array holding respectively width and height as needed
 * @return				Consumer to accept lines to be added to the scrollable area
 */
@SuppressWarnings("UnusedAssignment")
public static Consumer<String> scrollDisplay(final String title,
		final double... dimensions)
{
	/* Assume the semaphore is going to be acquired. If it is not acquired, then
	   do not check JavaFX, and return null. */
	if( !ready(SCROLL_WINDOW_IN_USE, null) ) return null;

	/* The text area to be contained within the scroller pane, where the
	   messages are added. */
	final TextArea reportArea = new TextArea();

	/**
	 * The Consumer that should be the result of the invocation.
	 */
	Consumer<String> acceptor = new Consumer<>()
	{
	@Override
	public void accept(String line)
	{
		if( SCROLL_WINDOW_IN_USE.availablePermits() != 0 ||
				this != reportArea.getUserData() )
			throw new IllegalStateException("scrollDisplay not active or incorrect");
		{
			/* Latch to prevent the return from this method until the
			   {@code runLater} task has completed. */
			final CountDownLatch countDownLatch = new CountDownLatch(1);
			Platform.runLater(() ->
			{
				/* If any action here finishes normally, or throws an exception,
				   the finally clause wil be executed to release the latch. */
				try
				{
					reportArea.appendText(line + lineSeparator());
				} finally
				{
					countDownLatch.countDown();
				}
			});
			/* Wait here until the appending of the text has been done by the JavaFX
			   system. */
			try
			{
				countDownLatch.await();
			} catch( InterruptedException ex ) {}
		}
	}
	};

	/* Used to ensure the acceptor being used is for this report area. */
	reportArea.setUserData(acceptor);

	/* Blocking queue for Optional<List<File>> types. */
	final ArrayBlockingQueue<Optional<Boolean>> SHOWING =
			new ArrayBlockingQueue<>(1);

	/* Show the scrollable area in a new window. */
	Runnable setup = () ->
	{
		final Stage stage = new Stage(StageStyle.UTILITY);
		/* The given stage cannot be the owner if it is not showing. */
		if( owner != null && owner.isShowing() ) stage.initOwner(owner);
		if( nonNull(title) ) stage.setTitle(title);

		/* Procedure for closing the Stage. */
		Runnable finishUp = (() ->
		{
			Platform.runLater(() ->
			{
					stage.close();
					reportArea.setUserData(null);
					readyAgain(SCROLL_WINDOW_IN_USE);
					Platform.exit();
			});
		});
		stage.setOnCloseRequest((e) -> {finishUp.run();});
		/* VBox to hold report area and the "Done" button. */
		VBox container = new VBox();
		/* The text area for the messages. Clear it. */
		reportArea.clear();
		reportArea.setWrapText(false);
		/* HBox to hold the button, centered, at the bottom of the scrollable area.*/
		HBox hBox = new HBox();
		Button done = new Button("Done");
		done.setOnAction((e) -> {finishUp.run();});
		hBox.getChildren().add(done);
		hBox.setAlignment(Pos.CENTER);
		/* Add the report area and the button to the VBox. */
		container.getChildren().addAll(reportArea, hBox);
		VBox.setVgrow(reportArea, Priority.ALWAYS);
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(container);
		/* Dimensions are respectively height, and width, if present. */
		if( dimensions != null )
		{
			scrollPane.setPrefWidth(DEFAULT_WIDTH);
			if( dimensions.length > 0 && dimensions[0] != 0.0)
				container.setPrefWidth(dimensions[0]);
			scrollPane.setPrefHeight(DEFAULT_HEIGHT);
			if( dimensions.length > 1 && dimensions[1] != 0.0)
				container.setPrefHeight(dimensions[1]);
		}
		/* The scroller is to fit the content. */
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);

		/* Show this window. */
		Scene scene = new Scene(container);
		stage.setScene(scene);
		stage.isResizable();
		stage.show();
		SHOWING.add(Optional.ofNullable(true));
	};
	runRunnable(setup);
	/* Wait for the optional result. */
	wait(SHOWING);
	setup = null;
	return acceptor;
}

/**
 * Called when some message is to be shown, but only for a given time. The dialog
 * can dismissed by the user, but if this is not done, after the given time, the
 * dialog disappears.
 *
 * @param title			title for the temporary dialog
 * @param message		to the user for temporary showing
 * @param milliseconds	maximum length of time to show the message
 */
@SuppressWarnings({ "CallToThreadYield", "Convert2Lambda" })
public static void temporary(final String title, final String message,
		long milliseconds)
{
	ready(MESSAGE_SHOWING, null);	// with null, {@code ready} always returns true

	/* Show the dialog, and post a non-result. */
	runRunnable(new Runnable()
	{
	@Override
	public void run()
	{
		/* At least one button, with ButtonData&nbsp = CANCEL_CLOSE,
			   should be present for the dialog (Alert)to be closeable. This
			   button may be pressed to close the dialog, but if not, the
			   Timeline will close it after the given period. */
		final Alert alert = new Alert(NONE, message,
				new ButtonType("WAIT", ButtonData.CANCEL_CLOSE));
		prepare(alert, title);
		/* Timeline to cause waiting in temporary. This is
			   executed within the JavaFX framework. */
		Timeline timeline = new Timeline(
				new KeyFrame(Duration.millis(milliseconds),
						(e) -> alert.close()));
		timeline.play();					// start the timeline
		alert.showAndWait();				// show the alert
		timeline.stop();					// stop the timeline
		alert.close();						// close alert if not done
		BUTTON.add(Optional.empty());		// no useful return
	}
	});
	/* Causes a wait until a result is posted. */
	wait(BUTTON);
	/* When user or Timeline dismisses the dialog, message showing is over. */
	readyAgain(MESSAGE_SHOWING);
}

/**
 * The preparatory steps to showing the alert. This arranges for the dialog to be
 * shown on top of other windows, listeners to detect the current location of the
 * dialog (in case the user has moved it), and to effect whether it is shown over
 * the top of an existing Stage, or displaced with respect to that Stage.
 * <p>
 * The two static BlockingQueues are also cleared.
 *
 * @param dialog	the dialog that is to be shown
 * @param title		if not null, title to add to dialog window
 */
@SuppressWarnings("NestedAssignment")
private static void prepare(Dialog<? extends Object> dialog, String title)
{
	if( title != null ) dialog.setTitle(title);
	/* This is to ensure the alert is seen on top of all windows. */
	Stage origin = ( (Stage) dialog.getDialogPane().getScene().getWindow() );
	origin.setAlwaysOnTop(true);
	/* Add listeners in case the window is moved. Remember the new location. */
	dialog.xProperty().addListener(
			(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) -> x = (double) newValue);
	dialog.yProperty().addListener(
			(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) -> y = (double) newValue);
	/* If an explicit location has not been made, then locate wrt the owner. */
	if( Double.isNaN(x) || Double.isNaN(y) )
	{
		/* The given stage cannot be the owner if it is not showing. */
		if( owner != null && owner.isShowing() )
			dialog.initOwner((origin = owner));
		/* If margin is zero, allow the default placing of the alert window,
		   otherwise (here) position using the given margin. */
		if( margin != 0 )
		{
			dialog.setX(origin.getX() + margin);
			dialog.setY(origin.getY() + origin.getHeight() - margin);
		}
	} else
	{
		/* Use the location of the move. */
		dialog.setX(x);
		dialog.setY(y);
	}
	/* Set up the queues for new results. */
	ENTERED.clear();
	BUTTON.clear();
}

/**
 * If the wait parameter is null, or of zero length, or has the (zeroth) value true,
 * wait until the MESSAGE_SHOWING semaphore can be acquired, otherwise try to
 * acquire it immediately. If and when successfully acquired, JavaFX is ensured to
 * be running, and true is returned. Otherwise, return false, and no message should
 * be shown.
 *
 * @param showing		the Semaphore to be claimed
 * @param wait			the parameter from the invocation
 * @return				true if the semaphore has been acquired, false otherwise
 */
private static boolean ready(Semaphore showing, boolean[] wait)
{
	/* Assume the semaphore is going to be acquired. If it is not acquired, then
	   do not check JavaFX, and return false. */
	boolean result = true;
	if( wait == null || wait.length == 0 || wait[0] )
		showing.acquireUninterruptibly();
	else result = showing.tryAcquire();

	/* The semaphore has been obtained, so ensure JavaFX is running. */
	if( result && jFXPanel == null ) jFXPanel = new JFXPanel();
	return result;
}

/**
 * The message showing has ended, and the class is ready again to shown another
 * message.
 *
 * @param showing		Semaphore to be released
 */
private static void readyAgain(Semaphore showing)
{
	showing.release();
}

/**
 * Given an runnable, run it on the Application thread.
 *
 * @param runnable		the runnable
 */
private static void runRunnable(Runnable runnable)
{
	/* If called on the Application thread, just run the runnable, otherwise ... */
	if( Platform.isFxApplicationThread() )
		runnable.run();
	else
	{
		/* ... run the runnable by scheduling it on the Application thread. */
		Platform.setImplicitExit(false);
		Platform.runLater(runnable);
	}
}

/**
 * By using take, wait for an Optional&lt;T&gt; to be posted to a blocking array.
 *
 * @param <T>			type of the Optional being awaited in the queue
 * @param array			the blocking queue to which the optional will be posted
 * @return				the optional that was posted
 */
private static <T> Optional<T> wait(ArrayBlockingQueue<Optional<T>> array)
{
	Optional<T> result;
	try
	{
		/* Take waits until there is an entry in the queue. */
		result = array.take();
	} catch( InterruptedException ex )
	{
		result = Optional.empty();
	}
	return result;
}
}
