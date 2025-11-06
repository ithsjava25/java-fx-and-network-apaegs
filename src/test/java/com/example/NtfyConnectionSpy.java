package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection{

    String message;
    Consumer<NtfyMessageDto> handler;

    @Override
    public boolean send(String message) {
        this.message = message;
        return true;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.handler = messageHandler;
    }

    public void simulateIncoming(NtfyMessageDto msg) {
        if (handler != null) handler.accept(msg);
    }
}