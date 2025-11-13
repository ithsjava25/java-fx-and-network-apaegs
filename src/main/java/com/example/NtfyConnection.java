package com.example;

import java.util.function.Consumer;

public interface NtfyConnection {

    default boolean send(String message) {
        final boolean[] result = {false};
        send(message, success -> result[0] = success);
        return result[0];
    }

    void send(String message, Consumer<Boolean> callback);

    void receive(Consumer<NtfyMessageDto> messageHandler);

    // Nya metoder för topic-hantering
    default String getCurrentTopic() {
        return "mytopic";
    }

    default void setCurrentTopic(String topic) {
        // Default implementation gör ingenting
    }

    default String getUserId() {
        return "unknown";
    }
}