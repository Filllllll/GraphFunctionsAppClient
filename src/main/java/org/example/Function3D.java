package org.example;

public class Function3D {
    public float compute(float x, float y, float param1, float param2) {
        // Здесь можно изменить формулу на любую другую функцию
        return (float) (Math.sin(x * param1) * Math.cos(y * param2));
    }
}
