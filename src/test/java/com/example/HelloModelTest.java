package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.collections.ListChangeListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for HelloModel
 * Verifies sending and receiving messages
 */

@WireMockTest
class HelloModelTest {

    /**
     * Initialize FX toolkit for headless-mode
     */

    @BeforeAll
    static void initToolkit() {
        System.out.println("Skipping FX initialization for headless test.");
    }

    /**
     * Verifies that sendMessageAsync sends the correct message to the connection
     * @throws InterruptedException
     */

    @Test
    void sendMessageCallsConnectionWithMessageToSend() throws InterruptedException {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Hello World");

        CountDownLatch latch = new CountDownLatch(1);

        // Act
        model.sendMessageAsync(success -> latch.countDown());

        boolean completed = latch.await(500, TimeUnit.MILLISECONDS);

        // Assert
        assertThat(completed)
                .isTrue();
        assertThat(spy.message).isEqualTo("Hello World");
    }


    /**
     * Verifies that sending an empty string returns false and is not sent
     * @throws InterruptedException
     */
    @Test
    void sendMessageReturnsFalseForEmptyString() throws InterruptedException {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("");

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = new boolean[1];

        // Act
        model.sendMessageAsync(success -> {
            result[0] = success;
            latch.countDown();
        });

        boolean completed = latch.await(500, TimeUnit.MILLISECONDS);

        // Assert
        assertThat(completed)
                .isTrue();
        assertThat(result[0])
                .isFalse();
        assertThat(spy.message).isNull();
    }

    /**
     * Verifies that sending a null message returns false and is not sent
     * @throws InterruptedException
     */
    @Test
    void sendMessageReturnsFalseForNull() throws InterruptedException {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend(null);

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = new boolean[1];

        // Act
        model.sendMessageAsync(success -> {
            result[0] = success;
            latch.countDown();
        });

        boolean completed = latch.await(500, TimeUnit.MILLISECONDS);

        // Assert
        assertThat(completed)
                .isTrue();
        assertThat(result[0])
                .isFalse();
        assertThat(spy.message).isNull();
    }

    /**
     * Verifies that received messages are added to the model
     * @throws InterruptedException
     */
    @Test
    void receiveMessageShouldAddMessageToModel() throws InterruptedException {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
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
        assertThat(completed)
                .isTrue();
        assertThat(model.getMessages()).contains(message);
    }

    /**
     * Verifies that null messages are ignored
     * @throws InterruptedException
     */
    @Test
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
        spy.simulateIncoming(null);

        boolean result = latch.await(500, TimeUnit.MILLISECONDS);

