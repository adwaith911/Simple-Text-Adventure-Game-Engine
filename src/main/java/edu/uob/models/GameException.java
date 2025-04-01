package edu.uob.models;

import java.io.Serial;

public class GameException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public GameException(String message) {
        super(message);
    }
}