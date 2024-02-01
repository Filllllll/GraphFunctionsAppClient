package org.example;

import org.json.JSONObject;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import static org.example.Client.getFloatValue;
import static org.example.Params.*;

public class MainApp extends Application {

    private final static int PORT = 8081;
    private final static String HOST = "localhost";

    private Graph3DRenderer graph3DRenderer;

    private TextField tfT0;

    private TextField tfTEnd;
    private TextField tfTStep;
    private TextField tfParam1;
    private TextField tfParam2;
    private TextField tfTEmit;
    private TextField tfFuncName;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ApplicationGraphFunctions");

        GridPane inputGridPane = new GridPane();
        inputGridPane.setPadding(new Insets(10));
        inputGridPane.setHgap(10);
        inputGridPane.setVgap(10);

        inputGridPane.add(new Label("Функция:"), 0, 0);
        tfFuncName = new TextField();
        inputGridPane.add(tfFuncName, 1, 0);
        inputGridPane.add(new Label("t_emit:"), 0, 1);
        tfTEmit = new TextField("0");
        inputGridPane.add(tfTEmit, 1, 1);
        inputGridPane.add(new Label("t_step:"), 0, 2);
        tfTStep = new TextField("0.1");
        inputGridPane.add(tfTStep, 1, 2);
        inputGridPane.add(new Label("t_end:"), 0, 3);
        tfTEnd = new TextField("6");
        inputGridPane.add(tfTEnd, 1, 3);
        inputGridPane.add(new Label("t_0:"), 0, 4);
        tfT0 = new TextField("0");
        inputGridPane.add(tfT0, 1, 4);
        inputGridPane.add(new Label("param1:"), 0, 5);
        tfParam1 = new TextField("1");
        inputGridPane.add(tfParam1, 1, 5);
        inputGridPane.add(new Label("param2:"), 0, 6);
        tfParam2 = new TextField("1");
        inputGridPane.add(tfParam2, 1, 6);

        Button drawGraphButton = new Button("Построить график");

        // Действия для кнопок
        drawGraphButton.setOnAction(event -> {
            // Получение данных из текстовых полей
            if (checkValueOfFields()) { // проверка на корректность полей
                System.out.println("setOnAction");
                double t0 = Double.parseDouble(tfT0.getText());
                double tend = Double.parseDouble(tfTEnd.getText());
                double tStep = Double.parseDouble(tfTStep.getText());
                double param1 = Double.parseDouble(tfParam1.getText());
                double param2 = Double.parseDouble(tfParam2.getText());

                // Рендеринг графика
//                graph3DRenderer.drawPoint(t0, tend, tStep, param1, param2);

                // Сериализация данных в JSON и отправка на сервер
                String jsonMessage = createJson(tfFuncName.getText(), tfTEmit.getText(), tfTStep.getText(), tfTEnd.getText(), tfT0.getText(), tfParam1.getText(), tfParam2.getText());

                try (Socket socket = new Socket(HOST, PORT);
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
                            System.out.println("Point: x = " + getFloatValue(lastJson, X)
                                    + ", Y = " + getFloatValue(lastJson, Y)
                                    + ", Z = " + getFloatValue(lastJson, Z));
                            JSONObject json = new JSONObject(response);
                            lastJson = json;
                            graph3DRenderer.drawPoint(getFloatValue(lastJson, X), getFloatValue(lastJson, Y), getFloatValue(lastJson, Z)); // рисуем точку
                        }
                        response = in.readLine();
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Добавляем все элементы в GridPane
        inputGridPane.add(drawGraphButton, 0, 7, 2, 1);

        StackPane graphContainer = new StackPane();
        SubScene subScene3D = new SubScene(new Group(), 700, 700);
        subScene3D.setFill(Color.ALICEBLUE);
        graphContainer.getChildren().add(subScene3D);
        graph3DRenderer = new Graph3DRenderer(subScene3D);


        HBox mainContainer = new HBox(10, inputGridPane, graphContainer);

        // Создаем основную сцену и отображаем ее
        Scene scene = new Scene(mainContainer, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleSendAction(String jsonMessage) {
        try (Socket socket = new Socket(HOST, PORT);
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

    public static void main(String[] args) {
        launch(args);
    }

    private String createJson(String funcName, String tEmit, String tStep, String tEnd, String t0, String param1, String param2) {
        // Сериализация данных в JSON
        JSONObject json = new JSONObject();
        json.put(FUNC_NAME.name(), funcName);
        json.put(T_EMIT.name(), tEmit);
        json.put(T_STEP.name(), tStep);
        json.put(T_END.name(), tEnd);
        json.put(T_0.name(), t0);
        json.put(PARAM1.name(), param1);
        json.put(PARAM2.name(), param2);
        return json.toString();
    }

    private boolean checkValueOfFields() {
        boolean res = true;

        res = isRes(res, tfTEmit, tfT0, tfTEnd);

        res = isRes(res, tfTStep, tfParam1, tfParam2);

        return res;
    }

    private boolean isRes(boolean res, TextField tfTStep, TextField tfParam1, TextField tfParam2) {
        try {
            Double.parseDouble(tfTStep.getText());
        } catch (IllegalArgumentException ex) {
            tfTStep.setText("Need a number!");
            res = false;
        }

        try {
            Double.parseDouble(tfParam1.getText());
        } catch (IllegalArgumentException ex) {
            tfParam1.setText("Need a number!");
            res = false;
        }

        try {
            Double.parseDouble(tfParam2.getText());
        } catch (IllegalArgumentException ex) {
            tfParam2.setText("Need a number!");
            res = false;
        }
        return res;
    }
}
