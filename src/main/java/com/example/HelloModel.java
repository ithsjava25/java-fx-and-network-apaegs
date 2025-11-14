package com.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.function.Consumer;

import static com.example.FxUtils.runOnFx;

/**
 * Model layer for the YadaChat app.
 * Holds current topic, outgoing message text and received messages.
 * Communicates with an {@link NtfyConnection}.
 */
public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();
    private final StringProperty currentTopic = new SimpleStringProperty();

    /**
     * Creates a new model using the given connection.
     *
     * @param connection the Ntfy connection; must not be null
     */
    public HelloModel(NtfyConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("connection cannot be null");
        }
        this.connection = connection;
        this.currentTopic.set(connection.getCurrentTopic());
        receiveMessage();
    }

    /** Returns the observable list of messages. */
    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    /** Returns the current text in the message input. */
    public String getMessageToSend() {
        return messageToSend.get();
    }

    /** Returns the property for the message to send. */
    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    /** Sets the message to send. */
    public void setMessageToSend(String message) {
        messageToSend.set(message);
    }

    /** Returns the current topic. */
    public String getCurrentTopic() {
        return currentTopic.get();
    }

    /** Returns the property for the current topic. */
    public StringProperty currentTopicProperty() {
        return currentTopic;
    }

    /**
     * Changes the active topic and clears old messages.
     *
     * @param topic new topic name
     */
    public void setCurrentTopic(String topic) {
        if (topic != null && !topic.isBlank()) {
            connection.setCurrentTopic(topic);
            this.currentTopic.set(topic);
            messages.clear();
            receiveMessage();
        }
    }

    /** Returns the current user ID. */
    public String getUserId() {
        return connection.getUserId();
    }

    /** Returns the greeting text for the chat. */
    public String getGreeting() {
        return "YadaChat";
    }

//    public boolean canSendMessage() {
//        String msg = messageToSend.get();
//        return msg != null && !msg.isBlank();
//    }

    /**
     * Sends message asynchronously
     * Clears message input if message sent successfully
     */
    public void sendMessageAsync(Consumer<Boolean> callback) {
        String msg = messageToSend.get();
        if (msg == null || msg.isBlank()) {
            System.out.println("Nothing to send!");
            callback.accept(false);
            return;
        }

        try {
            connection.send(msg, success -> {
                if (success) {
                    runOnFx(() -> {
                        if (msg.equals(messageToSend.get())) {
                            messageToSend.set("");
                        }
                        callback.accept(true);
                    });
                } else {
                    callback.accept(false);
                }

            });
        } catch (Exception e) {
            System.out.println("Exception while sending message: " + e.getMessage());
            e.printStackTrace();
            callback.accept(false);
        }
    }

    /**
     * Receiving messages from server for the current topic
     * New messages added to observable list
     */

    public void receiveMessage() {
        connection.receive(m -> {
            if (m == null || m.message() == null || m.message().isBlank()) return;
            runOnFx(() -> messages.add(m));
        });
    }


}
