package edu.uob.tokeniser;

import java.io.Serial;

public class TokeniserException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public TokeniserException(String message) {
        super(message);
    }

}
