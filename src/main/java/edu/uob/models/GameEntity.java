package edu.uob.models;

/**
 * Class for storing the game entities fetched from entities.dot file
 */

public class GameEntity {
    protected String id;
    protected String type;
    protected String description;

    public GameEntity(String id, String type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;

    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

}
