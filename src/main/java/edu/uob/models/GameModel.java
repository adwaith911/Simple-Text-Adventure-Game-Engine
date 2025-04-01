package edu.uob.models;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import com.alexmerz.graphviz.objects.Edge;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;

/**
 * /**
 * The GameModel class serves as a singleton that maintains the game state,
 * including players, locations, entities, paths, and actions. It acts as
 * the single source of truth for the game world, ensuring consistency
 * across all updates and actions.
 * <p>
 * Implements the Singleton design pattern to ensure that only one instance
 * of GameModel exists throughout the game's lifecycle.
 */
public class GameModel {

    private Map<String, GameEntity> entityList;
    private Map<String, HashSet<String>> paths;
    private Map<String, HashSet<GameAction>> actionList;
    private Map<String, Player> playerList;
    private Player currentPlayer;
    private Location startingLocation;

    public Location getStartingLocation() {
        return startingLocation;
    }

    public Map<String, Player> getPlayerList() {
        return playerList;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        if (this.currentPlayer.getCurrentLocation() == null) {
            this.currentPlayer.setCurrentLocation(this.startingLocation);
        }
    }

    public void addPlayer(String playerName) {
        if (!playerList.containsKey(playerName)) {
            playerList.put(playerName, new Player(playerName));
        }
    }


    public GameModel(File entitiesFile, File actionsFile) throws Exception {
        try {
            entityList = new HashMap<>();
            paths = new HashMap<>();
            actionList = new HashMap<>();
            playerList = new HashMap<>();
            currentPlayer = null;
            startingLocation = null;
            this.loadEntities(entitiesFile);
            this.loadActionsFile(actionsFile);

        } catch (Exception e) {
            throw new GameException(String.format("Error while loading game: %s", e.getMessage()));
        }
    }

