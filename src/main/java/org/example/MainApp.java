package org.example;

import org.json.JSONObject;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainApp extends Application {
    private TextArea textArea; // Это переменная класса, которая должна быть инициализирована
    private Graph3DRenderer graph3DRenderer;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ApplicationGraphFunctions");

        GridPane inputGridPane = new GridPane();
        inputGridPane.setPadding(new Insets(10));
        inputGridPane.setHgap(10);
        inputGridPane.setVgap(10);

        inputGridPane.add(new Label("Функция:"), 0, 0);
        TextField tfFuncName = new TextField();
        inputGridPane.add(tfFuncName, 1, 0);
        inputGridPane.add(new Label("t_emit:"), 0, 1);
        TextField tfTEmit = new TextField();
        inputGridPane.add(tfTEmit, 1, 1);
        inputGridPane.add(new Label("t_step:"), 0, 2);
        TextField tfTStep = new TextField();
        inputGridPane.add(tfTStep, 1, 2);
        inputGridPane.add(new Label("t_end:"), 0, 3);
        TextField tfTEnd = new TextField();
        inputGridPane.add(tfTEnd, 1, 3);
        inputGridPane.add(new Label("t_0:"), 0, 4);
        TextField tfT0 = new TextField();
        inputGridPane.add(tfT0, 1, 4);
        inputGridPane.add(new Label("param1:"), 0, 5);
        TextField tfParam1 = new TextField();
        inputGridPane.add(tfParam1, 1, 5);
        inputGridPane.add(new Label("param2:"), 0, 6);
        TextField tfParam2 = new TextField();
        inputGridPane.add(tfParam2, 1, 6);

        textArea = new TextArea();
        textArea.setEditable(false);
        Button drawGraphButton = new Button("Построить график");
        Button startServerButton = new Button("Запустить сервер");

        // Действия для кнопок
        drawGraphButton.setOnAction(event -> {
            // Получение данных из текстовых полей
            double t0 = Double.parseDouble(tfT0.getText());
            double tend = Double.parseDouble(tfTEnd.getText());
            double tStep = Double.parseDouble(tfTStep.getText());
            double param1 = Double.parseDouble(tfParam1.getText());
            double param2 = Double.parseDouble(tfParam2.getText());

            // Рендеринг графика
            graph3DRenderer.renderGraph(t0, tend, tStep, param1, param2);

            // Сериализация данных в JSON и отправка на сервер
            String json = createJson(tfFuncName.getText(), tfTEmit.getText(), tfTStep.getText(), tfTEnd.getText(), tfT0.getText(), tfParam1.getText(), tfParam2.getText());
            handleSendAction(json);
        });

        // Добавляем все элементы в GridPane
        inputGridPane.add(drawGraphButton, 0, 7, 2, 1);
        inputGridPane.add(startServerButton, 0, 8, 2, 1);
        inputGridPane.add(textArea, 0, 9, 2, 1);

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


    private void handleSendAction(String text) {
        textArea.appendText("Данные отправлены: " + text + "\n");
        Client client = new Client("localhost", 8080, textArea);
        client.sendMessage(text);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private String createJson(String funcName, String tEmit, String tStep, String tEnd, String t0, String param1, String param2) {
        // Сериализация данных в JSON
        JSONObject json = new JSONObject();
        json.put("func_name", funcName);
        json.put("t_emit", tEmit);
        json.put("t_step", tStep);
        json.put("t_end", tEnd);
        json.put("t_0", t0);
        json.put("param1", param1);
        json.put("param2", param2);
        return json.toString();
    }
}
