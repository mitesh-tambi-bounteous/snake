package com.snake;

import java.util.List;

public final class Game {

    private final int width;
    private final int height;
    private final Snake snake;
    private final FoodSpawner foodSpawner;
    private final List<Position> initialSnakeBody;
    private Food food;
    private int score = 0;
    private GameStatus status = GameStatus.RUNNING;

    public Game(int width, int height, Snake snake, Food food, FoodSpawner foodSpawner) {
        this.width = width;
        this.height = height;
        this.snake = snake;
        this.food = food;
        this.foodSpawner = foodSpawner;
        this.initialSnakeBody = List.copyOf(snake.segments());
    }

    public void restart() {
        snake.reset(initialSnakeBody);
        score = 0;
        status = GameStatus.RUNNING;
        food = new Food(foodSpawner.spawn(width, height, snake));
    }

    public void tick(Direction direction) {
        if (status != GameStatus.RUNNING) {
            return;
        }
        Position head = snake.head();
        Position next = new Position(head.x() + direction.dx, head.y() + direction.dy);
        boolean hitsWall = next.x() < 0 || next.y() < 0 || next.x() >= width || next.y() >= height;
        if (hitsWall || snake.collidesExcludingTail(next)) {
            status = GameStatus.GAME_OVER;
            return;
        }
        boolean ateFood = next.equals(food.position());
        snake.advance(next, ateFood);
        if (ateFood) {
            score++;
            food = new Food(foodSpawner.spawn(width, height, snake));
        }
    }

    public List<Position> snakeSegments() {
        return snake.segments();
    }

    public Position foodPosition() {
        return food.position();
    }

    public int score() {
        return score;
    }

    public int snakeLength() {
        return snake.length();
    }

    public GameStatus status() {
        return status;
    }
}
