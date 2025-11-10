package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

@WireMockTest
class HelloModelTest {

    @BeforeAll
    static void initToolkit() {
        if (!Platform.isFxApplicationThread()) {
            Platform.startup(() -> {}); // starta JavaFX-plattformen en gång
        }
    }

    @Test
    void sendMessageCallsConnectionWithMessageToSend() {
        //Arrange  Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Hello World");
        //Act  When
        model.sendMessage();
        //Assert   Then
        assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRuntimeInfo) {
        var con = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World");
        stubFor(post("/mytopic").willReturn(ok()));

        model.sendMessage();

        //Verify call made to server
        verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(matching("Hello World")));
    }

    @Test
    void sendMessageReturnsFalseForNullOrBlankMessage() {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        String[] testValues = {"", null};

        for (String value : testValues) {

            model.setMessageToSend(value);

            // Act
            boolean result = model.sendMessage();

            // Assert
            assertFalse(result);
            assertNull(spy.message);
        }
    }


    @Test
    void sendMessageReturnsFalseForEmptyString() {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("");

        // Act
        boolean result = model.sendMessage();

        // Assert
        assertThat(result).isFalse();
        assertThat(spy.message).isNull();
    }

    @Test
    void sendMessageReturnsFalseForNull() {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend(null);

        // Act
        boolean result = model.sendMessage();

        // Assert
        assertThat(result).isFalse();
        assertThat(spy.message).isNull();
    }

    @Test
    void receiveMessageShouldAddMessageToModel() throws InterruptedException {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);  // receiveMessage() anropas automatiskt i constructor
        var message = new NtfyMessageDto("Test", 1, "message", "myroom", "Test");

        CountDownLatch latch = new CountDownLatch(1);

        model.getMessages().addListener((ListChangeListener<NtfyMessageDto>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    latch.countDown();
                }
            }
        });

        // Act
        spy.simulateIncoming(message);

        boolean completed = latch.await(1, TimeUnit.SECONDS);

        // Assert
        assertThat(completed).isTrue();
        assertThat(model.getMessages()).contains(message);
    }

    @Test
    @DisplayName("receiveMessage should ignore null message safely")
    void receiveMessageShouldIgnoreNullMessage() throws InterruptedException {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        CountDownLatch latch = new CountDownLatch(1);
        model.getMessages().addListener((ListChangeListener<NtfyMessageDto>) change -> {
            while (change.next()) {
                if (change.wasAdded()) latch.countDown();
            }
        });

        // Act
        spy.simulateIncoming(null); // simulate broken incoming event

        // Assert
        boolean noAdd = latch.await(500, TimeUnit.MILLISECONDS);
        assertThat(noAdd).isFalse(); // should not trigger addition
        assertThat(model.getMessages()).isEmpty();
    }

    @Test
    @DisplayName("receiveMessage should ignore empty or blank messages")
    void receiveMessageShouldIgnoreEmptyOrBlankMessages() throws InterruptedException {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        CountDownLatch latch = new CountDownLatch(1);
        model.getMessages().addListener((ListChangeListener<NtfyMessageDto>) change -> {
            while (change.next()) {
                if (change.wasAdded()) latch.countDown();
            }
        });

        // Act
        var blank = new NtfyMessageDto("id1", 1, "message", "room", "   ");
        var empty = new NtfyMessageDto("id2", 2, "message", "room", "");

        spy.simulateIncoming(blank);
        spy.simulateIncoming(empty);

        // Assert
        boolean noAdd = latch.await(500, TimeUnit.MILLISECONDS);
        assertThat(noAdd).isFalse(); // no add events
        assertThat(model.getMessages()).isEmpty();
    }



}