package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

class ExtendedTests {

    private GameServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    void setup() {
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
    }

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    void testMultiplePlayersGetSameItem()
    {
        String response = sendCommandToServer("simon: get axe");
        System.out.println(response);
        response = sendCommandToServer("simon: inv");
        System.out.println(response);
        assertTrue(response.contains("axe"), "Failed to pickup axe in earlier command");
        sendCommandToServer("adwaith: get axe");
        response = sendCommandToServer(" adwaith: inv");
        assertFalse(response.contains("axe"), "Non existent item was picked up");
    }

    @Test
    void testOpenTrapdoorConsumesKeyAndProducesCellar() {
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: open trapdoor with key");
        String response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertFalse(response.contains("key"), "Key was not consumed after opening the trapdoor");
        assertTrue(response.contains("cellar"), "Cellar path was not produced after opening the trapdoor");
    }

    @Test
    void testChopTreeConsumesTreeAndProducesLog() {

        sendCommandToServer("simon: get axe");
        sendCommandToServer("simon: goto forest");
        String response = sendCommandToServer("simon: chop tree with axe");
        response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertFalse(response.contains("tree"), "Tree was not consumed after chopping");
        assertTrue(response.contains("log"), "Log was not produced after chopping the tree");
    }

    @Test
    void testFightElfConsumesHealth() {
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: open trapdoor");
        sendCommandToServer("simon: goto cellar");
        sendCommandToServer("simon: fight elf");
        sendCommandToServer("simon: fight elf");
        String response =sendCommandToServer("simon: fight elf");
        response = response.toLowerCase();
        assertTrue(response.contains("dead"), "player should have died after losing three health points");
        response = sendCommandToServer("simon: inv");
        assertTrue(response.contains("empty"), "Inventory should have been cleared when player dies");

    }

    @Test
    void testBridgeRiverConsumesLogAndProducesClearing() {
        sendCommandToServer("SIMON: get axe");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: chop tree with axe");
        sendCommandToServer("simon: get log");
        sendCommandToServer("simon: goto riverbank");
        sendCommandToServer("simon: bridge river with log");
        String response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertFalse(response.contains("log"), "Log was not consumed after bridging the river");
        assertTrue(response.contains("clearing"), "Clearing path was not produced after bridging the river");
        sendCommandToServer("simon: goto clearing");
        response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertTrue(response.contains("ground"), "Player was not able to go to clearing");

    }

    @Test
    void testDigGroundConsumesGroundAndProducesHoleAndGold() {
        sendCommandToServer("simon: get axe");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: get coin");
        sendCommandToServer("simon: open trapdoor");
        sendCommandToServer("simon: goto cellar");
        sendCommandToServer("simon: pay coin");
        sendCommandToServer("simon: get shovel");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: chop tree with axe");
        sendCommandToServer("simon: get log");
        sendCommandToServer("simon: goto riverbank");
        sendCommandToServer("simon: bridge river with log");
        sendCommandToServer("simon: goto clearing");
        String response = sendCommandToServer("simon: dig ground with shovel");
        response = response.toLowerCase();
        response = sendCommandToServer("simon: look");
        assertTrue(response.contains("hole"), "Hole was not produced after digging");
        assertTrue(response.contains("gold"), "Gold was not produced after digging");
    }

    @Test
    void testBlowHornProducesLumberjack() {
        sendCommandToServer("simon: get axe");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: get coin");
        sendCommandToServer("simon: open trapdoor");
        sendCommandToServer("simon: goto cellar");
        sendCommandToServer("simon: pay coin");
        sendCommandToServer("simon: get shovel");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: chop tree with axe");
        sendCommandToServer("simon: get log");
        sendCommandToServer("simon: goto riverbank");
        sendCommandToServer("simon: blow horn");
        String response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertTrue(response.contains("horn"), "Horn should still be present as it wasn't consumed");
        assertTrue(response.contains("lumberjack"), "Lumberjack was not produced after blowing the horn");
    }

    @Test
    void testPlayersMaintainIndependentLocations() {

        sendCommandToServer("simon: goto forest");
        String simonLook = sendCommandToServer("simon: look").toLowerCase();
        String adwaithLook = sendCommandToServer("adwaith: look").toLowerCase();
        assertTrue(simonLook.contains("you are in forest"), "Simon should be in the forest");
        assertTrue(adwaithLook.contains("you are in cabin"), "Adwaith should remain in the starting location");
    }

    @Test
    void testMultiplePlayersinSameLocation() {
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("adwaith: goto forest");
        sendCommandToServer("sion: goto forest");
        String simonLook = sendCommandToServer("simon: look").toLowerCase();
        assertTrue(simonLook.contains("adwaith"), "Simon should be able to see adwaith");
        assertTrue(simonLook.contains("sion"), "Simon should be able to see sion");
    }

}

