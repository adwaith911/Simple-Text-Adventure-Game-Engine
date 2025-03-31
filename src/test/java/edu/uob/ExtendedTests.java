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

    // A lot of tests will probably check the game state using 'look' - so we better make sure 'look' works well !
    @Test
    void testLook() {
        String response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertTrue(response.contains("cabin"), "Did not see the name of the current room in response to look");
        assertTrue(response.contains("log cabin"), "Did not see a description of the room in response to look");
        assertTrue(response.contains("magic potion"), "Did not see a description of artifacts in response to look");
        assertTrue(response.contains("wooden trapdoor"), "Did not see description of furniture in response to look");
        assertTrue(response.contains("forest"), "Did not see available paths in response to look");
    }

    // Test that we can pick something up and that it appears in our inventory
    @Test
    void testGet()
    {
        String response;
        sendCommandToServer("simon: get potion");
        response = sendCommandToServer("simon: inv");
        response = response.toLowerCase();
        assertTrue(response.contains("potion"), "Did not see the potion in the inventory after an attempt was made to get it");
        response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertFalse(response.contains("potion"), "Potion is still present in the room after an attempt was made to get it");
    }

    // Test that we can goto a different location (we won't get very far if we can't move around the game !)
    @Test
    void testGoto()
    {
        sendCommandToServer("simon: goto forest");
        String response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertTrue(response.contains("key"), "Failed attempt to use 'goto' command to move to the forest - there is no key in the current location");
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

}

