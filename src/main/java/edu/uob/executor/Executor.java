package edu.uob.executor;

import edu.uob.commands.Command;
import edu.uob.models.*;

import java.util.*;

public class Executor implements CommandExecutor {
    GameModel gameModel;

    public Executor(GameModel gameModel) throws ExecutorException {
        this.gameModel = gameModel;
    }

    public String execute(Command command) throws ExecutorException {
        return command.execute(this);
    }

    @Override
    public String executeDropCommand(HashMap<String, Integer> tokenMap) throws ExecutorException {
        try {
            Player currentPlayer = this.gameModel.getCurrentPlayer();
            Map<String, GameEntity> playerInventory = currentPlayer.getInventory();
            String item = checkEntityList(tokenMap, playerInventory);
            if (item == null) {
                throw new ExecutorException("Item to be dropped is not in inventory or multiple items in command");
            }
            GameEntity artefact = currentPlayer.getInventory().get(item);
            Location currentLocation = currentPlayer.getCurrentLocation();
            currentLocation.addAttribute(artefact.getId(), artefact);
            currentPlayer.removeFromInventory(artefact.getId());
            updateLocationInGameModel(currentLocation);
            updatePlayerInGameModel(currentPlayer);

            return new StringBuilder()
                    .append("You dropped a ")
                    .append(artefact.getId())
                    .append("\n")
                    .toString();
        } catch (Exception e) {
            throw new ExecutorException(e.getMessage());
        }
    }

    @Override
    public String executeGetCommand(HashMap<String, Integer> tokenMap) throws ExecutorException {
        try {
            Player currentPlayer = this.gameModel.getCurrentPlayer();
            Location currentLocation = currentPlayer.getCurrentLocation();
            String item = checkEntityList(tokenMap, currentLocation.getAttributes());
            if (item == null) {
                throw new ExecutorException("Invalid item to get or multiple items in command");
            }
            GameEntity artefact = currentLocation.getAttributes().get(item.toLowerCase());
            currentPlayer.addToInventory(artefact.getId(), artefact);
            currentLocation.removeAttribute(artefact.getId());
            updateLocationInGameModel(currentLocation);
            updatePlayerInGameModel(currentPlayer);
            return new StringBuilder()
                    .append("You picked up a ")
                    .append(artefact.getId())
                    .append("\n")
                    .toString();
        } catch (Exception e) {
            throw new ExecutorException(e.getMessage());
        }
    }

    @Override
    public String executeInvCommand(HashMap<String, Integer> tokenMap) throws ExecutorException {
        try {
            StringBuilder artefacts = new StringBuilder();
            Map<String, GameEntity> playerInventory = this.gameModel.getCurrentPlayer().getInventory();
            if (playerInventory.isEmpty()) {
                artefacts.append("Your inventory is empty.");
            } else {
                artefacts.append("The items in your inventory:\n");
                for (Map.Entry<String, GameEntity> entry : playerInventory.entrySet()) {
                    GameEntity entity = entry.getValue();
                    if (entity.getType().equals("artefact")) {
                        artefacts.append(entity.getId() + " - " + entity.getDescription() + "\n");
                    }
                }
            }
            return artefacts.toString();
        }catch(Exception e) {
            throw new ExecutorException(e.getMessage());
        }
    }

    @Override
    public String executeLookCommand(HashMap<String, Integer> tokenMap) throws ExecutorException {
        try {
            StringBuilder description = new StringBuilder();
            Location currentLocation = this.gameModel.getCurrentPlayer().getCurrentLocation();
            description.append(new StringBuilder()
                    .append("You are in ")
                    .append(currentLocation.getId())
                    .append(" - ")
                    .append(currentLocation.getDescription())
                    .append("\n\n"));
            if (!currentLocation.getAttributes().isEmpty()) {
                describeEntities(description, currentLocation);
            }
            if (!this.gameModel.getPaths().get(currentLocation.getId()).isEmpty()) {
                describePaths(description, currentLocation);
            }
            describeOtherPlayers(description);
            return description.toString();
        }catch(Exception e) {
            throw new ExecutorException(e.getMessage());
        }
    }

    @Override
    public String executeGotoCommand(HashMap<String, Integer> tokenMap) throws ExecutorException {
        try {
            Location currentLocation = this.gameModel.getCurrentPlayer().getCurrentLocation();
            String locationName = getPathToLocation(tokenMap, this.gameModel.getPaths(), currentLocation);
            if (locationName == null) {
                throw new ExecutorException("Path to this location doesnt exist from current location");
            }
            Map<String, GameEntity> entityList = this.gameModel.getEntityList();
            Location location = (Location) entityList.get(locationName);
            this.gameModel.getCurrentPlayer().setCurrentLocation(location);
            return new StringBuilder()
                    .append("you went to ")
                    .append(locationName)
                    .append("\n").toString();
        }catch(Exception e) {
            throw new ExecutorException(e.getMessage());
        }
    }

