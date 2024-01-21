package org.example;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import static org.example.Params.*;

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
            System.out.println("Отправка длины сообщения - " + jsonMessage.length());
            // Отправка самого сообщения
            out.writeUTF(jsonMessage);
            System.out.println("Отправка самого сообщения - " + jsonMessage);

            // Чтение подтверждения от сервера
            String response = in.readLine();

            while (!response.equals("end")) {
                JSONObject json = new JSONObject(response);
                System.out.println("Point x = " + getFloatValue(json, X) + ", Y = "
                        + getFloatValue(json, Y) + ", Z = " + getFloatValue(json, Z));

                response = in.readLine();
                System.out.println("new response - " + response);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Float getFloatValue(JSONObject jsonObject, Params fieldName) {
        return Float.valueOf(String.valueOf(jsonObject.get(fieldName.name())));

    }
}
