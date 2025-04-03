package edu.uob.commands;

import edu.uob.executor.CommandExecutor;
import edu.uob.executor.ExecutorException;

import java.util.HashMap;

/**
 * Command serves as the abstract class for commands, with each command being a
 * concrete command subclass following the command design pattern.
 * <p>
 * The abstract execute method in this class delegates execution logic to any class
 * implementing CommandExecutor following visitor design pattern, this
 * is done so that execution logic is not tied on to command classes and can be changed easily
 * without affecting the command classes.
 */
public abstract class Command {
    protected HashMap<String, Integer> tokenMap;

    public Command(HashMap<String, Integer> tokenMap) {
        this.tokenMap = tokenMap;
    }

    public HashMap<String, Integer> getTokenMap() {
        return tokenMap;
    }


    public abstract String execute(CommandExecutor executor) throws ExecutorException;
}
