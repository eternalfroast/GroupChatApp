
/**
 * @author:Saurav Pradhan
 * 
 * It is a JAVA GUI program that would facilitate text chatting/exchanging between two or multiple computers over the network/internet,
 * using the concept of JAVA socket programming. If there is no network environment, user can run on a single machine by 
 * instantiating this program multiple times.
 * 
 * 
 */



//importing the necessary library
import javafx.scene.paint.Color;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.control.Tooltip;

public class Client extends Application implements Runnable {
    // Text field for chat

    private TextField textInputMessage = new TextField("Please enter your message and press enter");

    // Text field for name
    private TextField textNameOfClient = new TextField("Please enter your name");
    private ArrayList<String> infoStore = new ArrayList();
    private ArrayList<String> clientsNameInfo;
    
    // Text area to display contents
    // private TextArea ta = new TextArea();
    private String nameOfClients;
    // Socket
    private Socket socket;
    
    // IO streams
    private DataOutputStream dout;
    private DataInputStream din;
    private String theMessageFromUser;
    private Label status;


    private VBox connectionStatusBox;
    private VBox twoContainer;
    private VBox displayMessagePane = new VBox();
    private Label nameOfClient;

    //global flags for controlling the connection status
    
    private boolean connectionError;
    private boolean token;
    private boolean isHimself;
    private boolean midWayFailure;
   

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {

        //container for the textField and connection status box--------------------------------//
        VBox twoContainer = new VBox(10);
        connectionStatusBox = new VBox(10);
        connectionStatusBox.setPadding(new Insets(10));
        status = new Label();

        status.setTextFill(Color.WHITE);
        connectionStatusBox.setAlignment(Pos.CENTER);

        connectionStatusBox.getChildren().addAll(status);

        //----------------------------end-----------------------------------------------------//
        
        //setting up the width and height of the textfields
        textNameOfClient.setMaxWidth(200);
        textInputMessage.setMinHeight(45);

        VBox headerBox = new VBox(10);

        
        //setting up the padding
        headerBox.setPadding(new Insets(10));
        nameOfClient = new Label();

        //setting the name of client 
        nameOfClient.setText(nameOfClients);
        //setting the color to white
        nameOfClient.setTextFill(Color.WHITE);
        //setting the background to white
        headerBox.setStyle("-fx-background-color: white;");

        //making the textfield background transparent
        textNameOfClient.setStyle("-fx-background-color: transparent;");
        textNameOfClient.setAlignment(Pos.CENTER);

        //when someone enters the name in the textfield and leaves the field, the title is automatically updated
        textNameOfClient.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            primaryStage.setTitle(textNameOfClient.getText());
        });

        //designing the headerBox------------------//
        headerBox.setAlignment(Pos.CENTER);
        headerBox.getChildren().addAll(textNameOfClient);

        //using tooltip to show the information to the user---------------------------------//
        final Tooltip textClientInputInfo = new Tooltip();
        textClientInputInfo.setText(
            "Please enter the message that you want to type then press enter to send the message\n "+
            "The user can put any name like example: Saurav Pradhan or any fancy name they like\n" + 
            "Once user enters the name, it will be automatically updated in the chat bubble and title of the application\n"

            
        );
        textNameOfClient.setTooltip(textClientInputInfo);


        textInputMessage.setMinHeight(45);

        

        final Tooltip textInputInfo = new Tooltip();
        textInputInfo.setText(
            "The message from the users are shown here.\n"+
            "If the message is from the other user it is shown in the left side with orange color bubble\n" + 
            "If the message is from ownself it is shown in the right with blue color bubble\n"
            
        );
        textInputMessage.setTooltip(textInputInfo);

        // final Tooltip textDisplayMessageInfo = new Tooltip();
        // textDisplayMessageInfo.setText(
        //     "This field shows the message from users. It automatically scrolls."+
        //     "The message from other user is shown in the left and indicated by orange bubble"+
        //     "The message from ownself is shown on right and indicated by blue bubble"
        // );
        // scrollPane.setTooltip(textDisplayMessageInfo);
        //using tooltip to show the information to the user---------------------------------//

        // --------------------------------Header
        // Part--------------------------------------------------------------//
        BorderPane mainPane = new BorderPane();

        //creating the scrollable pane
        ScrollPane scrollPane = new ScrollPane(displayMessagePane);

        //seting up the padding of vbox
        displayMessagePane.setPadding(new Insets(15));

        //setting up the color of the text of textfield.
        nameOfClient.setTextFill(Color.WHITE);

        //adding elements to the VBox
        twoContainer.getChildren().addAll(textInputMessage, connectionStatusBox);

        // -----------------automatically scroll down to
        // bottom----------------------------//
        scrollPane.setVvalue(1D);
        displayMessagePane.heightProperty().addListener(observable -> scrollPane.setVvalue(1D));

        // ----------------------------automatically scrolldown to
        // bottom--------------------------//

        //
        displayMessagePane.prefWidthProperty().bind(scrollPane.widthProperty());
        scrollPane.prefWidthProperty().bind(mainPane.widthProperty());
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);

        // ----------------setting up the scrollpane and
        // scrollablility----------------------------------------------//


        

        //adding the elements to the borderpane
        mainPane.setTop(headerBox);
        mainPane.setCenter(scrollPane);
        mainPane.setBottom(twoContainer);
        mainPane.setPadding(new Insets(20));
        // pane.setCenter(new ScrollPane(ta));

        // Create a scene and place it in the stage
        Scene scene = new Scene(mainPane, 450, 500);
        mainPane.getStylesheets().add(this.getClass().getResource("messenger.css").toExternalForm());

        scrollPane.setBackground(Background.EMPTY);
        // primaryStage.setTitle("Messaging App"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        textInputMessage.setOnAction(e -> process()); // Register listener
        connection();

    }


    public void run() {
        try {
            while (true) {
                // Get message
                String text = din.readUTF();


                //splitting the text and getting the name and the message as seperate
                nameOfClients = text.substring(0, text.indexOf(":"));
                theMessageFromUser = text.substring(text.indexOf(":") + 2, text.length());
                clientsNameInfo = new ArrayList();

                //creating an arraylist and adding the name of clients in the arraylist
                clientsNameInfo.add(nameOfClients);

                //determines the position of message box displayed
                //Eg: if it's the user then the message box is displayed at the right
                //But if the user is another person, the message is displayed in the left
                if (nameOfClients.equals(textNameOfClient.getText()) == false) {
                    isHimself = false;
                } else {
                    isHimself = true;
                }

                //runs the javafx uI thread
                Platform.runLater(new ChatPopUP(nameOfClients, theMessageFromUser, isHimself));

                
            }
        } catch (IOException ex) {

            //calls the method if there is an error
            socketError();

            
        }
    }

    //method for creating connection to the client
    private void connection() {

        try {

            // Create a socket to connect to the server
            socket = new Socket("localhost", 8000);
            

            // Create an input stream to receive data from the server
            din = new DataInputStream(socket.getInputStream());

            // Create an output stream to send data to the server
            dout = new DataOutputStream(socket.getOutputStream());

            //connection error is false means that it was sucessfully connected to server and marks the flag
            connectionError = false;

            //this is for also one of the global flag
            token = true;

            //the label is updated as below
            status.setText("Connected to Server");
            //the color of the vbox is changed as following
            connectionStatusBox.setStyle("-fx-background-color: green;");

            // Start a new thread for receiving messages
            new Thread(() -> run()).start();

        } catch (IOException ex) {

            //if the server is off when person is chatting, the following if condition will activate
            if (midWayFailure == true) {
                connectionError = true;
                status.setText("Not connected to Server");
                connectionStatusBox.setStyle("-fx-background-color: red;");
                return;

            } else {
                socketError();
                status.setText("Not connected to Server");
                connectionStatusBox.setStyle("-fx-background-color: red;");
                connectionError = true;
                
            }

        }

    }


    //this determines if there is connection error or not. If there is connection error, then if condition is activated
    //otherwise else condition is activated

    private void process() {

        if (connectionError == true) {
            connection();
            if (token == true) {
                socketMessage();
            }
        } else {
            socketMessage();
        }
    }

    //this method sends the message to the server
    private void socketMessage() {
        try {
            // Get the text from the text field
            String string = textNameOfClient.getText().trim() + ": " + textInputMessage.getText().trim();

            // Send the text to the server
            dout.writeUTF(string);

            // Clear jtf
            textInputMessage.setText("");
        } catch (IOException ex) {

            //if the server is not found this part will activate
            socketError();
            midWayFailure = true;
            connection();
        }
    }

    /**
     * The main method is only needed for the IDE with limited JavaFX support. Not
     * needed for running from the command line.
     */
    public static void main(String[] args) {
        launch(args);
    }

    //class that is responsible for the bubble in the background of the scrollable text
    class ChatPopUP implements Runnable {

        //declaring the variables
        private String userName;
        private boolean clientCheck;
        private String message;

        //constructor of the class
        public ChatPopUP(String userName, String message, boolean clientCheck) {
            this.userName = userName;
            this.clientCheck = clientCheck;
            this.message = message;
        }

        //when this runs, depending on whether its the user or another person
        //the bubble position is controlled.

        //if it's the user, the bubble will appear in left, if it's another person bubble will appear on the right.
        public void run() {
            if (clientCheck == false) {
                sendMessageLeft(userName, message);
            } else {
                sendMessageRight(userName, message);
            }

        }

        //this method will created bubble, sets it color, add text to it and set it's position.
        private void sendMessageLeft(String userName, String message) {
            HBox newContainer = new HBox();
            //creating new textarea
            TextArea txtArea = new TextArea();
            //adding the padding
            txtArea.setPadding(new Insets(10, 0, 0, 0));
            //adding user name first
            txtArea.appendText(userName + ":\n");
            //then adding the message
            txtArea.appendText(message);
            txtArea.setWrapText(true);
            
            //setting up the ID so it can be decorated using CSS
            txtArea.setId("MessageGt");
            newContainer.getChildren().addAll(txtArea);
            newContainer.setAlignment(Pos.BOTTOM_LEFT);
            // txtArea.setEditable(true);
            // txtArea.setPadding(new Insets(10,10,10,10));
            
            //calls the method, to resize the bubble
            resizeTextArea(txtArea);

            displayMessagePane.getChildren().add(newContainer);
            textInputMessage.clear();
        }

        //this method will created bubble, sets it color, add text to it and set it's position.
        private void sendMessageRight(String userName, String message) {
            HBox newContainer = new HBox();

            //creating new textarea
            TextArea txtArea = new TextArea();
            //setting up the padding
            txtArea.setPadding(new Insets(10, 0, 0, 0));

            //adding the username first
            txtArea.appendText(userName + ":\n");
            //adding the message later
            txtArea.appendText(message);
            txtArea.setWrapText(true);
            
            txtArea.setId("Message");
            newContainer.getChildren().addAll(txtArea);
            newContainer.setAlignment(Pos.BOTTOM_RIGHT);
            // txtArea.setEditable(true);
            // txtArea.setPadding(new Insets(10,10,10,10));

            //calls the method, to resize the bubble
            resizeTextArea(txtArea);

            displayMessagePane.getChildren().add(newContainer);
            textInputMessage.clear();
        }

        //method responsible for the resizing the bubble
        private void resizeTextArea(TextArea txtArea) {

            //gets the text from the text area
            String text = txtArea.getText();

            double maxWidth = displayMessagePane.getWidth() - 80;

            HBox hBox = new HBox();
            //getting the message and adding the text
            Text textGetter = new Text(text);
            textGetter.setFont(Font.font(15));
            hBox.getChildren().add(textGetter);
            Scene scene = new Scene(hBox);
            //applying the css
            textGetter.applyCss();

            double width = textGetter.prefWidth(-1) + 20;
            double height = textGetter.prefHeight(-1) + 20;

            //determines the width of the bubble
            if (width > maxWidth) {
                txtArea.setMaxWidth(maxWidth);
                txtArea.setMinWidth(maxWidth);
            } else {
                txtArea.setMaxWidth(width);
                txtArea.setMinWidth(width);

            }

            //determines the height of the bubble
            txtArea.setMinHeight(height);
            txtArea.setMaxHeight(height);
        }

    }

    public Alert socketError() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Socket Error");
        alert.setHeaderText("Connection Error ");
        alert.setContentText("Please ensure that you are connected to the server");

        alert.showAndWait();
        return alert;
    }
}
