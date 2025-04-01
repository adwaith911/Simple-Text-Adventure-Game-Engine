package edu.uob.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static edu.uob.Constants.MaxHealth;

/**
 * Class for storing player data. It contains
 * details about the inventory,cuurent location
 * and health of the player
 */

public class Player {

    String name;
    Map<String, GameEntity> inventory;
    Location currentLocation;
    int health = MaxHealth;


    public Player(String name) {
        this.name = name;
        this.inventory = new HashMap<>();
        this.currentLocation = null;

    }

    public Map<String, GameEntity> getInventory() {
        return inventory;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }


    public String getName() {
        return name;
    }

    public void removeFromInventory(String itemKey) {
        if (itemKey == null) {
            return;
        }

        if (this.inventory.containsKey(itemKey)) {
            this.inventory.remove(itemKey);
        }
    }

    public void addToInventory(String itemKey, GameEntity item) {

        if (!this.inventory.containsKey(itemKey)) {
            this.inventory.put(itemKey, item);
        }

    }

    public boolean hasItemInInventory(String itemKey) {
        if (itemKey == null) {
            return false;
        }
        return this.inventory.containsKey(itemKey);
    }

    public void increaseHealth() {
        if (health < MaxHealth) {
            health++;
        }
    }

    public void decreaseHealth() {
        if (this.health > 0) {
            this.health--;
        }
    }

    public void resetHealth() {
        this.health = MaxHealth;
    }

    public boolean isDead() {
        return health <= 0;
    }

}

