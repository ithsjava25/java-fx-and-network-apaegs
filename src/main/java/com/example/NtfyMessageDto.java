package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a message received from or sent to the Ntfy server
 * @param id The id of the message
 * @param time The timestamp of the message
 * @param event The type of event
 * @param topic The Topic of the message
 * @param message The actual message
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessageDto(String id, long time, String event, String topic, String message) {
}