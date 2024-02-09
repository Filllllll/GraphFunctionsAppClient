package org.example;

import javafx.application.Platform;
import javafx.scene.control.Slider;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.example.Client.getFloatValue;
import static org.example.Params.*;

public class MainApp extends Application {

    private final static int PORT = 8081;
    private final static String HOST = "localhost";

    private TextField tfT0;
    private TextField tfTEnd;
    private TextField tfTStep;
    private TextField tfParam1;
    private TextField tfParam2;
    private TextField tfTEmit;
    private TextField tfFuncName;
    private Slider slider; // Добавляем слайдер

    private Group group3D;

    private List<Sphere> listOfCurrentPoints = new ArrayList<>();
    private List<List<Point3D>> listOfListPoints= new ArrayList<>();

    private double scaleFactor = 0;
    private final double LIMIT_Z = 5;

    Graph3DRenderer1 graph;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ApplicationGraphFunctions");
        group3D = new Group();
        SubScene subScene3D = new SubScene(group3D, 700, 700);

        GridPane inputGridPane = new GridPane();
        inputGridPane.setPadding(new Insets(10));
        inputGridPane.setHgap(10);
        inputGridPane.setVgap(10);
        StackPane graphContainer = new StackPane();

        inputGridPane.add(new Label("Функция:"), 0, 0);
        tfFuncName = new TextField();
        inputGridPane.add(tfFuncName, 1, 0);
        inputGridPane.add(new Label("t_emit:"), 0, 1);
        tfTEmit = new TextField("200");
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

