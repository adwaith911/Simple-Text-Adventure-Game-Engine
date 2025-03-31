package edu.uob.commands;

import edu.uob.executor.CommandExecutor;
import edu.uob.executor.ExecutorException;

import java.util.HashMap;

// BasicCommand Class
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
