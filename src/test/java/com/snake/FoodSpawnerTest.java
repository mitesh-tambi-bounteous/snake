package com.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class FoodSpawnerTest {

    @Test
    void spawnRetriesUntilCandidateIsUnoccupied() {
        Snake snake = new Snake(List.of(new Position(0, 0), new Position(1, 0)));
        Random scripted = new Random() {
            private final int[] xs = {0, 2};
            private final int[] ys = {0, 2};
            private int call = 0;

            @Override
            public int nextInt(int bound) {
                int value = (call % 2 == 0) ? xs[call / 2] : ys[call / 2];
                call++;
                return value;
            }
        };
        Position spawned = new FoodSpawner(scripted).spawn(5, 5, snake);
        assertEquals(new Position(2, 2), spawned);
    }
}
