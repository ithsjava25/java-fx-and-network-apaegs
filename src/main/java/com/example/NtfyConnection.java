package com.example;

import java.util.function.Consumer;

/**
 * Interface for sending and receiving messages over an Ntfy connection.
 */

public interface NtfyConnection {
   /**
     * Sends a message asynchronously.
     *
     * @param message the message to send
     * @param callback invoked when send completes; receives true on success, false on failure.
     *                 May be called on any thread.
     */
    void send(String message, Consumer<Boolean> callback);

    /**
     * Registers a handler to receive incoming messages.
     * Calling this multiple times replaces the previous handler.
     *
     * @param messageHandler invoked for each incoming message; may be called on any thread
     */
    void receive(Consumer<NtfyMessageDto> messageHandler);

    /**
     * Changes the current topic.
     * Null or blank not accepted.
     * @param topic the new topic name
     */
    void setCurrentTopic(String topic);


    /**
     * Gets the current topic.
     *
     * @return current topic
     */
    default String getCurrentTopic() { return "mytopic"; }

    /**
     * Gets the user ID for this connection.
     *
     * @return user ID
     */
    default String getUserId() { return "unknown"; }
}
