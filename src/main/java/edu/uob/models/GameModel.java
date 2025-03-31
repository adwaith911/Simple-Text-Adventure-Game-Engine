package edu.uob.models;

import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import com.alexmerz.graphviz.objects.Edge;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;

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

    public void setPlayerList(Map<String, Player> playerList) {
        this.playerList = playerList;
    }


    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        if(this.currentPlayer.getCurrentLocation()==null) {
            this.currentPlayer.setCurrentLocation(this.startingLocation);
        }
    }

    public void addPlayer(String playerName) {
        if(!playerList.containsKey(playerName)) {
           playerList.put(playerName, new Player(playerName));
        }
    }


    public GameModel(File entitiesFile,File actionsFile)  {
        try {
            entityList = new HashMap<>();
            paths = new HashMap<>();
            actionList = new HashMap<>();
            playerList = new HashMap<>();
            currentPlayer = null;
            startingLocation = null;
            this.loadEntities(entitiesFile);
            this.loadActionsFile(actionsFile);

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void loadActionsFile(File actionsFile) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(actionsFile);
        Element root = document.getDocumentElement();
        NodeList actions = root.getChildNodes();
        for(int i=1; i<actions.getLength(); i+=2){
            Element action = (Element) actions.item(i);
            GameAction newAction = new GameAction();
            Element triggers = (Element)action.getElementsByTagName("triggers").item(0);

            for(actionAttributeType type : actionAttributeType.values()) {
                Element currentType = (Element) action.getElementsByTagName(type.toString()).item(0);
                for(int k=0; k<currentType.getElementsByTagName("entity").getLength(); k++) {
                    String typePhrase = currentType.getElementsByTagName("entity").item(k).getTextContent();
                    newAction.addAttributes(type.toString(), typePhrase);
                }
            }
            // Add narration to newAction
            Element narration = (Element) action.getElementsByTagName("narration").item(0);
            String narrationSentence = narration.getTextContent();
            newAction.addNarration(narrationSentence);

            // Get trigger phrases
            for(int j=0; j<triggers.getElementsByTagName("keyphrase").getLength(); j++){
                String triggerPhrase = triggers.getElementsByTagName("keyphrase").item(j).getTextContent();
                // Check if the hashset of a trigger already exists
                if(actionList.containsKey(triggerPhrase)){
                    if(!actionExists(triggerPhrase, newAction)) {
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

    // Check if there are two identical actions in xml file
    private boolean actionExists(String triggerPhrase, GameAction newAction){
        HashSet<GameAction> actions = actionList.get(triggerPhrase);
        for(GameAction action : actions){
            if(action.getSubjects().equals(newAction.getSubjects()) || action.getNarration().equals(newAction.getNarration())
                    || action.getConsumed().equals(newAction.getConsumed()) || action.getProduced().equals(newAction.getProduced()))
                return true;
        }
        return false;
    }

    public void loadEntities(File dotFile) {
        try {
            Reader reader = new FileReader(dotFile);
            Parser parser = new Parser();
            parser.parse(reader);

            // Get the parsed graph
            List<Graph> graphs = parser.getGraphs();
            if (graphs.isEmpty()) {
                System.err.println("No graphs found in the DOT file.");
                return;
            }

            Graph mainGraph = graphs.get(0);
            List<Graph> subGraphs = mainGraph.getSubgraphs();

            // Process locations and their contents
            for (Graph subGraph : subGraphs) {
                if (subGraph.getId().getId().equals("locations")) {
                    this.processLocations(subGraph);
                } else if (subGraph.getId().getId().equals("paths")) {
                    this.processPaths(subGraph);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processLocations(Graph locationsGraph) {
        // Iterate over each cluster (each location)
        for (Graph clusterGraph : locationsGraph.getSubgraphs()) {
            String currentLocation = null;
            Location locationEntity = null;
            // Create a fresh map for each location's entities
            Map<String, GameEntity> locationEntityList = new HashMap<>();

            // Process nodes directly in the clusterGraph to find the location node.
            for (Node node : clusterGraph.getNodes(false)) {
                // If this node is not a subgraph, it represents the location.
                if (!node.isSubgraph()) {
                    currentLocation = node.getId().getId();
                    String description = this.getNodeAttribute(node, "description");
                    locationEntity = new Location(currentLocation, "location", description, null);
                    if(this.startingLocation == null){
                        this.startingLocation = locationEntity;
                    }
                    // Assume there's only one location node per cluster.
                    break;
                }
            }

            // If no location node was found, skip processing entities in this cluster.
            if (locationEntity == null) {
                continue;
            }

            // Process each subgraph within the cluster for artefacts, furniture, or characters.
            for (Graph subgraph : clusterGraph.getSubgraphs()) {
                String entityType = subgraph.getId().getId(); // e.g., "artefacts", "furniture", or "characters"
                for (Node subNode : subgraph.getNodes(false)) {
                    String entityId = subNode.getId().getId();
                    String description = this.getNodeAttribute(subNode, "description");

                    // Skip nodes that are just default definitions
                    if (entityId.equals("node")) {
                        continue;
                    }

                    // Determine the entity type based on the subgraph id
                    String type;
                    if (entityType.equals("artefacts")) {
                        type = "artefact";
                    } else if (entityType.equals("furniture")) {
                        type = "furniture";
                    } else if (entityType.equals("characters")) {
                        type = "character";
                    } else {
                        type = "unknown";
                    }

                    // Create the GameEntity and add it to both the location-specific map and the global entity list.
                    GameEntity entity = new GameEntity(entityId, type, description);
                    locationEntityList.put(entityId, entity);
                    entityList.put(entityId, entity);
                }
            }
            // Set the collected attributes for this location and add the location entity to the global list.
            locationEntity.setAttributes(locationEntityList);
            entityList.put(locationEntity.getId(), locationEntity);
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
