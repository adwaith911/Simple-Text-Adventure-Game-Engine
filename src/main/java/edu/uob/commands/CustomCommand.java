package edu.uob.commands;

import edu.uob.executor.CommandExecutor;
import edu.uob.executor.ExecutorException;
import edu.uob.models.GameAction;
import edu.uob.models.GameEntity;
import edu.uob.models.GameModel;
import edu.uob.models.Player;

import java.util.*;

public class CustomCommand extends Command{

    GameAction gameAction;
    public CustomCommand(HashMap<String, Integer> tokenMap, GameAction gameAction) {
        super(tokenMap);
        this.gameAction = gameAction;
    }

    @Override
    public String execute(CommandExecutor executor) throws ExecutorException {
        return executor.executeCustomCommand(this.tokenMap, this.gameAction);
    }
}
