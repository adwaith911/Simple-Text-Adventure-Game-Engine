package edu.uob.commands;


import edu.uob.executor.CommandExecutor;
import edu.uob.executor.ExecutorException;

import java.util.HashMap;

public class GotoCommand extends Command{

    public GotoCommand(HashMap<String, Integer> tokenMap) {
        super(tokenMap);
    }

    public String execute(CommandExecutor executor) throws ExecutorException {
        return executor.executeGotoCommand(tokenMap);
    }

}
