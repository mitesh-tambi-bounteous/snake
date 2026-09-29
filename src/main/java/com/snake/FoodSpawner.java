package com.snake;

import java.util.Random;

public final class FoodSpawner {

    private final Random random;

    public FoodSpawner(Random random) {
        this.random = random;
    }

    public Position spawn(int width, int height, Snake snake) {
        Position candidate;
        do {
            candidate = new Position(random.nextInt(width), random.nextInt(height));
        } while (snake.occupies(candidate));
        return candidate;
    }
}
