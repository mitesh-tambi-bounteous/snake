package com.snake;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.util.Random;

public final class GamePanel extends JPanel implements ActionListener {

    private static final String[] FOOD_GLYPHS = {"🍎", "🍌", "🍇", "🍊", "🍓", "🍒", "🍍"};

    private final Game game;
    private final int cellSize;
    private final Random glyphRandom = new Random();
    private Direction direction = Direction.RIGHT;
    private Position lastFoodPosition;
    private String currentFoodGlyph = FOOD_GLYPHS[0];

    public GamePanel(Game game, int cellSize) {
        this.game = game;
        this.cellSize = cellSize;
        setFocusable(true);
        bindArrowKey(KeyEvent.VK_UP, Direction.UP);
        bindArrowKey(KeyEvent.VK_DOWN, Direction.DOWN);
        bindArrowKey(KeyEvent.VK_LEFT, Direction.LEFT);
        bindArrowKey(KeyEvent.VK_RIGHT, Direction.RIGHT);
        bindRestartKey();
    }

    private void bindRestartKey() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "restart");
        getActionMap().put("restart", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.restart();
                repaint();
            }
        });
    }

    private void bindArrowKey(int keyCode, Direction mapped) {
        String actionKey = "direction_" + mapped.name();
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0), actionKey);
        getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                direction = mapped;
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        game.tick(direction);
        repaint();
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
        g.setColor(Color.RED);
        for (Position segment : game.snakeSegments()) {
            g.fillRect(segment.x() * cellSize, segment.y() * cellSize, cellSize, cellSize);
        }
        Position food = game.foodPosition();
        if (!food.equals(lastFoodPosition)) {
            lastFoodPosition = food;
            currentFoodGlyph = FOOD_GLYPHS[glyphRandom.nextInt(FOOD_GLYPHS.length)];
        }
        Font previousFont = g.getFont();
        g.setFont(previousFont.deriveFont(Font.PLAIN, (float) cellSize));
        FontMetrics foodMetrics = g.getFontMetrics();
        int glyphX = food.x() * cellSize + (cellSize - foodMetrics.stringWidth(currentFoodGlyph)) / 2;
        int glyphY = food.y() * cellSize + foodMetrics.getAscent();
        g.drawString(currentFoodGlyph, glyphX, glyphY);
        g.setFont(previousFont);

        g.setColor(Color.BLACK);
        String scoreText = "Score: " + game.score();
        int textWidth = g.getFontMetrics().stringWidth(scoreText);
        g.drawString(scoreText, (getWidth() - textWidth) / 2, 15);

        if (game.status() == GameStatus.GAME_OVER) {
            String overlayText = gameOverOverlayText();
            int overlayWidth = g.getFontMetrics().stringWidth(overlayText);
            g.drawString(overlayText, (getWidth() - overlayWidth) / 2, getHeight() / 2);
        }
    }

    String gameOverOverlayText() {
        return "Game Over  Score: " + game.score() + "  Press R to restart";
    }
}
