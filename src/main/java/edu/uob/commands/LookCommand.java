package edu.uob.commands;

import edu.uob.executor.CommandExecutor;
import edu.uob.executor.ExecutorException;

import java.util.HashMap;

public class LookCommand extends Command{
    public LookCommand(HashMap<String, Integer> tokenMap) {
       super(tokenMap);
    }

    public String execute(CommandExecutor executor) throws ExecutorException {
      return  executor.executeLookCommand(tokenMap);

    }
}