        // Инициализируем слайдер
        slider = new Slider();
        slider.setMin(0);
        slider.setMax(0); // Измените максимальное значение, когда у вас будут точки
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);

        // Добавляем все элементы в GridPane
        inputGridPane.add(drawGraphButton, 0, 7, 2, 1);
        inputGridPane.add(slider, 0, 8, 2, 1);

        subScene3D.setFill(Color.ALICEBLUE);
        graphContainer.getChildren().add(subScene3D);

        AtomicReference<HBox> mainContainer = new AtomicReference<>(new HBox(10, inputGridPane, graphContainer));

        // Создаем основную сцену и отображаем ее
        AtomicReference<Scene> scene = new AtomicReference<>(new Scene(mainContainer.get(), 1200, 800));
        primaryStage.setScene(scene.get());
        primaryStage.show();

        // Действия для кнопок
        List<Point3D> list = new ArrayList<>();

        drawGraphButton.setOnAction(event -> {
            scaleFactor = 200 / Math.max(Math.abs(Double.valueOf(tfTEnd.getText())), Math.abs(Double.valueOf(tfT0.getText())));
            System.out.println("scalefactor - " + scaleFactor);
            graph = new Graph3DRenderer1(subScene3D, Math.max(Math.abs(Double.valueOf(tfTEnd.getText())), Math.abs(Double.valueOf(tfT0.getText()))));
            System.out.println("graph");

            // Получение данных из текстовых полей
            if (checkValueOfFields()) { // проверка на корректность полей
                System.out.println("checkValueOfFields");
                // Удаляем точки
                listOfListPoints = new ArrayList<>(); // обнуляем историю
                group3D.getChildren().removeAll(listOfCurrentPoints);
                slider.setMax(0); // обнуляем слайдер
                System.out.println("group3D");

                // Сериализация данных в JSON и отправка на сервер
                String jsonMessage = createJson(tfFuncName.getText(), tfTEmit.getText(), tfTStep.getText(), tfTEnd.getText(), tfT0.getText(), tfParam1.getText(), tfParam2.getText());
                Thread socketThread = new Thread(() -> {
                    try (Socket socket = new Socket(HOST, PORT);
                         DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                        // Отправка длины сообщения
                        out.writeInt(jsonMessage.length());
                        // Отправка самого сообщения
                        out.writeUTF(jsonMessage);

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

                                    Thread draw = new Thread(() -> {
                                        Platform.runLater(() -> setPoints(list));
                                    });
                                    draw.start();
                                    Thread.sleep(200);
                                    list.clear();
                                }

                            } else {
                                JSONObject json = new JSONObject(response);
                                lastJson = json;
                                list.add(new Point3D(getFloatValue(lastJson, X), getFloatValue(lastJson, Y), getFloatValue(lastJson, Z)));
                            }
                            response = in.readLine();
                        }
                        Thread draw = new Thread(() -> {
                            Platform.runLater(() -> setPoints(list));
                        });
                        draw.start();
                        Thread.sleep(200);
                        list.clear();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                });
                socketThread.start();
            }
        });

        // Добавляем слушатель изменений значения слайдера
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int index = newValue.intValue(); // Получаем текущее значение слайдера

            Platform.runLater(() -> setPoints(index));
        });
    }

    private void setPoints(List<Point3D> list) {
        listOfListPoints.add(new ArrayList<Point3D>(list));

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.BLUE); // Устанавливаем основной цвет
        material.setSpecularColor(Color.WHITE); // Устанавливаем цвет бликов
        material.setSpecularPower(64); // Устанавливаем интенсивность бликов

        // Устанавливаем прозрачность (значение от 0.0 до 1.0, где 0.0 - полностью прозрачный, 1.0 - непрозрачный)
        material.setDiffuseColor(Color.rgb(0, 0, 255, 0.3)); // Устанавливаем синий цвет с прозрачностью 0.5

        for (int i = 0; i < list.size(); i++) {
            double x = list.get(i).getX();
            double y = list.get(i).getY();
            double z = list.get(i).getZ();
            if (z > LIMIT_Z) continue;

            Sphere sphere = new Sphere(3);
            sphere.setTranslateX(x * scaleFactor);
            sphere.setTranslateY(y * scaleFactor);
            sphere.setTranslateZ(z * scaleFactor);

            // Устанавливаем материал для сферы
            sphere.setMaterial(material);

            // Отображаем сферу на графике
            group3D.getChildren().add(sphere);

            // Добавляем точку в лист с точками в текущем отображении
            listOfCurrentPoints.add(sphere);
        }


        slider.setMax(slider.getMax() + 1); // увеличиваем на одно деление слайдер

    }

    private void setPoints(int ind) {
        group3D.getChildren().removeAll(listOfCurrentPoints); // удаляем все что есть на графике

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.BLUE); // Устанавливаем основной цвет
        material.setSpecularColor(Color.WHITE); // Устанавливаем цвет бликов
        material.setSpecularPower(64); // Устанавливаем интенсивность бликов
        // Устанавливаем прозрачность (значение от 0.0 до 1.0, где 0.0 - полностью прозрачный, 1.0 - непрозрачный)
        material.setDiffuseColor(Color.rgb(0, 0, 255, 0.3)); // Устанавливаем синий цвет с прозрачностью 0.5

        for (int j = 0; j < ind; j++) {
            List<Point3D> list = listOfListPoints.get(j);
            for (int i = 0; i < list.size(); i++) {
                double x = list.get(i).getX();
                double y = list.get(i).getY();
                double z = list.get(i).getZ();
                if (z > LIMIT_Z) continue; // ограничение на ось Z

                Sphere sphere = new Sphere(3);
                sphere.setTranslateX(x * scaleFactor);
                sphere.setTranslateY(y * scaleFactor);
                sphere.setTranslateZ(z * scaleFactor);

                // Устанавливаем материал для сферы
                sphere.setMaterial(material);

                // Отображаем сферу на графике
                group3D.getChildren().add(sphere);

                // Добавляем точку в лист с точками в текущем отображении
                listOfCurrentPoints.add(sphere);
            }
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

        try {
            Integer.parseInt(tfTEmit.getText());
        } catch (IllegalArgumentException ex) {
            tfTEmit.setText("Need a Integer!");
            res = false;
        }

        if (Integer.parseInt(tfTEnd.getText()) < Integer.parseInt(tfT0.getText())) {
            tfTEnd.setText("t_end must be >= t_0");
            tfT0.setText("t_0 must be <= t_end");
            res = false;
        }

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
