package com.example;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ListCell;

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
                if (empty || msg == null) {
                    setText(null);
                } else {
                    setText("[" + timeFormatter.format(Instant.ofEpochMilli(msg.time())) + "] "
                            + msg.message());
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
        model.sendMessage();
        messageInput.clear();
    }
}
