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
            JSONObject lastJson = null;

            while (!response.equals("end")) {
                if (response.equals("--END OF BATCH--")) {
                    // Конец пакета данных, выводим последнюю точку
                    if (lastJson != null) {
                        System.out.println("Last point in batch: x = " + getFloatValue(lastJson, X)
                                + ", Y = " + getFloatValue(lastJson, Y)
                                + ", Z = " + getFloatValue(lastJson, Z));
                        lastJson = null; // Сбрасываем для следующего пакета
                    }
                } else {
                    // Обновляем последнюю точку в пакете
                    JSONObject json = new JSONObject(response);
                    lastJson = json;
                }
                response = in.readLine();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Float getFloatValue(JSONObject jsonObject, Params fieldName) {
        return Float.valueOf(String.valueOf(jsonObject.get(fieldName.name())));

    }
}
