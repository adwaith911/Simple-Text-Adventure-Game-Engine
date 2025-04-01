package edu.uob.tokeniser;

import java.util.HashMap;


public class Tokeniser {

    private String command;
    private String playerName;

    public Tokeniser(String command) {
        this.command = command;
    }

    public HashMap<String, Integer> splitIntoTokenMap() throws TokeniserException {
        try {
            int delimiterIndex = command.indexOf(':');
            if (delimiterIndex == -1) {
                throw new TokeniserException("Command must contain a ':'");
            }
            playerName = command.substring(0, delimiterIndex).toLowerCase();
            String commands = command.substring(++delimiterIndex);
            HashMap<String, Integer> tokenMap = new HashMap<>();
            for (String token : commands.toLowerCase().split("[,.:!?()]|\\s+")) {
                if (token != null && !token.isEmpty()) {
                    tokenMap.put(token, tokenMap.getOrDefault(token, 0) + 1);
                }
            }
            return tokenMap;
        } catch (Exception e) {
            throw new TokeniserException(String.format("Error occurred during tokenisation: %s", e.getMessage()));
        }
    }

    public String getPlayerName() {
        return playerName;
    }

}
