package com.example;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    @FXML
    private Button sendButton;

    @FXML
    private Label messageLabel;

    @FXML
    private ListView<NtfyMessageDto> messageView;

    @FXML
    private TextArea messageInput;

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    private String formatTimestamp(long epochTime) {
        // Testa om det är millisekunder (större än 10^10)
        Instant instant = epochTime > 10_000_000_000L
                ? Instant.ofEpochMilli(epochTime)
                : Instant.ofEpochSecond(epochTime);

        return timeFormatter.format(instant);
    }

    @FXML
    private void initialize() {

        messageLabel.setText(model.getGreeting());

        messageView.setItems(model.getMessages());

        messageInput.textProperty().bindBidirectional(model.messageToSendProperty());

        sendButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> {
                    String text = messageInput.getText();
                    return text == null || text.trim().isEmpty();
                },
                messageInput.textProperty()
        ));


        // Formatering av message

        messageView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null || msg.message() == null || msg.message().isBlank()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Chat bubble
                    Label bubble = new Label(msg.message());
                    bubble.setWrapText(true);
                    bubble.setMaxWidth(300);
                    bubble.getStyleClass().add("chat-bubble");

                    // Timestamp (optional - ta bort om du inte vill ha)
                    Label timestamp = new Label(formatTimestamp(msg.time()));
                    timestamp.getStyleClass().add("message-timestamp");

                    VBox messageBox = new VBox(2, bubble, timestamp);
                    HBox container = new HBox(messageBox);
                    container.setPadding(new Insets(4));

                    // Olika styling beroende på avsändare
                    if ("myusername".equals(msg.id())) {
                        bubble.getStyleClass().add("chat-bubble-sent");
                        messageBox.setAlignment(Pos.CENTER_RIGHT);
                        container.setAlignment(Pos.CENTER_RIGHT);
                    } else {
                        bubble.getStyleClass().add("chat-bubble-received");
                        messageBox.setAlignment(Pos.CENTER_LEFT);
                        container.setAlignment(Pos.CENTER_LEFT);
                    }

                    setText(null);
                    setGraphic(container);
                }
            }
        });

        // Scrolla ner till senaste meddelandet
        model.getMessages().addListener((javafx.collections.ListChangeListener<NtfyMessageDto>) change -> {
            Platform.runLater(() -> {
                if (!messageView.getItems().isEmpty()) {
                    messageView.scrollTo(messageView.getItems().size() - 1);
                }
            });
        });
    }

    @FXML
    private void sendMessage(ActionEvent actionEvent) {
        model.sendMessageAsync(success -> {
            if (success) {
                Platform.runLater(() -> messageInput.clear());
            } else {
                // Här kan du visa ett felmeddelande i UI
                System.err.println("Failed to send message");
            }
        });
    }

}
