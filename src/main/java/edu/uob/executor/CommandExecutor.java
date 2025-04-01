package edu.uob.executor;

import edu.uob.models.GameAction;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * This is the interface defining the contract for any executor for objects of type Command.
 * Command pattern is used here, where in implementation classes of CommandExecutor
 * has concrete implementations for each of the methods defined here
 * <p>
 * This pattern enables us to switch between game command executors(implementations of CommandExecutor)
 * easily without affecting any other part of the codebase
 */

public interface CommandExecutor {

    String executeDropCommand(HashMap<String, Integer> tokenMap) throws ExecutorException;

    String executeGetCommand(HashMap<String, Integer> tokenMap) throws ExecutorException;

    String executeInvCommand(HashMap<String, Integer> tokenMap) throws ExecutorException;

    String executeLookCommand(HashMap<String, Integer> tokenMap) throws ExecutorException;

    String executeGotoCommand(HashMap<String, Integer> tokenMap) throws ExecutorException;

    String executeCustomCommand(HashMap<String, Integer> tokenMap, GameAction gameAction) throws ExecutorException;
}
