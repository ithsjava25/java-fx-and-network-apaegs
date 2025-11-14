package com.example;

import java.util.function.Consumer;

/**
 * Interface for sending and receiving messages over a Ntfy connection.
 */

public interface NtfyConnection {
   /**
     * Sends a message asynchronously.
     *
     * @param message the message to send (must not be null)
     * @param callback invoked when send completes; receives true on success, false on failure.
     *                 Must not be null. May be called on any thread.
     * @throws IllegalArgumentException if message or callback is null
     */
    void send(String message, Consumer<Boolean> callback);

    /**
     * Registers a handler to receive incoming messages.
     * Calling this multiple times replaces the previous handler.
     *
     * @param messageHandler invoked for each incoming message; may be called on any thread.
     *                       Must not be null. Pass null to unregister the handler.
     * @throws IllegalArgumentException if messageHandler is null
     */
    void receive(Consumer<NtfyMessageDto> messageHandler);

    /**
     * Changes the current topic.
     * Null or blank not accepted.
     *
     * @param topic the new topic name (must not be null or blank)
     * @throws IllegalArgumentException if topic is null or blank
     * @implSpec Changing the topic may affect active subscriptions depending on the implementation.
     *           Implementations should document their specific behavior.
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