    @Override
    public String executeCustomCommand(HashMap<String, Integer> tokenMap, GameAction gameAction) throws ExecutorException {
        try {
            Set<String> consumedEntities = gameAction.getConsumed();
            for (String consumedEntity : consumedEntities) {
                consumeEntities(consumedEntity);
            }

            if (this.gameModel.getCurrentPlayer().isDead()) {
                this.gameModel.getCurrentPlayer().resetHealth();
                transferInventoryToCurrentLocation();
                Location startingLocation = this.gameModel.getStartingLocation();
                this.gameModel.getCurrentPlayer().setCurrentLocation(startingLocation);
                return "You are dead and will appear in starting location";
            }

            Set<String> producedEntities = gameAction.getProduced();
            if (!gameAction.getProduced().isEmpty()) {
                for (String producedEntity : producedEntities) {
                    produceEntities(producedEntity);
                }
            }
            return gameAction.getNarration();
        }catch(Exception e){
            throw new ExecutorException(e.getMessage());
        }
    }


    private void consumeEntities(String consumedEntity) throws ExecutorException {
        consumeHealth(consumedEntity);
        consumePath(consumedEntity);
        consumeFromPlayer(consumedEntity);
        consumeFromLocations(consumedEntity);
    }

    private void consumeHealth(String consumedEntity) {
        if(consumedEntity.equalsIgnoreCase("health")) {
            Player currentPlayer = this.gameModel.getCurrentPlayer();
            currentPlayer.decreaseHealth();
            updatePlayerInGameModel(currentPlayer);
        }
    }

    private void consumePath(String consumedEntity) {
        Player currentPlayer = this.gameModel.getCurrentPlayer();
        Location currentLocation = currentPlayer.getCurrentLocation();

        if(currentLocation.pathExists(consumedEntity)) {
            currentLocation.removePath(consumedEntity);
            this.gameModel.getPaths().get(currentLocation.getId()).remove(consumedEntity);
            updateLocationInGameModel(currentLocation);
        }
    }

    private void consumeFromPlayer(String consumedEntity) {
        Player currentPlayer = this.gameModel.getCurrentPlayer();
        Location storeRoom = getStoreRoom();

        if(currentPlayer.hasItemInInventory(consumedEntity)) {
            GameEntity subject = this.gameModel.getEntityList().get(consumedEntity);

            if(storeRoom != null) {
                storeRoom.addAttribute(consumedEntity, subject);
                updateLocationInGameModel(storeRoom);
            }

            currentPlayer.removeFromInventory(consumedEntity);
            updatePlayerInGameModel(currentPlayer);
        }
    }

    private void consumeFromLocations(String consumedEntity) {
        Location storeRoom = getStoreRoom();

        for (Map.Entry<String, GameEntity> entry : this.gameModel.getEntityList().entrySet()) {
            if(entry.getValue().getType().equals("location")) {
                Location location = (Location) entry.getValue();

                if (location.hasAttribute(consumedEntity)) {
                    GameEntity subject = this.gameModel.getEntityList().get(consumedEntity);

                    if (storeRoom != null) {
                        storeRoom.addAttribute(consumedEntity, subject);
                        updateLocationInGameModel(storeRoom);
                    }

                    location.removeAttribute(consumedEntity);
                    updateLocationInGameModel(location);
                }
            }
        }
    }



    private Location getStoreRoom() {
        return checkStoreRoomExists() ?
                (Location)this.gameModel.getEntityList().get("storeroom") :
                null;
    }



    private String checkEntityList(Map<String, Integer> tokenMap, Map<String, GameEntity> entityList) {
        String matchedKey = null;
        int matchCount = 0;

        for (String key : tokenMap.keySet()) {
            if (entityList.containsKey(key) && entityList.get(key).getType().equals("artefact")) {
                matchCount++;
                if (matchCount > 1) {
                    return null;
                }
                matchedKey = key;
            }
        }
        return matchedKey;
    }


    private void describeEntities(StringBuilder description, Location currentLocation) {
        // Get attributes from current location
        Map<String, GameEntity> entities = currentLocation.getAttributes();

        // Check if entities map is null or empty
        if (entities == null || entities.isEmpty()) {
            return;
        }
        description.append("The entities in this location are :").append("\n");
        // Iterate through the entities
        for (Map.Entry<String, GameEntity> entry : entities.entrySet()) {
            GameEntity entity = entry.getValue();
            description.append(entity.getId())
                    .append(" - ")
                    .append(entity.getDescription())
                    .append("\n");
        }
    }

