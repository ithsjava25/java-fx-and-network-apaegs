package com.example;

import java.util.function.Consumer;

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

    void setCurrentTopic(String topic);

    default String getCurrentTopic() { return "mytopic"; }
    default String getUserId() {
        return "unknown";
    }
}
