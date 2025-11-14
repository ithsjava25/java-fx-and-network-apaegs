package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implementation of {@link NtfyConnection} using ntfy.sh-compatible HTTP calls.
 * Supports sending messages and receiving a continuous stream of JSON messages
 * from a selected topic.
 */
public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
    private final String userId;
    private String currentTopic;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a connection using values from a .env file:
     * HOST_NAME, USER_ID and optionally DEFAULT_TOPIC.
     */
    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        this.hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
        this.userId = Objects.requireNonNull(dotenv.get("USER_ID"), "USER_ID");
        this.currentTopic = dotenv.get("DEFAULT_TOPIC", "mytopic");
    }

    /**
     * Creates a connection with the given hostname and default values
     * intended mainly for tests.
     *
     * @param hostName ntfy server base URL
     */
    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
        this.userId = "testuser";
        this.currentTopic = "mytopic";
    }

//    public NtfyConnectionImpl(String hostName, String userId, String topic) {
//        this.hostName = hostName;
//        this.userId = userId;
//        this.currentTopic = topic;
//    }

    /** @return the user ID used for outgoing messages */
    public String getUserId() {
        return userId;
    }

    /** @return the current topic */
    public String getCurrentTopic() {
        return currentTopic;
    }

    /**
     * Sets the topic to use for sending and receiving messages.
     *
     * @param topic the new topic name
     */
    public void setCurrentTopic(String topic) {
        this.currentTopic = topic;
    }

    /**
     * Send a message to the current topic asynchronously
     *
     * @param message the message to send
     * @param callback invoked when send completes; receives true on success, false on failure.
     *                 May be called on any thread.
     */

    @Override
    public void send(String message, Consumer<Boolean> callback) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .header("Cache", "no")
                .header("X-User-Id", userId)
                .uri(URI.create(hostName + "/" + currentTopic))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> response.statusCode() / 100 == 2)
                .exceptionally(ex -> {
                    System.out.println("Error sending message: " + ex.getMessage());
                    return false;
                })
                .thenAccept(callback);
    }

    /**
     * Starts receiving messages from the current topic.
     * Calls the provided handler for each message
     *
     * @param messageHandler invoked for each incoming message; may be called on any thread
     */

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/" + currentTopic + "/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s -> {
                            try {
                                return mapper.readValue(s, NtfyMessageDto.class);
                            } catch (Exception e) {
                                System.out.println("Failed to parse message: " + e.getMessage());
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .peek(System.out::println)
                        .forEach(messageHandler));
    }
}