package edu.uob.models;

import java.util.HashSet;
import java.util.Map;

/**
 * Class for storing location entity data. It contains
 * details about the entities present in each location
 * and also paths available to and from each location
 */

public class Location extends GameEntity {

    Map<String, GameEntity> attributes;
    HashSet<String> paths;

    public Location(String id, String type, String description, Map<String, GameEntity> attributes) {
        super(id, type, description);
        this.attributes = attributes;
        this.paths = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public void removeAttribute(String name) {
        if (this.attributes.containsKey(name)) {
            this.attributes.remove(name);
        }
    }

    public void setAttributes(Map<String, GameEntity> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String name, GameEntity attribute) {
        attributes.put(name, attribute);
    }

    public Map<String, GameEntity> getAttributes() {
        return attributes;
    }

    public void addPath(String path) {
        paths.add(path);
    }

    public boolean pathExists(String path) {
        return paths.contains(path);
    }

    public void removePath(String path) {
        paths.remove(path);
    }

    public boolean hasAttribute(String attributeName) {
        return this.attributes.containsKey(attributeName);
    }

}
