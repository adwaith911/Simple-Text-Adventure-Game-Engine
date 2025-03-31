package edu.uob.parser;

import edu.uob.commands.*;
import edu.uob.executor.ExecutorException;
import edu.uob.models.GameAction;
import edu.uob.models.GameEntity;
import edu.uob.models.GameModel;
import edu.uob.models.Player;

import java.util.*;

public class Parser {
    private HashMap<String, Integer> tokenMap;
    private final HashSet<String> commands = new HashSet<>(Arrays.asList("inventory", "inv", "get", "drop", "goto", "look"));
    private GameModel gameModel;
    private String currentPlayer;

    public Parser(HashMap<String, Integer> tokenMap, GameModel gameModel, String currentPlayer) {
        this.tokenMap = tokenMap;
        this.gameModel = gameModel;
        this.currentPlayer = currentPlayer;

    }

    public Command parseCommand() throws ParserException {
        try {
            if (this.tokenMap == null || this.tokenMap.isEmpty()) {
                throw new ParserException("Token map cannot be null or empty");
            }

            if (!this.gameModel.getPlayerList().containsKey(this.currentPlayer)) {
                this.gameModel.addPlayer(this.currentPlayer);
            }

            this.gameModel.setCurrentPlayer(this.gameModel.getPlayerList().get(this.currentPlayer));

            String matchedCommand = this.findMatchingCommand(this.tokenMap);

            switch (matchedCommand) {
                case "inventory":
                case "inv":
                    return new InvCommand(this.tokenMap);
                case "get":
                    return new GetCommand(this.tokenMap);
                case "drop":
                    return new DropCommand(this.tokenMap);
                case "goto":
                    return new GotoCommand(this.tokenMap);
                case "look":
                    return new LookCommand(this.tokenMap);
                default:
                    if (this.validateCommand(this.tokenMap)) {
                        if (this.getPossibleActions(this.tokenMap).size() == 1) {
                            return new CustomCommand(this.tokenMap, this.getPossibleActions(this.tokenMap).iterator().next());
                        } else {
                            throw new ParserException("Ambiguous command, multiple possible actions");
                        }
                    }
                    throw new ParserException("Invalid command");
            }
        } catch (Exception e) {
            throw new ParserException(String.format("Error parsing command: %s", e.getMessage()));
        }
    }
    private String findMatchingCommand(HashMap<String, Integer> tokenMap) {

        for (String command : commands) {
            if (tokenMap.containsKey(command)) {
                return command;
            }
        }
        return "";
    }

    private boolean validateCommand(HashMap<String, Integer> tokenMap) throws ParserException {
        for (Map.Entry<String, HashSet<GameAction>> entry : this.gameModel.getActionList().entrySet()) {
            if (tokenMap.containsKey(entry.getKey())) {
                return true;
            }
        }
        throw new ParserException("Command doesnt exist");
    }

    private HashMap<String, GameEntity> getAvailableEntities() {
        HashMap<String, GameEntity> availableEntities = new HashMap<>();
        Player currentPlayer = this.gameModel.getCurrentPlayer();
        availableEntities.putAll(currentPlayer.getInventory());
        availableEntities.putAll(currentPlayer.getCurrentLocation().getAttributes());
        return availableEntities;
    }

    private Set<String> checkSubjectInCommand(HashSet<GameAction> gameActions)  {
        for (GameAction action : gameActions) {
            for (String subject : action.getSubjects()) {
                if (tokenMap.containsKey(subject)) {
                    return action.getSubjects();
                }
            }
        }
        return null;
    }

    private boolean checkSubjectAvailability(Set<String> subjects) throws ParserException {
        HashMap<String, GameEntity> availableEntities = getAvailableEntities();
        for (String subject: subjects) {
            if (!availableEntities.containsKey(subject)) {
                throw new ParserException("Subject not available for this acion");
            }
        }


        return true;
    }

    private HashSet<GameAction> getPossibleActions(HashMap<String, Integer> tokenMap) throws  ParserException {
        HashSet<GameAction> possibleActions = new HashSet<>();
        for (Map.Entry<String, HashSet<GameAction>> entry : this.gameModel.getActionList().entrySet()) {
            if (tokenMap.containsKey(entry.getKey())) {
                Set<String> subjects = this.checkSubjectInCommand(entry.getValue());
                if (subjects != null) {
                    if (checkSubjectAvailability(subjects))
                        possibleActions.addAll(entry.getValue());
                }else{
                   throw new ParserException("Subject not available for this action");
                }
            }
        }
        return possibleActions;
    }

}
