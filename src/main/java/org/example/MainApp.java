package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import org.json.JSONObject;

public class MainApp extends Application {
    private TextArea textArea; // Это переменная класса, которая должна быть инициализирована
    private Graph3DRenderer graph3DRenderer;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ApplicationGraphFunctions");

        VBox vBox = new VBox(10); // Установка расстояния между элементами в VBox
        vBox.setPadding(new Insets(10)); // Установка отступа для VBox

        // Создаем горизонтальные контейнеры для каждой пары "метка + текстовое поле"
        HBox hbFuncName = new HBox(10, new Label("Функция:"), new TextField("user")); // TODO: clear value of fields
        HBox hbTEmit = new HBox(10, new Label("t_emit:"), new TextField("1"));
        HBox hbTStep = new HBox(10, new Label("t_step:"), new TextField("0.1"));
        HBox hbTEnd = new HBox(10, new Label("t_end:"), new TextField("6.28"));
        HBox hbT0 = new HBox(10, new Label("t_0:"), new TextField("0"));
        HBox hbParam1 = new HBox(10, new Label("param1:"), new TextField("1"));
        HBox hbParam2 = new HBox(10, new Label("param2:"), new TextField("1"));

        // Текстовая область для вывода информации
        textArea = new TextArea();
        textArea.setEditable(false);

        // Создаем кнопки и устанавливаем их обработчики событий
        Button drawGraphButton = new Button("Построить график");
        drawGraphButton.setMaxWidth(Double.MAX_VALUE);

        Button startServerButton = new Button("Запустить сервер");
        startServerButton.setMaxWidth(Double.MAX_VALUE);

        // Объединяем обработчики событий для кнопки построения графика
        drawGraphButton.setOnAction(event -> {
            double t0 = Double.parseDouble(((TextField) hbT0.getChildren().get(1)).getText());
            double tend = Double.parseDouble(((TextField) hbTEnd.getChildren().get(1)).getText());
            double tStep = Double.parseDouble(((TextField) hbTStep.getChildren().get(1)).getText());
            double param1 = Double.parseDouble(((TextField) hbParam1.getChildren().get(1)).getText());
            double param2 = Double.parseDouble(((TextField) hbParam2.getChildren().get(1)).getText());

            // Построение графика
            graph3DRenderer.renderGraph(t0, tend, tStep, param1, param2);

            // Подготовка данных для отправки на сервер
            String funcName = ((TextField) hbFuncName.getChildren().get(1)).getText();
            String tEmit = ((TextField) hbTEmit.getChildren().get(1)).getText();

            String json = createJson(funcName, tEmit, String.valueOf(tStep),
                    String.valueOf(tend), String.valueOf(t0),
                    String.valueOf(param1), String.valueOf(param2));
            handleSendAction(json);
        });

        SubScene subScene3D = new SubScene(new Group(), 600, 600);
        subScene3D.setFill(Color.ALICEBLUE);

        // Установка камеры
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        camera.getTransforms().addAll(new Translate(0, 0, -200)); // Подвинем камеру назад
        subScene3D.setCamera(camera);

        graph3DRenderer = new Graph3DRenderer(subScene3D);

        // Добавляем все элементы в VBox
        vBox.getChildren()
                .addAll(hbFuncName,
                        hbTEmit,
                        hbTStep,
                        hbTEnd,
                        hbT0,
                        hbParam1,
                        hbParam2,
                        drawGraphButton,
                        startServerButton,
                        textArea,
                        subScene3D
                );

        Scene scene = new Scene(vBox, 800, 600); // Устанавливаем размер сцены
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleSendAction(String text) { // client
        textArea.appendText("Данные отправлены: " + text + "\n");
        Client client = new Client("localhost", 8081, textArea);
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