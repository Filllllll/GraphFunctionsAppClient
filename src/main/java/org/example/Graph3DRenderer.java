package org.example;

import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Graph3DRenderer {
    private final SubScene subScene3D;
    private final Group group3D;
    private double anchorX, anchorY, anchorAngleX = 0, anchorAngleY = 0;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

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
        subScene3D.setCamera(camera);
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
        Cylinder xAxis = new Cylinder(0.01, 400);
        Cylinder yAxis = new Cylinder(0.01, 400);
        Cylinder zAxis = new Cylinder(0.01, 400);

        xAxis.setMaterial(new PhongMaterial(Color.RED));
        yAxis.setMaterial(new PhongMaterial(Color.GREEN));
        zAxis.setMaterial(new PhongMaterial(Color.BLUE));

        xAxis.setRotationAxis(Rotate.Z_AXIS);
        xAxis.setRotate(90);

        yAxis.setTranslateY(200);

        zAxis.setRotationAxis(Rotate.X_AXIS);
        zAxis.setRotate(90);
        zAxis.setTranslateZ(200);

        group3D.getChildren().addAll(xAxis, yAxis, zAxis);
    }

    public void renderGraph(double t0, double tend, double tstep, double param1, double param2) {
        Function3D function = new Function3D();
        group3D.getChildren().clear();
        addAxes();

        int size = (int) ((tend - t0) / tstep) + 1; // Количество точек по оси
        TriangleMesh mesh = new TriangleMesh();

        // Создание вершин
        for (double x = t0; x <= tend; x += tstep) {
            for (double y = t0; y <= tend; y += tstep) {
                double z = function.compute(x, y, param1, param2);
                mesh.getPoints().addAll((float) x, (float) y, (float) z);
            }
        }

        // Создание текстурных координат (не используются в данном примере)
        mesh.getTexCoords().addAll(0, 0);

        // Создание граней
        for (int x = 0; x < size - 1; x++) {
            for (int y = 0; y < size - 1; y++) {
                int tl = x * size + y; // top-left
                int tr = tl + 1; // top-right
                int bl = tl + size; // bottom-left
                int br = bl + 1; // bottom-right

                // Создание двух треугольников для каждой ячейки сетки
                mesh.getFaces().addAll(tl, 0, bl, 0, tr, 0);
                mesh.getFaces().addAll(tr, 0, bl, 0, br, 0);
            }
        }

        // Создание объекта MeshView для отображения сетки
        MeshView meshView = new MeshView(mesh);
        meshView.setDrawMode(DrawMode.LINE);
        meshView.setMaterial(new PhongMaterial(Color.BLUE));

        // Поворот и масштабирование графика для лучшего отображения
        meshView.setRotationAxis(Rotate.Y_AXIS);
        meshView.setRotate(180);
        meshView.setScaleX(5);
        meshView.setScaleY(5);
        meshView.setScaleZ(5);

        // Позиционирование графика
        meshView.setTranslateX(0); // Центр в SubScene
        meshView.setTranslateY(0); // Центр в SubScene

        // Добавление MeshView в группу для отображения
        group3D.getChildren().add(meshView);
    }
}
