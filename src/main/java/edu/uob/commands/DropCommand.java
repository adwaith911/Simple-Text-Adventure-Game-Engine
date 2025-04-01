package edu.uob.commands;


import edu.uob.executor.CommandExecutor;
import edu.uob.executor.ExecutorException;

import java.util.HashMap;

public class DropCommand extends Command {
    public DropCommand(HashMap<String, Integer> tokenMap) {
        super(tokenMap);
    }

    public String execute(CommandExecutor executor) throws ExecutorException {
        return executor.executeDropCommand(tokenMap);

    }
}
