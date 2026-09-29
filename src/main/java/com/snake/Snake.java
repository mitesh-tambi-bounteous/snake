package com.snake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Snake {

    private final LinkedList<Position> body;

    public Snake(List<Position> initialBody) {
        this.body = new LinkedList<>(initialBody);
    }

    public Position head() {
        return body.getFirst();
    }

    public int length() {
        return body.size();
    }

    public List<Position> segments() {
        return Collections.unmodifiableList(new ArrayList<>(body));
    }

    public boolean collidesExcludingTail(Position candidate) {
        return body.subList(0, body.size() - 1).contains(candidate);
    }

    public boolean occupies(Position candidate) {
        return body.contains(candidate);
    }

    public void advance(Position newHead, boolean grow) {
        body.addFirst(newHead);
        if (!grow) {
            body.removeLast();
        }
    }
}
