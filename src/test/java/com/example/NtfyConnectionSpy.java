package com.example;

import java.util.function.Consumer;


/**
 * A spy implementation of {@link NtfyConnection} used for testing.
 * Captures sent messages and allows simulating incoming messages.
 */
public class NtfyConnectionSpy implements NtfyConnection{

    /** Stores the last message sent via send(). */
    String message;

    /** Handler to receive incoming messages. */
    private volatile Consumer<NtfyMessageDto> handler;

    /**
     * Simulates sending a message.
     * Stores the message and invokes the callback with true asynchronously.
     *
     * @param message  the message to send
     * @param callback called with true to indicate successful send
     */
    @Override
    public void send(String message, Consumer<Boolean> callback) {
        this.message = message;
        new Thread(() -> callback.accept(true)).start();
    }

    /**
     * Registers a handler to receive incoming messages.
     *
     * @param messageHandler the consumer that handles incoming messages
     */
    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.handler = messageHandler;
    }

    /**
     * Sets the current topic.
     * This spy does not implement topic handling.
     *
     * @param topic the topic to set
     */
    @Override
    public void setCurrentTopic(String topic) { }

    /**
     * Simulates an incoming message by calling the registered handler, if any.
     *
     * @param msg the message to simulate as incoming
     */
    public void simulateIncoming(NtfyMessageDto msg) {
        Consumer<NtfyMessageDto> localHandler = handler;
        if (localHandler != null) {
            localHandler.accept(msg);
        }
    }
}