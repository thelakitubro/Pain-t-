package paint;

import java.awt.Desktop;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class Paint extends Application {

    public static double imgMinX, imgMinY;
    public static int imageWidth, imageHeight;
    public static Image image;
    public static TextArea textArea = new TextArea();
    //Set up for undo and redo
    public static UndoRedoStack uRStack = new UndoRedoStack();

    @Override
    public void start(Stage primaryStage) {

        FileChooser fileChooser = new FileChooser();

        //Open button
        final Button open = new Button("Open Image");
        open.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                setExtFilters(fileChooser);
                List<File> files = (List<File>) fileChooser.showOpenMultipleDialog(primaryStage);
                if (files != null) {
                    separateFiles(files);
                }
            }
        });

        //Polite close button
        final Button exit = new Button("Exit Program");
        exit.setOnAction(e -> Platform.exit());

        //Format the opening window
        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(25, 25, 25, 25));
        root.add(open, 0, 1);
        root.add(exit, 1, 1);

        Scene scene = new Scene(root, 400, 150);

        primaryStage.setTitle("Open and Exit");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void setExtFilters(FileChooser chooser) {
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Images", "*.*")
        );
    }

    private void setCanvas(Canvas canvas, Image img) {
        GraphicsContext graphCont = canvas.getGraphicsContext2D();
        graphCont.drawImage(img, 0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    /**
     * Separate Files.
     * Separates a list of files in order to process them individually.
     * Feeds each file into openNewImageWindow.
     * 
     * @param files List of files
     */
    private void separateFiles(List<File> files) {
        for (int n = 0; n < files.size(); n++) {
            openNewImageWindow(files.get(n));
        }
    }

    private void openNewImageWindow(File file) {
        Stage secondStage = new Stage();
        image = new Image(file.toURI().toString());
        imageWidth = (int) image.getWidth();
        imageHeight = (int) image.getHeight();
        imgMinX = 0;
        imgMinY = 0;
        
        //Set up drawing buttons
        DrawingButtons drawBtns = new DrawingButtons();
        ImageEditButtons imgEditBtns = new ImageEditButtons();
        
        Canvas canvas = new Canvas();
        GraphicsContext graphCont = canvas.getGraphicsContext2D();

        String imagePath = file.getPath();

        //Set scene to be same size as image
        Scene scene = new Scene(new VBox(), imageWidth, imageHeight);

        //Set canvas properties
        canvas.setWidth(imageWidth);
        canvas.setHeight(imageHeight);
        setCanvas(canvas, image);
        graphCont.setLineWidth(2.0);

        //Create scroll bars
        ScrollPane scroll = new ScrollPane();
        scroll.setContent(canvas);
        scroll.fitToWidthProperty().set(true);
        scroll.fitToHeightProperty().set(true);
        scroll.setHbarPolicy(ScrollBarPolicy.ALWAYS);
        scroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        
        textArea.setText("Insert text here.");

        final VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(0, 10, 0, 10));
        vbox.getChildren().addAll(canvas, scroll);

        Logger logger = Logger.getLogger("MyLog");
        FileHandler fHandler;
        try {
            fHandler = new FileHandler("C:/Users/jlane7/Documents/NetBeansProjects/Paint/MyLogFile.log");
            logger.addHandler(fHandler);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance();
        Date calendarDate = calendar.getTime();

        //Set up timer tasks
        Timer autoSaveTimer = new Timer();
        TimerTask autoSaveTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    File file = new File(imagePath);
                    try {
                        WritableImage writableImage = new WritableImage(imageWidth, imageHeight);
                        canvas.snapshot(null, writableImage);
                        RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                        ImageIO.write(renderedImage, "png", file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("AutoSave Complete.");
                });
            }
        };
        autoSaveTimer.scheduleAtFixedRate(autoSaveTask, calendarDate, 60000);

        Timer logTimer = new Timer();
        TimerTask logTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    logger.info(imagePath);
                    logger.info("Currently using " + drawBtns.currentBtn + " and " + imgEditBtns.currentBtn);  
                    System.out.println("Log Complete.");
                });
            }
        };
        logTimer.scheduleAtFixedRate(logTask, calendarDate, 60000);

        //Create menu bar
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("_File");
        Menu menuEdit = new Menu("_Edit");
        Menu menuOptions = new Menu("_Options");

        //Save
        MenuItem menuItem_Save = new MenuItem("Save");
        menuItem_Save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File file = new File(imagePath);
                try {
                    WritableImage writableImage = new WritableImage(imageWidth, imageHeight);
                    canvas.snapshot(null, writableImage);
                    RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    ImageIO.write(renderedImage, "png", file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        //Save As
        MenuItem menuItem_Save_As = new MenuItem("Save As");
        menuItem_Save_As.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save As");

                FileChooser.ExtensionFilter pngFilter
                        = new FileChooser.ExtensionFilter("PNG File (*.png)", "*.png");
                FileChooser.ExtensionFilter jpgFilter
                        = new FileChooser.ExtensionFilter("JPG File (*.jpg)", "*.jpg");
                FileChooser.ExtensionFilter gifFilter
                        = new FileChooser.ExtensionFilter("GIF File (*.gif)", "*.gif");
                FileChooser.ExtensionFilter tiffFilter
                        = new FileChooser.ExtensionFilter("TIFF File (*.tiff)", "*.tiff");
                FileChooser.ExtensionFilter rawFilter
                        = new FileChooser.ExtensionFilter("RAW File (*.raw)", "*.raw");
                fileChooser.getExtensionFilters().addAll(pngFilter, jpgFilter, gifFilter, tiffFilter, rawFilter);

                File file = fileChooser.showSaveDialog(secondStage);
                if (file != null) {
                    try {
                        WritableImage writableImage = new WritableImage(imageWidth, imageHeight);
                        canvas.snapshot(null, writableImage);
                        RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                        ImageIO.write(renderedImage, "png", file);
                    } catch (IOException ex) {
                        Logger.getLogger(
                                Paint.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        //Zoom in and out
        MenuItem menuItem_Zoom = new MenuItem("Zoom");
        menuItem_Zoom.setOnAction(e -> {
            TextInputDialog zoom = new TextInputDialog("Zoom In/Out");
            zoom.setHeaderText("Please enter how much you want to zoom in or out:");
            zoom.showAndWait();
            String input = zoom.getEditor().getText();
            int zoomFactor = Integer.parseInt(input);
            canvas.setScaleX(zoomFactor);
            canvas.setScaleY(zoomFactor);
        });

        //Release notes for help menu
        Hyperlink link = new Hyperlink();
        File linked = new File("C:\\Users\\jlane7\\Documents\\NetBeansProjects\\Paint");
        link.setText("Click here for the release notes.");
        link.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(linked);
                } catch (IOException ex) {
                    Logger.getLogger(Paint.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        //Help menu
        MenuItem menuItem_Help = new MenuItem("Help");
        menuItem_Help.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                VBox dialogVbox = new VBox();
                Text text = new Text("Welcome to Paint!\n\n"
                        + "This program allows you to draw on selected images.\n"
                        + "You can either save the image to edit it directly,\n"
                        + "or save it as a new file to create a copy.\n"
                        + "You can also zoom in and out by entering a number\n"
                        + "into the zoom menu (1 is the most zoomed out).\n\n"
                        + "The pencil tool allows you to draw on the image.\n"
                        + "The slider tool adjusts the width of the pencil.\n"
                        + "The color select tool changes the color of the pencil.\n"
                        + "The color grabber allows you to grab a color from the image.\n"
                        + "Adding text allows you to draw the typed message onto the image.\n"
                        + "The move image button allows you to shift the image with your mouse.\n\n"
                        + "This link will take you to the \n");
                dialogVbox.getChildren().addAll(text, link);
                Scene dialogScene = new Scene(dialogVbox);
                dialog.setScene(dialogScene);
                dialog.show();
            }
        });
        menuFile.getItems().addAll(menuItem_Save, menuItem_Save_As, menuItem_Zoom, menuItem_Help);

        //Undo
        MenuItem menuItem_Undo = new MenuItem("Undo");
        menuItem_Undo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                uRStack.undo(canvas, graphCont);
            }
        });

        //Redo
        MenuItem menuItem_Redo = new MenuItem("Redo");
        menuItem_Redo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                uRStack.redo(canvas, graphCont);
            }
        });
        menuEdit.getItems().addAll(menuItem_Undo, menuItem_Redo);

        //Turn autosave timer off
        MenuItem menuItem_AutoSave_Timer = new MenuItem("Turn Off AutoSave Timer");
        menuItem_AutoSave_Timer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                autoSaveTimer.cancel();
                System.out.println("Autosave is nown off.");
            }
        });
        menuOptions.getItems().addAll(menuItem_AutoSave_Timer);

        menuBar.getMenus().addAll(menuFile, menuEdit, menuOptions);

        //Keyboard shortcuts
        menuItem_Save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        menuItem_Save_As.setAccelerator(new KeyCodeCombination(KeyCode.F12));
        menuItem_Zoom.setAccelerator(new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN));
        menuItem_Help.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
        menuItem_Undo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        menuItem_Redo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));

        //Warning for exit
        secondStage.setOnCloseRequest(event -> {
            Alert exitWarn = new Alert(AlertType.CONFIRMATION, "Are you sure you want to exit?", ButtonType.YES, ButtonType.NO);
            exitWarn.setTitle("Confirm Exit");
            exitWarn.setHeaderText("Any unsaved work will be lost.");
            ButtonType result = exitWarn.showAndWait().orElse(ButtonType.NO);
            if (ButtonType.NO.equals(result)) {
                event.consume();
            }
            autoSaveTimer.cancel();
            logTimer.cancel();
        });


        //Drawing a line
        graphCont.strokeLine(100, 50, 300, 50);

        //Draw multiple shapes
        //Square
        graphCont.strokeRect(10, 70, 30, 30);
        graphCont.fillRect(60, 70, 30, 30);

        //Rectangle
        graphCont.strokeRect(10, 120, 50, 30);
        graphCont.fillRect(80, 120, 50, 30);

        //Ellipse
        graphCont.strokeOval(10, 170, 50, 30);
        graphCont.fillOval(80, 170, 50, 30);

        //Circle
        graphCont.strokeOval(10, 220, 30, 30);
        graphCont.fillOval(60, 220, 30, 30);

        
        drawBtns.setUpButtons(canvas, graphCont);
        imgEditBtns.setUpButtons(canvas, graphCont);
        
        ToolBar toolbar = new ToolBar();
        toolbar.getItems().addAll(drawBtns.mouse, drawBtns.pencil, drawBtns.eraser, drawBtns.lineWidth, drawBtns.colorPicker, drawBtns.getColor, 
                drawBtns.btnRectangle, drawBtns.btnCircle, drawBtns.btnTriangle, drawBtns.addText, imgEditBtns.moveImage, imgEditBtns.rotateImage);

        //Set tooltips for buttons
        drawBtns.setToolTips();
        imgEditBtns.setToolTips();

        ((VBox) scene.getRoot()).getChildren().addAll(menuBar, toolbar, vbox, textArea);

        secondStage.setTitle(file.getName());
        secondStage.setScene(scene);
        secondStage.setWidth(imageWidth + toolbar.getWidth() + menuBar.getWidth());
        secondStage.setHeight(imageHeight + toolbar.getHeight() + menuBar.getHeight());
        secondStage.show();
    }
}
