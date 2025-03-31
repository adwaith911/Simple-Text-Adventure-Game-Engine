package edu.uob.commands;

import edu.uob.executor.CommandExecutor;
import edu.uob.executor.ExecutorException;

import java.util.HashMap;

public class InvCommand extends  Command{
    public InvCommand (HashMap<String, Integer> tokenMap) {
        super(tokenMap);
    }

    public String execute(CommandExecutor executor) throws ExecutorException {
       return executor.executeInvCommand(tokenMap);

    }

}
