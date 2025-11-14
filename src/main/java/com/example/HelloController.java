package com.example;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;


/**
 * Controller layer: mediates between the view (FXML) and the model.
 */

public class HelloController {

    /** The main model for handling messages and topics. */
    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    /** Button to send a message **/
    @FXML
    private Button sendButton;

    /** Label displaying the name of the program **/
    @FXML
    private Label messageLabel;

    /** Label displaying the name of the current topic **/
    @FXML
    private Label topicLabel;

    /** Listview displaying all the chat messages */
    @FXML
    private ListView<NtfyMessageDto> messageView;

    /** Text area where the user types a message to send */
    @FXML
    private TextArea messageInput;

    /** Text field where the user types a new topic */
    @FXML
    private TextField topicInput;

    /** Botton to change topic **/
    @FXML
    private Button changeTopicButton;

//    private final DateTimeFormatter timeFormatter =
//            DateTimeFormatter.ofPattern("HH:mm:ss")
//                    .withZone(ZoneId.systemDefault());


    /**
     * Initializes the controller after the FXML is loaded.
     * Sets up the bindings between the model and UI,
     * configures the message list view,
     * and automatically scrolls down to the bottom of the message list
     */
    @FXML
    private void initialize() {
        messageLabel.setText(model.getGreeting());

        Platform.runLater(() -> messageInput.requestFocus());

        topicLabel.setText("/" + model.getCurrentTopic());
        model.currentTopicProperty().addListener((obs, oldVal, newVal) -> {
            topicLabel.setText("/" + newVal);
        });

        messageView.setItems(model.getMessages());

        messageInput.textProperty().bindBidirectional(model.messageToSendProperty());

        sendButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> {
                    String text = messageInput.getText();
                    return text == null || text.trim().isEmpty();
                },
                messageInput.textProperty()
        ));

        if (changeTopicButton != null) {
            changeTopicButton.disableProperty().bind(Bindings.createBooleanBinding(
                    () -> {
                        String text = topicInput.getText();
                        return text == null || text.trim().isEmpty();
                    },
                    topicInput.textProperty()
            ));
        }


        // Set up cell-factory to show chat bubbles
        messageView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null || msg.message() == null || msg.message().isBlank()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label bubble = new Label(msg.message());
                    bubble.setWrapText(true);
                    bubble.setMaxWidth(250);
                    bubble.setPadding(new Insets(10));
                    bubble.getStyleClass().addAll("chat-bubble", "chat-bubble-received");

                    HBox container = new HBox(bubble);
                    container.setPadding(new Insets(5));
                    container.setAlignment(Pos.CENTER_LEFT); // alla samma sida

                    setText(null);
                    setGraphic(container);
                }
            }
        });




        // Scroll down to last message automatically
        model.getMessages().addListener((javafx.collections.ListChangeListener<NtfyMessageDto>) change -> {
            Platform.runLater(() -> {
                if (!messageView.getItems().isEmpty()) {
                    messageView.scrollTo(messageView.getItems().size() - 1);
                }
            });
        });
    }

    /**
     * Sends the message typed by the user
     * Displays error if message send fails
     */

    @FXML
    private void sendMessage(ActionEvent actionEvent) {
        model.sendMessageAsync(success -> {
            if (success) {
                Platform.runLater(() -> messageInput.clear());
                Platform.runLater(() -> messageInput.requestFocus());
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Send Failed");
                    alert.setHeaderText("Failed to send message");
                    alert.setContentText("Could not send your message. Please try again.");
                    alert.showAndWait();
                });
            }
        });
    }

    /**
     * Changes the chat topic to the one entered by user
     * Clears topic input field
     * @param actionEvent topic name
     */

    @FXML
    private void changeTopic(ActionEvent actionEvent) {
        String newTopic = topicInput.getText();
        if (newTopic != null && !newTopic.isBlank()) {
            model.setCurrentTopic(newTopic);
            topicInput.clear();
        }
    }
}