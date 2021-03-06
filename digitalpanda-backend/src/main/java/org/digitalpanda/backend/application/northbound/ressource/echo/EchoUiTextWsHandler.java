package org.digitalpanda.backend.application.northbound.ressource.echo;

import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Native text handler for bidirectionnal sockets
 *  - https://docs.spring.io/spring-framework/docs/4.3.x/spring-framework-reference/html/websocket.html
 *  - https://github.com/ahmadmu/websocket-rxjs-ng8-spring
 */
public class EchoUiTextWsHandler extends TextWebSocketHandler {


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String textResponse = "[WS-text-full-duplex:" + getTimestamp() + "] echo \"" + message.getPayload() + "\"";
        session.sendMessage(new TextMessage(textResponse));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // do something once the connection is opened
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // do something on connection closed
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        // handle binary message
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // hanedle transport error
    }

    private static String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
