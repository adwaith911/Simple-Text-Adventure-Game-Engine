package edu.uob.parser;


import java.io.Serial;

public class ParserException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public ParserException(String message) {
        super(message);
    }


}