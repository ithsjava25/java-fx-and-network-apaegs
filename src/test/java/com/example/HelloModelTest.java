package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class HelloModelTest {

    @BeforeAll
    static void initToolkit() {
        if (!Platform.isFxApplicationThread()) {
            Platform.startup(() -> {}); // starta JavaFX-plattformen en gång
        }
    }


    @Test
    @DisplayName("Given a model with messageToSend when calling sendMessage then send method on connection should be called")
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
        stubFor(post(HelloModel.getRoom()).willReturn(ok()));

        model.sendMessage();

        //Verify call made to server
        verify(postRequestedFor(urlEqualTo(HelloModel.getRoom()))
                .withRequestBody(matching("Hello World")));
    }

    @Test
    void receiveMessageShouldAddMessageToModel() throws InterruptedException {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        var message = new NtfyMessageDto("Test", 1, "message", "myroom", "Test");

        // Gör en latch för att vänta på Platform.runLater
        CountDownLatch latch = new CountDownLatch(1);

        // Modifiera spy så den räknar ner latches när meddelande tas emot
        spy.handler = msg -> {
            Platform.runLater(() -> {
                model.getMessages().add(msg);
                latch.countDown(); // signalera att meddelandet lagts till
            });
        };

        // Act
        spy.simulateIncoming(message);

        // Vänta max 1 sekund på att runLater ska köras
        boolean completed = latch.await(1, TimeUnit.SECONDS);

        // Assert
        assertThat(completed).isTrue(); // säkerställ att latch räknades ner
        assertThat(model.getMessages()).contains(message);
    }
}