    private void loadActionsFile(File actionsFile) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(actionsFile);
        Element root = document.getDocumentElement();
        NodeList actions = root.getChildNodes();
        for (int i = 1; i < actions.getLength(); i += 2) {
            Element action = (Element) actions.item(i);
            GameAction newAction = new GameAction();
            Element triggers = (Element) action.getElementsByTagName("triggers").item(0);
            for (ActionAttribute attribute : ActionAttribute.values()) {
                Element currentType = (Element) action.getElementsByTagName(attribute.toString()).item(0);
                for (int k = 0; k < currentType.getElementsByTagName("entity").getLength(); k++) {
                    String typePhrase = currentType.getElementsByTagName("entity").item(k).getTextContent();
                    newAction.addAttributes(attribute.toString(), typePhrase);
                }
            }
            Element narration = (Element) action.getElementsByTagName("narration").item(0);
            String narrationSentence = narration.getTextContent();
            newAction.addNarration(narrationSentence);

            for (int j = 0; j < triggers.getElementsByTagName("keyphrase").getLength(); j++) {
                String triggerPhrase = triggers.getElementsByTagName("keyphrase").item(j).getTextContent();
                if (actionList.containsKey(triggerPhrase)) {
                    if (!actionExists(triggerPhrase, newAction)) {
                        actionList.get(triggerPhrase).add(newAction);
                        actionList.put(triggerPhrase, actionList.get(triggerPhrase));
                    }
                } else {
                    HashSet<GameAction> list = new HashSet<>();
                    list.add(newAction);
                    actionList.put(triggerPhrase, list);
                }
            }

        }

    }

    private boolean actionExists(String triggerPhrase, GameAction newAction) {
        HashSet<GameAction> actions = actionList.get(triggerPhrase);
        for (GameAction action : actions) {
            if (action.getSubjects().equals(newAction.getSubjects()) || action.getNarration().equals(newAction.getNarration())
                    || action.getConsumed().equals(newAction.getConsumed()) || action.getProduced().equals(newAction.getProduced()))
                return true;
        }
        return false;
    }

    public void loadEntities(File dotFile) throws GameException, FileNotFoundException, ParseException {
        try {
            Reader reader = new FileReader(dotFile);
            Parser parser = new Parser();
            parser.parse(reader);
            List<Graph> graphs = parser.getGraphs();
            if (graphs.isEmpty()) {
                throw new GameException("Entities file is empty");
            }
            Graph mainGraph = graphs.get(0);
            List<Graph> subGraphs = mainGraph.getSubgraphs();

            for (Graph subGraph : subGraphs) {
                if (subGraph.getId().getId().equals("locations")) {
                    this.processLocations(subGraph);
                } else if (subGraph.getId().getId().equals("paths")) {
                    this.processPaths(subGraph);
                }
            }

        } catch (Exception e) {
            throw e;
        }
    }

    private void processLocations(Graph locationsGraph) {
        for (Graph clusterGraph : locationsGraph.getSubgraphs()) {
            Location locationEntity = this.extractLocation(clusterGraph);
            if (locationEntity == null) {
                continue;
            }
            Map<String, GameEntity> locationEntityList = extractEntities(clusterGraph);
            locationEntity.setAttributes(locationEntityList);
            entityList.put(locationEntity.getId(), locationEntity);
        }
    }

    private Location extractLocation(Graph clusterGraph) {
        for (Node node : clusterGraph.getNodes(false)) {
            if (!node.isSubgraph()) {
                String currentLocation = node.getId().getId();
                String description = this.getNodeAttribute(node, "description");
                Location location = new Location(currentLocation, "location", description, null);
                if (this.startingLocation == null) {
                    this.startingLocation = location;
                }
                return location;
            }
        }
        return null;
    }

    private Map<String, GameEntity> extractEntities(Graph clusterGraph) {
        Map<String, GameEntity> locationEntityList = new HashMap<>();
        for (Graph subgraph : clusterGraph.getSubgraphs()) {
            String entityType = subgraph.getId().getId();
            for (Node subNode : subgraph.getNodes(false)) {
                this.processEntity(subNode, entityType, locationEntityList);
            }
        }
        return locationEntityList;
    }

    private void processEntity(Node subNode, String entityType, Map<String, GameEntity> locationEntityList) {
        String entityId = subNode.getId().getId();
        if (entityId.equals("node")) {
            return;
        }
        String description = this.getNodeAttribute(subNode, "description");
        String type = this.getEntityType(entityType);
        GameEntity entity = new GameEntity(entityId, type, description);
        locationEntityList.put(entityId, entity);
        entityList.put(entityId, entity);
    }

    private String getEntityType(String entityType) {
        switch (entityType) {
            case "artefacts":
                return "artefact";
            case "furniture":
                return "furniture";
            case "characters":
                return "character";
            default:
                return "unknown";
        }
    }

    private void processPaths(Graph pathsGraph) {
        for (Edge edge : pathsGraph.getEdges()) {
            String from = edge.getSource().getNode().getId().getId();
            String to = edge.getTarget().getNode().getId().getId();

            if (!paths.containsKey(from)) {
                paths.put(from, new HashSet<>());
            }
            if (this.entityList.containsKey(from)) {
                GameEntity entity = this.entityList.get(from);
                if (entity instanceof Location) {
                    ((Location) entity).addPath(to);
                }
            }

            paths.get(from).add(to);
        }
    }

    private String getNodeAttribute(Node node, String attributeName) {
        String value = node.getAttribute(attributeName);
        return value;
    }

    public Map<String, GameEntity> getEntityList() {
        return entityList;
    }

    public Map<String, HashSet<String>> getPaths() {
        return paths;
    }

    public Map<String, HashSet<GameAction>> getActionList() {
        return actionList;
    }

    public void addPath(String fromLocation, String toLocation) {
        if (!paths.containsKey(fromLocation)) {
            paths.put(fromLocation, new HashSet<>());
        }
        paths.get(fromLocation).add(toLocation);
    }

}
