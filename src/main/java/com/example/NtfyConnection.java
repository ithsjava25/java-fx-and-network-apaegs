package com.example;

import java.util.function.Consumer;

public interface NtfyConnection {

    default boolean send(String message) {
        final boolean[] result = {false};
        send(message, success -> result[0] = success);
        return result[0];
    }

    public void send(String message, Consumer<Boolean> callback);

    public void receive(Consumer<NtfyMessageDto> messageHandler);

}