    private void describePaths(StringBuilder description, Location currentLocation) {
        description.append("The paths to locations available from this place are: ").append("\n");
        for (String path : this.gameModel.getPaths().get(currentLocation.getId())) {
            description.append(path)
                    .append("\n");
        }
    }

    private void describeOtherPlayers(StringBuilder description) {
        if (this.gameModel.getPlayerList().size()<=1) {
            description.append("No other players in the game.\n");
            return;
        }

        description.append("The other players present in the game are: ").append("\n");

        for (Map.Entry<String, Player> entry : this.gameModel.getPlayerList().entrySet()) {
            Player player = entry.getValue();
            if (player.getName() != this.gameModel.getCurrentPlayer().getName()) {
                description.append(player.getName())
                        .append("\n");
            }
        }

    }

    private String getPathToLocation(Map<String, Integer> tokenMap, Map<String, HashSet<String>> pathList,Location currentLocation) {
        for (String key : tokenMap.keySet()) {
            if (pathList.get(currentLocation.getId()).contains(key)) {
                return key;
            }
        }
        return null;
    }



    private void produceEntities(String producedEntity) throws ExecutorException {
        produceToLocation(producedEntity);
        produceHealth(producedEntity);
        producePath(producedEntity);
    }

    private void produceToLocation(String producedEntity)  {
        Player currentPlayer = this.gameModel.getCurrentPlayer();
        Location currentLocation = currentPlayer.getCurrentLocation();
        Location storeRoom = getStoreRoom();
        if(checkStoreRoomExists()) {
            Map<String, GameEntity> attributes = storeRoom.getAttributes();
            if (storeRoom.hasAttribute(producedEntity)) {
                GameEntity entity = this.gameModel.getEntityList().get(producedEntity);
                currentLocation.addAttribute(producedEntity, entity);
                storeRoom.removeAttribute(producedEntity);
                updateLocationInGameModel(storeRoom);
                updateLocationInGameModel(currentLocation);
            }
        }


        for (Map.Entry<String, GameEntity> locationEntity : this.gameModel.getEntityList().entrySet()) {
            if(locationEntity.getValue().getType().equals("location")) {
                Location location = (Location) locationEntity.getValue();
                if (location.getId().equals(currentLocation.getId())) {
                    continue;
                }
                for (Map.Entry<String, GameEntity> entity : location.getAttributes().entrySet()) {
                    if (entity.getValue().getId().equalsIgnoreCase(producedEntity)) {
                        currentLocation.addAttribute(producedEntity, entity.getValue());
                        location.removeAttribute(producedEntity);
                        updateLocationInGameModel(currentLocation);
                        updateLocationInGameModel(location);
                        break;
                    }
                }
            }
        }
    }


    private void produceHealth(String producedEntity){
        if(producedEntity.equalsIgnoreCase("health")){
           this.gameModel.getCurrentPlayer().increaseHealth();
        }
    }

    private void producePath(String producedEntity){
        Location currentLocation = this.gameModel.getCurrentPlayer().getCurrentLocation();
        for (Map.Entry<String, GameEntity> locationEntity : this.gameModel.getEntityList().entrySet()) {
            if(locationEntity.getValue().getType().equals("location")) {
                if(locationEntity.getValue().getId().equalsIgnoreCase(producedEntity)) {
                    currentLocation.addPath(producedEntity);
                    updateLocationInGameModel(currentLocation);
                    this.gameModel.addPath(currentLocation.getId(),producedEntity);
                }
            }
        }
    }

    private boolean checkStoreRoomExists(){
        return this.gameModel.getEntityList().containsKey("storeroom");
    }

    private void updateLocationInGameModel(Location location) {
        this.gameModel.getEntityList().put(location.getId(), location);
    }

    private void updatePlayerInGameModel(Player player) {
        this.gameModel.getPlayerList().put(player.getName(), player);
    }

    private void transferInventoryToCurrentLocation() throws ExecutorException {
        Player currentPlayer = this.gameModel.getCurrentPlayer();
        Location currentLocation = currentPlayer.getCurrentLocation();

        Map<String, GameEntity> inventory = currentPlayer.getInventory();

        for (Map.Entry<String, GameEntity> entry : inventory.entrySet()) {
            String itemId = entry.getKey();
            GameEntity item = entry.getValue();
            currentLocation.addAttribute(itemId, item);
            currentPlayer.removeFromInventory(itemId);
        }

        updateLocationInGameModel(currentLocation);
        updatePlayerInGameModel(currentPlayer);
    }



}


