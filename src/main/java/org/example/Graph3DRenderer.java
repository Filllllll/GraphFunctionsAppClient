package org.example;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.util.ArrayList;
import java.util.List;

public class Graph3DRenderer {
    private final SubScene subScene3D;
    private final Group group3D;
    private double anchorX, anchorY, anchorAngleX = 0, anchorAngleY = 0;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    private List<Sphere> spheres = new ArrayList<>();
    private double scaleFactor = 30.0;

    public Graph3DRenderer(SubScene subScene3D) {
        this.subScene3D = subScene3D;
        this.group3D = (Group) subScene3D.getRoot();
        initializeCamera();
        addMouseControl();
        addAxes();
    }

    private void initializeCamera() {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(
                new Rotate(-20, Rotate.Y_AXIS),
                new Rotate(-20, Rotate.X_AXIS),
                new Translate(0, 0, -50)
        );

        group3D.getChildren().add(new SubScene(new Group(), 700, 700, true, null));
        group3D.getChildren().get(0).setTranslateX(350); // Переместим SubScene, чтобы он был в центре
        group3D.getChildren().get(0).setTranslateY(350); // Переместим SubScene, чтобы он был в центре
        group3D.setTranslateX(350); // Пример смещения на 50 единиц вправо
        group3D.setTranslateY(330); // Пример смещения на 50 единиц вниз
        ((SubScene) group3D.getChildren().get(0)).setFill(Color.ALICEBLUE);
        ((SubScene) group3D.getChildren().get(0)).setCamera(camera);
    }

    private void addMouseControl() {
        subScene3D.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });

        subScene3D.setOnMouseDragged(event -> {
            rotateX.setAngle(anchorAngleX - (anchorY - event.getSceneY()));
            rotateY.setAngle(anchorAngleY + anchorX - event.getSceneX());
        });

        group3D.getTransforms().addAll(rotateX, rotateY);
    }

    private void addAxes() {
        // Ось X (красная)
        Cylinder xAxis = new Cylinder(1, 400);
        xAxis.setMaterial(new PhongMaterial(Color.RED));
        xAxis.setRotationAxis(Rotate.Z_AXIS);
        xAxis.setRotate(90);

        // Ось Y (зеленая)
        Cylinder yAxis = new Cylinder(1, 400);
        yAxis.setMaterial(new PhongMaterial(Color.GREEN));

        // Ось Z (синяя)
        Cylinder zAxis = new Cylinder(1, 400);
        zAxis.setMaterial(new PhongMaterial(Color.BLUE));
        zAxis.setRotationAxis(Rotate.X_AXIS);
        zAxis.setRotate(90);

        group3D.getChildren().addAll(xAxis, yAxis, zAxis);

        // Добавление подписей к осям и числовых делений
        int len = 210;
        addAxisLabel("X", len, 0, 0);
        addAxisLabel("Y", 0, len, 0);
        addAxisLabel("Z", 0, 0, len);

        int numberDivision = 20;

        addTickMarksOnXAxis(numberDivision);  // 10 делений на оси X
        addTickMarksOnYAxis(numberDivision);  // 10 делений на оси Y
        addTickMarksOnZAxis(numberDivision);  // 10 делений на оси Z
    }


    private void addTickMarksOnXAxis(int numTickMarks) {
        double axisLength = 400;
        double tickMarkSpacing = axisLength / numTickMarks;

        // Найдем точку пересечения осей
        double originX = 0;
        double originY = 0;
        double originZ = 0;

        // Начинаем добавление делений с половины влево от точки пересечения осей
        for (int i = -numTickMarks / 2; i <= numTickMarks / 2; i++) {
            double x = originX + i * tickMarkSpacing;
            double y = originY;
            double z = originZ;

            addTickMark(String.valueOf(i), x, y, z);
        }

    }

    private void addTickMarksOnYAxis(int numTickMarks) {
        double axisLength = 400;
        double tickMarkSpacing = axisLength / numTickMarks;

        // Найдем точку пересечения осей
        double originX = 0;
        double originY = 0;
        double originZ = 0;

        // Начинаем добавление делений с половины влево от точки пересечения осей
        for (int i = -numTickMarks / 2; i <= numTickMarks / 2; i++) {
            double x = originX;
            double y = originY + i * tickMarkSpacing;
            double z = originZ;

            addTickMark(String.valueOf(i), x, y, z);
        }
    }

    private void addTickMarksOnZAxis(int numTickMarks) {
        double axisLength = 400;
        double tickMarkSpacing = axisLength / numTickMarks;

        // Найдем точку пересечения осей
        double originX = 0;
        double originY = 0;
        double originZ = 0;

        // Начинаем добавление делений с половины влево от точки пересечения осей
        for (int i = -numTickMarks / 2; i <= numTickMarks / 2; i++) {
            double x = originX;
            double y = originY;
            double z = originZ + i * tickMarkSpacing;

            addTickMark(String.valueOf(i), x, y, z);
        }
    }

    private void addTickMark(String label, double x, double y, double z) {
        Text tickMark = new Text(label);
        tickMark.setFill(Color.BLACK);
        tickMark.setFont(Font.font("Arial", 10));
        tickMark.setTranslateX(x);
        tickMark.setTranslateY(y);
        tickMark.setTranslateZ(z);
        group3D.getChildren().add(tickMark);
    }

    private void addAxisLabel(String label, double x, double y, double z) {
        Text text = new Text(label);
        text.setFill(Color.BLACK);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        text.setTranslateX(x);
        text.setTranslateY(y);
        text.setTranslateZ(z);
        group3D.getChildren().add(text);
    }

    public void drawPoint(float x, float y, float z) {
        // Создаем сферу для отображения точки
        Sphere sphere = new Sphere(3);
        sphere.setTranslateX(x * scaleFactor);
        sphere.setTranslateY(y * scaleFactor);
        sphere.setTranslateZ(z * scaleFactor);

        // Отображаем сферу на графике
        group3D.getChildren().add(sphere);

        // Добавляем сферу в список для возможности удаления
        spheres.add(sphere);
    }

    // Метод для вычисления расстояния между двумя точками
    private double getDistance(Sphere sphere1, Sphere sphere2) {
        return Math.sqrt(Math.pow(sphere2.getTranslateX() - sphere1.getTranslateX(), 2) +
                Math.pow(sphere2.getTranslateY() - sphere1.getTranslateY(), 2) +
                Math.pow(sphere2.getTranslateZ() - sphere1.getTranslateZ(), 2));
    }

    // Метод для вычисления угла поворота между двумя точками
    private double getRotationAngle(Sphere sphere1, Sphere sphere2) {
        double deltaX = sphere2.getTranslateX() - sphere1.getTranslateX();
        double deltaY = sphere2.getTranslateY() - sphere1.getTranslateY();
        return Math.toDegrees(Math.atan2(deltaY, deltaX));
    }

    public void finishRendering() {
        // Вызывается по завершению приема точек от сервера
        // Очищаем список сфер
        spheres.clear();
    }
}