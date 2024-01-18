package org.example;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void sendMessage(String jsonMessage) {
        try (Socket socket = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Отправка длины сообщения
            out.writeInt(jsonMessage.length());
            // Отправка самого сообщения
            out.writeUTF(jsonMessage);

            // Чтение подтверждения от сервера
            String response = in.readLine();

            // Чтение данных с сервера (получение данных для графика)
            String dataJson = in.readLine();
            Platform.runLater(() -> {
                // Обработка полученного JSON и построение графика
                // ...
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