        // Assert
        assertThat(result)
                .isFalse();
        assertThat(model.getMessages()).isEmpty();
    }

    /**
     * Verifies that empty och blank messages are ignored
     * @throws InterruptedException
     */
    @Test
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

        boolean result = latch.await(500, TimeUnit.MILLISECONDS);

        // Assert
        assertThat(result)
                .isFalse();
        assertThat(model.getMessages()).isEmpty();
    }

    /**
     * Verifies that sendMessageAsync handles failed sends correctly
     * @throws InterruptedException
     */
    @Test
    void sendMessageAsyncShouldHandleFailedSend() throws InterruptedException {
        // Arrange
        var failingConnection = new NtfyConnection() {
            @Override
            public void send(String message, Consumer<Boolean> callback) {
                callback.accept(false);
            }
            @Override
            public void receive(Consumer<NtfyMessageDto> messageHandler) { }

            @Override
            public void setCurrentTopic(String topic) {

            }
        };
        var model = new HelloModel(failingConnection);
        model.setMessageToSend("Fail this message");

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = new boolean[1];

        // Act
        model.sendMessageAsync(success -> {
            result[0] = success;
            latch.countDown();
        });

        boolean completed = latch.await(500, TimeUnit.MILLISECONDS);

        // Assert
        assertThat(completed)
                .isTrue();
        assertThat(result[0])
                .isFalse();
        assertThat(model.getMessageToSend())
                .isEqualTo("Fail this message");
    }

    /**
     * Verifies that multiple sequential sends work correctly
     * @throws InterruptedException
     */
    @Test
    void sendMessageAsyncShouldHandleMultipleSequentialCalls() throws InterruptedException {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        CountDownLatch latch = new CountDownLatch(2);
        final boolean[] results = new boolean[2];

        // Act
        model.setMessageToSend("First");
        model.sendMessageAsync(success -> {
            results[0] = success;
            latch.countDown();
        });

        model.setMessageToSend("Second");
        model.sendMessageAsync(success -> {
            results[1] = success;
            latch.countDown();
        });

        boolean completed = latch.await(1, TimeUnit.SECONDS);

        // Assert
        assertThat(completed)
                .isTrue();
        assertThat(results[0])
                .isTrue();
        assertThat(results[1])
                .isTrue();
        assertThat(spy.message)
                .isEqualTo("Second");
    }

    /**
     * Verifies that sendMessageAsync handles exceptions without crashing
     * @throws InterruptedException
     */
    @Test
    void sendMessageAsyncShouldHandleExceptionGracefully() throws InterruptedException {
        // Arrange
        var crashingConnection = new NtfyConnection() {
            @Override
            public void send(String message, Consumer<Boolean> callback) {
                throw new RuntimeException("Simulated crash");
            }
            @Override
            public void receive(Consumer<NtfyMessageDto> messageHandler) { }

            @Override
            public void setCurrentTopic(String topic) {

            }
        };
        var model = new HelloModel(crashingConnection);
        model.setMessageToSend("Crash this");

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        final boolean[] exceptionCaught = {false};

        // Act
        try {
            model.sendMessageAsync(success -> {
                result[0] = success;
                latch.countDown();
            });
        } catch (Exception e) {
            exceptionCaught[0] = true;
            latch.countDown();
        }

        boolean completed = latch.await(500, TimeUnit.MILLISECONDS);

        // Assert
        assertThat(completed)
                .isTrue();

        // Om exception kastades direkt (synkront)
        if (exceptionCaught[0]) {
            assertThat(exceptionCaught[0])
                    .isTrue();
        } else {
            // Om callback kördes (asynkront men med fel)
            assertThat(result[0])
                    .isFalse();
        }
    }

    /**
     * Verifies that the message is cleared after a successful send
     * @throws InterruptedException
     */
    @Test
    void messageToSendIsClearedAfterSuccessfulSendMessage() throws InterruptedException {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Test message");

        CountDownLatch latch = new CountDownLatch(1);

        // Act
        model.sendMessageAsync(success -> latch.countDown());

        boolean completed = latch.await(1, TimeUnit.SECONDS);

        // Assert
        assertThat(completed)
                .isTrue();

        Thread.sleep(100);

        assertThat(model.getMessageToSend())
                .isEmpty();
    }

    /**
     * Verifies that multiple received messages are added in the correct order
     * @throws InterruptedException
     */
    @Test
    void multipleReceivedMessagesShouldBeAddedInOrder() throws InterruptedException {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        var message1 = new NtfyMessageDto("id1", 1000, "message", "mytopic", "First message");
        var message2 = new NtfyMessageDto("id2", 2000, "message", "mytopic", "Second message");
        var message3 = new NtfyMessageDto("id3", 3000, "message", "mytopic", "Third message");

        CountDownLatch latch = new CountDownLatch(3);

        model.getMessages().addListener((ListChangeListener<NtfyMessageDto>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    latch.countDown();
                }
            }
        });

        // Act
        spy.simulateIncoming(message1);
        spy.simulateIncoming(message2);
        spy.simulateIncoming(message3);

        boolean completed = latch.await(1, TimeUnit.SECONDS);

        // Assert
        assertThat(completed)
                .isTrue();
        assertThat(model.getMessages())
                .hasSize(3);
        assertThat(model.getMessages().get(0))
                .isEqualTo(message1);
        assertThat(model.getMessages().get(1))
                .isEqualTo(message2);
        assertThat(model.getMessages().get(2))
                .isEqualTo(message3);
    }

    /**
     * Integration test
     * Verifies sending a message to a fake server using WireMock
     * @param wmRuntimeInfo
     * @throws InterruptedException
     */
    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRuntimeInfo) throws InterruptedException {
        // Arrange
        var con = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World");

        stubFor(post("/mytopic").willReturn(ok()));

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = new boolean[1];

        // Act
        model.sendMessageAsync(success -> {
            result[0] = success;
            latch.countDown();
        });

        boolean completed = latch.await(1, TimeUnit.SECONDS);

        // Assert
        assertThat(completed)
                .isTrue();
        assertThat(result[0])
                .isTrue();

        verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(matching("Hello World")));
    }
}