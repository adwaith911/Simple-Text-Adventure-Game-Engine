package edu.uob.executor;

import edu.uob.models.GameAction;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public interface CommandExecutor {

    String executeDropCommand(HashMap<String, Integer> tokenMap) throws ExecutorException;

    String executeGetCommand(HashMap<String, Integer> tokenMap) throws ExecutorException;

    String executeInvCommand(HashMap<String, Integer> tokenMap) throws ExecutorException;

    String executeLookCommand(HashMap<String, Integer> tokenMap) throws ExecutorException;

    String executeGotoCommand(HashMap<String, Integer> tokenMap) throws ExecutorException;

    String executeCustomCommand(HashMap<String, Integer> tokenMap, GameAction gameAction) throws ExecutorException;
}
