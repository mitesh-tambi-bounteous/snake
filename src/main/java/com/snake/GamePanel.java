package com.snake;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;

public final class GamePanel extends JPanel implements ActionListener, KeyListener {

    private final Game game;
    private final int cellSize;
    private Direction direction = Direction.RIGHT;

    public GamePanel(Game game, int cellSize) {
        this.game = game;
        this.cellSize = cellSize;
        setFocusable(true);
        addKeyListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        game.tick(direction);
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Direction mapped = mapKey(e.getKeyCode());
        if (mapped != null) {
            direction = mapped;
        }
    }

    static Direction mapKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
                return Direction.UP;
            case KeyEvent.VK_DOWN:
                return Direction.DOWN;
            case KeyEvent.VK_LEFT:
                return Direction.LEFT;
            case KeyEvent.VK_RIGHT:
                return Direction.RIGHT;
            default:
                return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.GREEN);
        for (Position segment : game.snakeSegments()) {
            g.fillRect(segment.x() * cellSize, segment.y() * cellSize, cellSize, cellSize);
        }
        g.setColor(Color.RED);
        Position food = game.foodPosition();
        g.fillRect(food.x() * cellSize, food.y() * cellSize, cellSize, cellSize);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
